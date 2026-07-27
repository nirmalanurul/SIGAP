package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.ADT.Kios;
import com.sigap.ADT.Penyewa;
import com.sigap.ADT.Penyewaan;
import com.sigap.APP.CRUD_Karyawan;
import com.sigap.APP.CRUD_Kios;
import com.sigap.APP.CRUD_Penyewa;
import com.sigap.APP.CRUD_Penyewaan;
import com.sigap.util.Session;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PenyewaanController implements Initializable {

    // 1. FXML FIELDS — FORM INPUT
    @FXML
    private TextField txtIdPenyewaan;
    @FXML
    private TextField txtNamaKaryawan;
    @FXML
    private TextField txtPenyewaTerpilih;
    @FXML
    private TextField txtKiosTerpilih;
    @FXML
    private DatePicker dpTglMulai;
    @FXML
    private DatePicker dpTglSelesai;
    @FXML
    private TextField txtTglPenyewaan;
    @FXML
    private TextField txtStatus;

    @FXML
    private Button btnPilihPenyewa;
    @FXML
    private Button btnPilihKios;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnBatalkan;

    // 2. FXML FIELDS — PENCARIAN, FILTER & TABLE
    @FXML
    private TextField txtCari;
    @FXML
    private MenuButton btnFilter;
    @FXML
    private Menu menuPenyewa;
    @FXML
    private Menu menuKios;
    @FXML
    private RadioMenuItem rmStatusMenunggu;
    @FXML
    private RadioMenuItem rmStatusBerlangsung;
    @FXML
    private RadioMenuItem rmStatusSelesai;
    @FXML
    private RadioMenuItem rmStatusDibatalkan;
    @FXML
    private TableView<Penyewaan> tabelPenyewaan;
    @FXML
    private TableColumn<Penyewaan, String> colId;
    @FXML
    private TableColumn<Penyewaan, String> colKios;
    @FXML
    private TableColumn<Penyewaan, String> colPenyewa;
    @FXML
    private TableColumn<Penyewaan, String> colKaryawan;
    @FXML
    private TableColumn<Penyewaan, String> colTglMulai;
    @FXML
    private TableColumn<Penyewaan, String> colTglSelesai;
    @FXML
    private TableColumn<Penyewaan, String> colTglTransaksi;
    @FXML
    private TableColumn<Penyewaan, String> colStatus;

    // 3. FXML FIELDS — PAGINATION
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;

    // 4. STATE
    /** Seluruh data penyewaan hasil load dari DB, sebelum kata kunci pencarian / filter diterapkan. */
    private List<Penyewaan> rawList = new ArrayList<>();
    /** Hasil setelah pencarian + filter diterapkan, inilah yang dipaginasi ke tabel. */
    private final ObservableList<Penyewaan> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    // Peta master, dipakai untuk join tampilan nama & detail popup di tabel
    private Map<String, Penyewa> petaPenyewa = Map.of();
    private Map<String, Kios> petaKios = Map.of();
    private Map<String, Karyawan> petaKaryawan = Map.of();

    // Kriteria filter aktif (null = tidak difilter pada kolom itu)
    private String filterIdKios = null;
    private String filterIdPenyewa = null;
    private String filterStatus = null;

    // Data terpilih dari dialog picker (Id, bukan cuma tampilan teks)
    private Penyewa penyewaTerpilih = null;
    private Kios kiosTerpilih = null;

    // Baris yang sedang dipilih di tabel (untuk aksi Batalkan)
    private Penyewaan selectedPenyewaan = null;

    // Format tanggal tampilan: "22 Juli 2026" (nama bulan, bukan angka)
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;-fx-text-fill:#888;";

    // 5. INITIALIZE
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdPenyewaan.setEditable(false);
        txtNamaKaryawan.setEditable(false);
        txtPenyewaTerpilih.setEditable(false);
        txtKiosTerpilih.setEditable(false);
        txtTglPenyewaan.setEditable(false);
        txtStatus.setEditable(false);
        txtStatus.setStyle(STYLE_READONLY);

        setupTable();
        setupDatePickers();
        setupFilterStatus();
        setFormState(false);

        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
            isiKaryawanLogin();
            txtTglPenyewaan.setText(LocalDate.now().format(FMT_TGL));
            txtStatus.setText("Menunggu");
        });
    }

    // 6. DATE PICKER — batasi tanggal mulai & selesai
    // Urutan input yang benar: Tanggal Mulai & Selesai diisi DULU, baru tombol
    // "Pilih Penyewa" / "Pilih Kios" aktif — karena ketersediaan kios ditentukan
    // oleh rentang tanggal ini.
    private void setupDatePickers() {
        dpTglMulai.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate batasMaks = LocalDate.now().plusYears(1);
                setDisable(empty || date.isBefore(LocalDate.now()) || date.isAfter(batasMaks));
            }
        });

        dpTglSelesai.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate acuanMulai = dpTglMulai.getValue() != null ? dpTglMulai.getValue() : LocalDate.now();
                LocalDate batasMin = acuanMulai.plusMonths(1);
                LocalDate batasMaks = acuanMulai.plusYears(5);
                setDisable(empty || date.isBefore(batasMin) || date.isAfter(batasMaks));
            }
        });

        dpTglMulai.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpTglSelesai.getValue() != null && dpTglSelesai.getValue().isBefore(newVal)) {
                dpTglSelesai.setValue(null);
            }
            onRentangTanggalBerubah();
        });
        dpTglSelesai.valueProperty().addListener((obs, oldVal, newVal) -> onRentangTanggalBerubah());

        // Tampilan DatePicker (kotak input) juga pakai format nama bulan, konsisten dengan tabel.
        dpTglMulai.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override public String toString(LocalDate date) { return date == null ? "" : date.format(FMT_TGL); }
            @Override public LocalDate fromString(String s) { return (s == null || s.isBlank()) ? null : LocalDate.parse(s, FMT_TGL); }
        });
        dpTglSelesai.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override public String toString(LocalDate date) { return date == null ? "" : date.format(FMT_TGL); }
            @Override public LocalDate fromString(String s) { return (s == null || s.isBlank()) ? null : LocalDate.parse(s, FMT_TGL); }
        });
    }

    /**
     * Dipanggil setiap kali Tanggal Mulai / Selesai berubah. Penyewa & Kios yang sudah
     * sempat dipilih di-reset karena ketersediaannya bisa berubah kalau rentang tanggal
     * berubah, lalu tombol Pilih Penyewa/Kios di-enable/disable ulang.
     */
    private void onRentangTanggalBerubah() {
        if (penyewaTerpilih != null || kiosTerpilih != null) {
            penyewaTerpilih = null;
            kiosTerpilih = null;
            txtPenyewaTerpilih.clear();
            txtKiosTerpilih.clear();
        }
        updateTombolPilih();
    }

    private void updateTombolPilih() {
        boolean sedangTerkunci = selectedPenyewaan != null;
        boolean tanggalLengkap = dpTglMulai.getValue() != null
                && dpTglSelesai.getValue() != null
                && !dpTglSelesai.getValue().isBefore(dpTglMulai.getValue());
        btnPilihPenyewa.setDisable(sedangTerkunci || !tanggalLengkap);
        btnPilihKios.setDisable(sedangTerkunci || !tanggalLengkap);
    }

    /** ToggleGroup untuk RadioMenuItem status di menu FILTER, supaya cuma 1 status aktif dalam satu waktu. */
    private void setupFilterStatus() {
        ToggleGroup grupStatus = new ToggleGroup();
        rmStatusMenunggu.setToggleGroup(grupStatus);
        rmStatusBerlangsung.setToggleGroup(grupStatus);
        rmStatusSelesai.setToggleGroup(grupStatus);
        rmStatusDibatalkan.setToggleGroup(grupStatus);
    }

    /**
     * Mengisi submenu Penyewa & Kios di menu FILTER secara dinamis (item-nya tergantung data master),
     * dipanggil ulang tiap kali peta master selesai dimuat.
     */
    private void populateFilterMenus() {
        menuPenyewa.getItems().clear();
        ToggleGroup grupPenyewa = new ToggleGroup();
        RadioMenuItem rmSemuaPenyewa = new RadioMenuItem("Semua Penyewa");
        rmSemuaPenyewa.setToggleGroup(grupPenyewa);
        rmSemuaPenyewa.setSelected(filterIdPenyewa == null);
        rmSemuaPenyewa.setOnAction(e -> { filterIdPenyewa = null; terapkanFilterDanCari(); });
        menuPenyewa.getItems().add(rmSemuaPenyewa);
        petaPenyewa.values().forEach(p -> {
            RadioMenuItem rmi = new RadioMenuItem(p.getIdPenyewa() + " - " + p.getNamaPenyewa());
            rmi.setToggleGroup(grupPenyewa);
            rmi.setSelected(p.getIdPenyewa().equalsIgnoreCase(filterIdPenyewa));
            rmi.setOnAction(e -> { filterIdPenyewa = p.getIdPenyewa(); terapkanFilterDanCari(); });
            menuPenyewa.getItems().add(rmi);
        });

        menuKios.getItems().clear();
        ToggleGroup grupKios = new ToggleGroup();
        RadioMenuItem rmSemuaKios = new RadioMenuItem("Semua Kios");
        rmSemuaKios.setToggleGroup(grupKios);
        rmSemuaKios.setSelected(filterIdKios == null);
        rmSemuaKios.setOnAction(e -> { filterIdKios = null; terapkanFilterDanCari(); });
        menuKios.getItems().add(rmSemuaKios);
        petaKios.values().forEach(k -> {
            RadioMenuItem rmi = new RadioMenuItem(k.getIdKios() + " - " + k.getDeskripsi());
            rmi.setToggleGroup(grupKios);
            rmi.setSelected(k.getIdKios().equalsIgnoreCase(filterIdKios));
            rmi.setOnAction(e -> { filterIdKios = k.getIdKios(); terapkanFilterDanCari(); });
            menuKios.getItems().add(rmi);
        });
    }

    // 7. TABLE SETUP
    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewaan()));
        colKios.setCellValueFactory(d -> new SimpleStringProperty(labelKios(d.getValue().getIdKios())));
        colPenyewa.setCellValueFactory(d -> new SimpleStringProperty(labelPenyewa(d.getValue().getIdPenyewa())));
        colKaryawan.setCellValueFactory(d -> new SimpleStringProperty(labelKaryawan(d.getValue().getIdKaryawan())));
        colTglMulai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTglMulai() == null ? "" : d.getValue().getTglMulai().format(FMT_TGL)));
        colTglSelesai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTglSelesai() == null ? "" : d.getValue().getTglSelesai().format(FMT_TGL)));
        colTglTransaksi.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTglPenyewaan() == null ? "" : d.getValue().getTglPenyewaan().format(FMT_TGL)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStsPenyewaan()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle(styleBadgeStatus(status));
                setGraphic(badge);
                setText(null);
            }
        });

        // Kolom ID berperan sebagai link -> klik untuk buka dialog detail transaksi penyewaan ini.
        colId.setCellFactory(col -> buatSelDetail(p -> bukaDialogDetailPenyewaan(p)));

        // Kolom Penyewa / Kios / Karyawan sekarang teks biasa saja (tidak bisa diklik, tidak ada detail popup).
    }

    /** Membuat TableCell yang menampilkan teks sebagai link dan membuka detail saat diklik. */
    private TableCell<Penyewaan, String> buatSelDetail(java.util.function.Consumer<Penyewaan> aksiDetail) {
        return new TableCell<>() {
            private final Label lbl = new Label();
            {
                lbl.setStyle("-fx-text-fill:#1A3A8F;-fx-underline:true;-fx-cursor:hand;-fx-font-size:12px;");
                lbl.setOnMouseClicked(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        aksiDetail.accept((Penyewaan) getTableRow().getItem());
                    }
                    e.consume();
                });
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); setText(null); return; }
                lbl.setText(value);
                setGraphic(lbl);
                setText(null);
            }
        };
    }

    private String styleBadgeStatus(String status) {
        String base = "-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;";
        return switch (status == null ? "" : status) {
            case "Berlangsung" -> "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;" + base;
            case "Selesai"     -> "-fx-background-color:#EAEAEA;-fx-text-fill:#555555;" + base;
            case "Dibatalkan"  -> "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;" + base;
            default            -> "-fx-background-color:#FFF3D6;-fx-text-fill:#B8860B;" + base; // Menunggu
        };
    }

    // 8. LABEL JOIN (ID -> "ID - Nama/Deskripsi") DAN DETAIL POPUP
    private String labelPenyewa(String idPenyewa) {
        if (idPenyewa == null) return "";
        Penyewa p = petaPenyewa.get(idPenyewa);
        if (p == null || p.getNamaPenyewa() == null || p.getNamaPenyewa().isBlank()) return idPenyewa;
        return idPenyewa + " - " + p.getNamaPenyewa();
    }

    private String labelKios(String idKios) {
        if (idKios == null) return "";
        Kios k = petaKios.get(idKios);
        if (k == null || k.getDeskripsi() == null || k.getDeskripsi().isBlank()) return idKios;
        return idKios + " - " + k.getDeskripsi();
    }

    private String labelKaryawan(String idKaryawan) {
        if (idKaryawan == null) return "";
        Karyawan k = petaKaryawan.get(idKaryawan);
        if (k == null || k.getNamaKaryawan() == null || k.getNamaKaryawan().isBlank()) return idKaryawan;
        return idKaryawan + " - " + k.getNamaKaryawan();
    }

    /** Detail lengkap satu baris transaksi penyewaan, dipicu klik pada kolom ID -> buka dialog FXML terpisah. */
    private void bukaDialogDetailPenyewaan(Penyewaan p) {
        if (p == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/Penyewaan/PenyewaanDetailDialog.fxml"));
            Parent root = loader.load();

            PenyewaanDetailController controller = loader.getController();
            controller.setData(
                    p.getIdPenyewaan(),
                    p.getStsPenyewaan(),
                    labelKios(p.getIdKios()),
                    labelPenyewa(p.getIdPenyewa()),
                    labelKaryawan(p.getIdKaryawan()),
                    p.getTglMulai() == null ? "-" : p.getTglMulai().format(FMT_TGL),
                    p.getTglSelesai() == null ? "-" : p.getTglSelesai().format(FMT_TGL),
                    p.getTglPenyewaan() == null ? "-" : p.getTglPenyewaan().format(FMT_TGL)
            );

            Stage dialog = new Stage();
            dialog.setTitle("Detail Penyewaan " + p.getIdPenyewaan());
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tabelPenyewaan.getScene() != null) dialog.initOwner(tabelPenyewaan.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog detail penyewaan gagal dibuka. Silakan coba lagi.");
        }
    }

    private String nz(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // 9. LOAD DATA & PAGINATION
    private void loadData() {
        try {
            muatPetaMaster();
            populateFilterMenus();
            rawList = CRUD_Penyewaan.getAll();
            terapkanFilterDanCari();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Koneksi",
                    "Gagal memuat data penyewaan. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    /** Memuat peta master (Penyewa, Kios, Karyawan) sekali di awal untuk join tampilan & detail. */
    private void muatPetaMaster() {
        try {
            petaPenyewa = CRUD_Penyewa.getAll().stream()
                    .collect(Collectors.toMap(Penyewa::getIdPenyewa, x -> x, (a, b) -> a));
        } catch (Exception e) {
            petaPenyewa = Map.of();
        }
        try {
            petaKios = CRUD_Kios.getAll().stream()
                    .collect(Collectors.toMap(Kios::getIdKios, x -> x, (a, b) -> a));
        } catch (Exception e) {
            petaKios = Map.of();
        }
        try {
            // Asumsi: ada CRUD_Karyawan.getAll() sejenis dengan CRUD_Penyewa/CRUD_Kios.
            // Sesuaikan nama class/method ini kalau berbeda di project SIGAP kamu.
            petaKaryawan = CRUD_Karyawan.getAll().stream()
                    .collect(Collectors.toMap(Karyawan::getIdKaryawan, x -> x, (a, b) -> a));
        } catch (Exception e) {
            petaKaryawan = Map.of();
        }
    }

    private void refreshTable() {
        int total = masterList.size();
        totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage;
        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        tabelPenyewaan.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        tabelPenyewaan.refresh();
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtIdPenyewaan.setText(CRUD_Penyewaan.generateNextId());
        } catch (Exception e) {
            txtIdPenyewaan.setText("PY001");
        }
    }

    private void isiKaryawanLogin() {
        Karyawan karyawanLogin = Session.getLoggedInUser();
        if (karyawanLogin != null) {
            txtNamaKaryawan.setText(karyawanLogin.getNamaKaryawan() + " (" + karyawanLogin.getIdKaryawan() + ")");
        }
    }

    // 10. FORM STATE
    // Form Penyewaan hanya punya 2 mode: siap-input-baru, atau terkunci (baris terpilih ditampilkan read-only).
    // Tidak ada mode "edit" karena tidak ada operasi UPDATE untuk transaksi penyewaan.
    private void setFormState(boolean adaBarisTerpilih) {
        btnSimpan.setDisable(adaBarisTerpilih);
        dpTglMulai.setDisable(adaBarisTerpilih);
        dpTglSelesai.setDisable(adaBarisTerpilih);

        boolean bisaDibatalkan = adaBarisTerpilih
                && selectedPenyewaan != null
                && "Menunggu".equalsIgnoreCase(selectedPenyewaan.getStsPenyewaan());
        btnBatalkan.setDisable(!bisaDibatalkan);

        updateTombolPilih();
    }

    private void bersihForm() {
        txtPenyewaTerpilih.clear();
        txtKiosTerpilih.clear();
        dpTglMulai.setValue(null);
        dpTglSelesai.setValue(null);
        txtTglPenyewaan.setText(LocalDate.now().format(FMT_TGL));
        txtStatus.setText("Menunggu");
        penyewaTerpilih = null;
        kiosTerpilih = null;
        selectedPenyewaan = null;
    }

    // 11. VALIDASI
    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        if (dpTglMulai.getValue() == null) {
            sb.append("• Tanggal Mulai wajib diisi.\n");
        }
        if (dpTglSelesai.getValue() == null) {
            sb.append("• Tanggal Selesai wajib diisi.\n");
        }
        if (dpTglMulai.getValue() != null && dpTglSelesai.getValue() != null
                && dpTglSelesai.getValue().isBefore(dpTglMulai.getValue())) {
            sb.append("• Tanggal Selesai tidak boleh sebelum Tanggal Mulai.\n");
        }
        if (penyewaTerpilih == null) sb.append("• Penyewa wajib dipilih.\n");
        if (kiosTerpilih == null) sb.append("• Kios wajib dipilih.\n");

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return false;
        }
        return true;
    }

    // 12. UTILITAS
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (txtIdPenyewaan != null && txtIdPenyewaan.getScene() != null)
                alert.initOwner(txtIdPenyewaan.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    // 13. EVENT HANDLER — PILIH PENYEWA / KIOS (buka dialog modal)
    // Tanggal harus sudah diisi lebih dulu; dialog Pilih Kios menerima rentang tanggal
    // ini supaya hanya kios yang TERSEDIA (tidak bentrok jadwal) yang ditampilkan.
    @FXML
    void onPilihPenyewa(ActionEvent event) {
        if (dpTglMulai.getValue() == null || dpTglSelesai.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Tanggal Belum Diisi",
                    "Isi Tanggal Mulai dan Tanggal Selesai terlebih dahulu sebelum memilih penyewa.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/Penyewaan/PilihPenyewa.fxml"));
            Parent root = loader.load();

            PilihPenyewaController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Pilih Penyewa");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (txtPenyewaTerpilih.getScene() != null) dialog.initOwner(txtPenyewaTerpilih.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Penyewa hasil = controller.getPenyewaTerpilih();
            if (hasil != null) {
                penyewaTerpilih = hasil;
                txtPenyewaTerpilih.setText(hasil.getIdPenyewa() + " - " + hasil.getNamaPenyewa());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih penyewa gagal dibuka. Silakan coba lagi.");
        }
    }

    @FXML
    void onPilihKios(ActionEvent event) {
        if (dpTglMulai.getValue() == null || dpTglSelesai.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Tanggal Belum Diisi",
                    "Isi Tanggal Mulai dan Tanggal Selesai terlebih dahulu sebelum memilih kios.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/Penyewaan/PilihKios.fxml"));
            Parent root = loader.load();

            PilihKiosController controller = loader.getController();
            // Kirim rentang tanggal supaya dialog hanya menampilkan kios yang tersedia (tidak bentrok).
            controller.setRentangTanggal(dpTglMulai.getValue(), dpTglSelesai.getValue());

            Stage dialog = new Stage();
            dialog.setTitle("Pilih Kios");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (txtKiosTerpilih.getScene() != null) dialog.initOwner(txtKiosTerpilih.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Kios hasil = controller.getKiosTerpilih();
            if (hasil != null) {
                kiosTerpilih = hasil;
                txtKiosTerpilih.setText(hasil.getIdKios() + " - " + hasil.getDeskripsi());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih kios gagal dibuka. Silakan coba lagi.");
        }
    }

    // 14. EVENT HANDLER — SIMPAN (INSERT)
    @FXML
    void onSimpan(ActionEvent event) {
        if (!validasi()) return;

        Karyawan karyawanLogin = Session.getLoggedInUser();
        if (karyawanLogin == null) {
            showAlert(Alert.AlertType.ERROR, "Sesi Tidak Valid",
                    "Sesi login karyawan tidak ditemukan. Silakan login ulang.");
            return;
        }

        try {
            LocalDate hariIni = LocalDate.now();
            LocalDate tglMulai = dpTglMulai.getValue();
            LocalDate tglSelesai = dpTglSelesai.getValue();

            String statusAwal;
            if (tglMulai.isAfter(hariIni)) {
                statusAwal = "Menunggu";
            } else if (tglSelesai.isBefore(hariIni)) {
                statusAwal = "Selesai";
            } else {
                statusAwal = "Berlangsung";
            }

            Penyewaan p = new Penyewaan(
                    txtIdPenyewaan.getText().trim(),
                    karyawanLogin.getIdKaryawan(),
                    penyewaTerpilih.getIdPenyewa(),
                    kiosTerpilih.getIdKios(),
                    tglMulai,
                    tglSelesai,
                    hariIni,
                    statusAwal
            );

            CRUD_Penyewaan.insert(p);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Transaksi penyewaan berhasil disimpan.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan",
                    "Transaksi penyewaan gagal disimpan. Pastikan data yang dimasukkan valid, lalu coba lagi.");
        }
    }

    // 15. EVENT HANDLER — BATALKAN (soft-cancel, bukan hapus, bukan update biasa)
    @FXML
    void onBatalkanTransaksi(ActionEvent event) {
        if (selectedPenyewaan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih transaksi penyewaan yang ingin dibatalkan.");
            return;
        }
        if (!"Menunggu".equalsIgnoreCase(selectedPenyewaan.getStsPenyewaan())) {
            showAlert(Alert.AlertType.WARNING, "Tidak Dapat Dibatalkan",
                    "Hanya transaksi berstatus Menunggu yang dapat dibatalkan.");
            return;
        }

        String id = selectedPenyewaan.getIdPenyewaan();

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Pembatalan");
        konfirmasi.setHeaderText("Batalkan Transaksi Penyewaan");
        konfirmasi.setContentText("Transaksi [" + id + "] akan diubah statusnya menjadi Dibatalkan.\nLanjutkan?");
        if (txtIdPenyewaan.getScene() != null)
            konfirmasi.initOwner(txtIdPenyewaan.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_Penyewaan.batalkan(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Transaksi berhasil dibatalkan.");
                loadData();
                onBersih(null);
            } catch (Exception e) {                          // ⬅️ blok ini yang diganti
                e.printStackTrace();
                String pesanSql = e.getMessage();
                String pesanTampil = (pesanSql != null && !pesanSql.isBlank())
                        ? pesanSql
                        : "Transaksi gagal dibatalkan. Silakan coba lagi atau hubungi admin sistem.";
                showAlert(Alert.AlertType.ERROR, "Gagal Membatalkan", pesanTampil);
            }
        }
    }

    @FXML
    void onBersih(ActionEvent event) {
        bersihForm();
        setFormState(false);
        tabelPenyewaan.getSelectionModel().clearSelection();
        autoGenerateId();
        isiKaryawanLogin();
    }

    // 16. EVENT HANDLER — KLIK BARIS TABEL (tampilkan detail, siapkan aksi Batalkan)
    @FXML
    void onTableClick(MouseEvent event) {
        Penyewaan p = tabelPenyewaan.getSelectionModel().getSelectedItem();
        if (p == null) return;

        selectedPenyewaan = p;

        txtIdPenyewaan.setText(p.getIdPenyewaan());
        txtNamaKaryawan.setText(labelKaryawan(p.getIdKaryawan()));
        txtPenyewaTerpilih.setText(labelPenyewa(p.getIdPenyewa()));
        txtKiosTerpilih.setText(labelKios(p.getIdKios()));
        dpTglMulai.setValue(p.getTglMulai());
        dpTglSelesai.setValue(p.getTglSelesai());
        txtTglPenyewaan.setText(p.getTglPenyewaan() == null ? "" : p.getTglPenyewaan().format(FMT_TGL));
        txtStatus.setText(p.getStsPenyewaan());

        setFormState(true);
    }

    // 17. EVENT HANDLER — PENCARIAN & FILTER
    @FXML
    void onCari(ActionEvent event) {
        terapkanFilterDanCari();
    }

    @FXML
    void onFilterStatus(ActionEvent event) {
        filterStatus = rmStatusMenunggu.isSelected() ? "Menunggu"
                : rmStatusBerlangsung.isSelected() ? "Berlangsung"
                : rmStatusSelesai.isSelected() ? "Selesai"
                : rmStatusDibatalkan.isSelected() ? "Dibatalkan" : null;
        terapkanFilterDanCari();
    }

    /** Reset semua filter (Penyewa, Kios, Status) sekaligus, dipicu item "Reset Filter" di MenuButton. */
    @FXML
    void onResetFilter(ActionEvent event) {
        filterIdPenyewa = null;
        filterIdKios = null;
        filterStatus = null;
        rmStatusMenunggu.setSelected(false);
        rmStatusBerlangsung.setSelected(false);
        rmStatusSelesai.setSelected(false);
        rmStatusDibatalkan.setSelected(false);
        populateFilterMenus();
        terapkanFilterDanCari();
    }

    /** Menerapkan kata kunci pencarian + kriteria filter ke rawList, lalu refresh tabel. */
    private void terapkanFilterDanCari() {
        String kw = txtCari.getText() == null ? "" : txtCari.getText().trim().toLowerCase();

        List<Penyewaan> hasil = rawList.stream()
                .filter(p -> filterIdPenyewa == null || filterIdPenyewa.equalsIgnoreCase(p.getIdPenyewa()))
                .filter(p -> filterIdKios == null || filterIdKios.equalsIgnoreCase(p.getIdKios()))
                .filter(p -> filterStatus == null || filterStatus.equalsIgnoreCase(p.getStsPenyewaan()))
                .filter(p -> kw.isEmpty() || cocokKeyword(p, kw))
                .collect(Collectors.toList());

        masterList.setAll(hasil);
        currentPage = 1;
        refreshTable();
    }

    /** Cocokkan kata kunci ke ID Penyewaan, ID/Nama Kios, ID/Nama Penyewa, ID/Nama Karyawan, dan Status. */
    private boolean cocokKeyword(Penyewaan p, String kwLower) {
        if (mengandung(p.getIdPenyewaan(), kwLower)) return true;
        if (mengandung(labelKios(p.getIdKios()), kwLower)) return true;
        if (mengandung(labelPenyewa(p.getIdPenyewa()), kwLower)) return true;
        if (mengandung(labelKaryawan(p.getIdKaryawan()), kwLower)) return true;
        return mengandung(p.getStsPenyewaan(), kwLower);
    }

    private boolean mengandung(String value, String kwLower) {
        return value != null && value.toLowerCase().contains(kwLower);
    }

    // 18. EVENT HANDLER — PAGINATION
    @FXML
    void onFirstPage(ActionEvent event) {
        currentPage = 1;
        refreshTable();
    }

    @FXML
    void onLastPage(ActionEvent event) {
        currentPage = totalPage;
        refreshTable();
    }

    @FXML
    void onNextPage(ActionEvent event) {
        if (currentPage < totalPage) { currentPage++; refreshTable(); }
    }

    @FXML
    void onPrevPage(ActionEvent event) {
        if (currentPage > 1) { currentPage--; refreshTable(); }
    }
}