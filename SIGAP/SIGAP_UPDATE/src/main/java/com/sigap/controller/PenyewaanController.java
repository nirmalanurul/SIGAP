package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.ADT.Kios;
import com.sigap.ADT.Penyewa;
import com.sigap.ADT.Penyewaan;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    // 2. FXML FIELDS — PENCARIAN & TABLE
    @FXML
    private TextField txtCari;
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
    private final ObservableList<Penyewaan> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    // Data terpilih dari dialog picker (Id, bukan cuma tampilan teks)
    private Penyewa penyewaTerpilih = null;
    private Kios kiosTerpilih = null;

    // Baris yang sedang dipilih di tabel (untuk aksi Batalkan)
    private Penyewaan selectedPenyewaan = null;

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
    private void setupDatePickers() {
        // Tgl Mulai tidak boleh sebelum hari ini
        dpTglMulai.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Tgl Selesai tidak boleh sebelum Tgl Mulai yang sudah dipilih
        dpTglSelesai.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate batasAwal = dpTglMulai.getValue() != null ? dpTglMulai.getValue() : LocalDate.now();
                setDisable(empty || date.isBefore(batasAwal));
            }
        });

        // Kalau Tgl Mulai berubah dan Tgl Selesai jadi tidak valid, kosongkan Tgl Selesai
        dpTglMulai.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpTglSelesai.getValue() != null && dpTglSelesai.getValue().isBefore(newVal)) {
                dpTglSelesai.setValue(null);
            }
        });
    }

    // 7. TABLE SETUP
    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewaan()));
        colKios.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdKios()));
        colPenyewa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewa()));
        colKaryawan.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdKaryawan()));
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

        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); setStyle(""); return; }
                setText(id);
                setStyle("-fx-text-fill:#1A3A8F;-fx-font-weight:600;");
            }
        });
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

    // 8. LOAD DATA & PAGINATION
    private void loadData() {
        try {
            List<Penyewaan> list = CRUD_Penyewaan.getAll();
            masterList.setAll(list);
            currentPage = 1;
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Koneksi",
                    "Gagal memuat data penyewaan. Periksa koneksi ke database atau hubungi admin sistem.");
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

    // 9. FORM STATE
    // Form Penyewaan hanya punya 2 mode: siap-input-baru, atau terkunci (baris terpilih ditampilkan read-only).
    // Tidak ada mode "edit" karena tidak ada operasi UPDATE untuk transaksi penyewaan.
    private void setFormState(boolean adaBarisTerpilih) {
        btnSimpan.setDisable(adaBarisTerpilih);
        btnPilihPenyewa.setDisable(adaBarisTerpilih);
        btnPilihKios.setDisable(adaBarisTerpilih);
        dpTglMulai.setDisable(adaBarisTerpilih);
        dpTglSelesai.setDisable(adaBarisTerpilih);

        boolean bisaDibatalkan = adaBarisTerpilih
                && selectedPenyewaan != null
                && "Menunggu".equalsIgnoreCase(selectedPenyewaan.getStsPenyewaan());
        btnBatalkan.setDisable(!bisaDibatalkan);
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

    // 10. VALIDASI
    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        if (penyewaTerpilih == null) sb.append("• Penyewa wajib dipilih.\n");
        if (kiosTerpilih == null) sb.append("• Kios wajib dipilih.\n");

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

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return false;
        }
        return true;
    }

    // 11. UTILITAS
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

    // 12. EVENT HANDLER — PILIH PENYEWA / KIOS (buka dialog modal)
    @FXML
    void onPilihPenyewa(ActionEvent event) {
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/Penyewaan/PilihKios.fxml"));
            Parent root = loader.load();

            PilihKiosController controller = loader.getController();

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

    // 13. EVENT HANDLER — SIMPAN (INSERT)
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

            // Status awal dihitung di sisi Java hanya untuk ditampilkan sebelum simpan;
            // nilai final yang benar-benar tersimpan tetap dihitung ulang oleh spInsertPenyewaan
            // di database (sumber kebenaran ada di server, bukan di klien).
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
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan",
                    "Transaksi penyewaan gagal disimpan. Pastikan data yang dimasukkan valid, lalu coba lagi.");
        }
    }

    // 14. EVENT HANDLER — BATALKAN (soft-cancel, bukan hapus, bukan update biasa)
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
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal Membatalkan",
                        "Transaksi gagal dibatalkan. Silakan coba lagi atau hubungi admin sistem.");
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

    // 15. EVENT HANDLER — KLIK BARIS TABEL (tampilkan detail, siapkan aksi Batalkan)
    @FXML
    void onTableClick(MouseEvent event) {
        Penyewaan p = tabelPenyewaan.getSelectionModel().getSelectedItem();
        if (p == null) return;

        selectedPenyewaan = p;

        txtIdPenyewaan.setText(p.getIdPenyewaan());
        txtNamaKaryawan.setText(p.getIdKaryawan());
        txtPenyewaTerpilih.setText(p.getIdPenyewa());
        txtKiosTerpilih.setText(p.getIdKios());
        dpTglMulai.setValue(p.getTglMulai());
        dpTglSelesai.setValue(p.getTglSelesai());
        txtTglPenyewaan.setText(p.getTglPenyewaan() == null ? "" : p.getTglPenyewaan().format(FMT_TGL));
        txtStatus.setText(p.getStsPenyewaan());

        setFormState(true);
    }

    // 16. EVENT HANDLER — PENCARIAN
    @FXML
    void onCari(ActionEvent event) {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Penyewaan> hasil = CRUD_Penyewaan.search(kw);
            masterList.setAll(hasil);
            currentPage = 1;
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Cari", "Error: " + e.getMessage());
        }
    }

    // 17. EVENT HANDLER — PAGINATION
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