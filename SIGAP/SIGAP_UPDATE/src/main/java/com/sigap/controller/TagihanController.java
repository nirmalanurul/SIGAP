package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.ADT.Penyewa;
import com.sigap.ADT.Penyewaan;
import com.sigap.ADT.TagihanPembayaranSewa;
import com.sigap.APP.CRUD_TagihanPembayaranSewa;
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
import javafx.scene.control.ComboBox;
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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class TagihanController implements Initializable {

    // 1. FXML FIELDS — FORM INPUT
    @FXML
    private TextField txtIdTagihan;
    @FXML
    private TextField txtNamaKaryawan;
    @FXML
    private TextField txtPenyewaanTerpilih;
    @FXML
    private ComboBox<String> cbMetodeBayar;
    @FXML
    private Label lblInfoSewa;
    @FXML
    private DatePicker dpTglJatuhTempo;
    @FXML
    private TextField txtTglBayar;
    @FXML
    private TextField txtTotalDibayarAwal;
    @FXML
    private TextField txtStatus;
    @FXML
    private TextField txtTotalTagihan;

    @FXML
    private Button btnPilihPenyewaan;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnBatalkan;

    // 2. FXML FIELDS — PANEL TAMBAH PEMBAYARAN
    @FXML
    private TextField txtSudahDibayar;
    @FXML
    private TextField txtNominalBayar;
    @FXML
    private Button btnBayar;

    // 3. FXML FIELDS — PENCARIAN & TABLE
    @FXML
    private TextField txtCari;
    @FXML
    private TableView<TagihanPembayaranSewa> tabelTagihan;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colId;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colPenyewaan;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colKaryawan;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colTglBayar;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colTglTempo;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colTotalTagihan;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colTotalDibayar;
    @FXML
    private TableColumn<TagihanPembayaranSewa, String> colStatus;

    // 4. FXML FIELDS — PAGINATION
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;

    // 5. STATE
    private final ObservableList<TagihanPembayaranSewa> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    // Data terpilih dari dialog picker
    private Penyewaan penyewaanTerpilih = null;

    // Baris yang sedang dipilih di tabel (untuk aksi Bayar/Batalkan)
    private TagihanPembayaranSewa selectedTagihan = null;

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final NumberFormat FMT_RUPIAH = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;-fx-text-fill:#888;";

    // 6. INITIALIZE
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdTagihan.setEditable(false);
        txtNamaKaryawan.setEditable(false);
        txtPenyewaanTerpilih.setEditable(false);
        txtTglBayar.setEditable(false);
        txtStatus.setEditable(false);
        txtStatus.setStyle(STYLE_READONLY);
        txtTotalTagihan.setEditable(false);
        txtTotalTagihan.setStyle(STYLE_READONLY);
        txtSudahDibayar.setEditable(false);

        cbMetodeBayar.setItems(FXCollections.observableArrayList(
                "Tunai", "Transfer Bank", "QRIS", "Kartu Debit", "Kartu Kredit"));

        setupTable();
        setupDatePicker();
        setFormState(false);

        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
            isiKaryawanLogin();
            txtTglBayar.setText(LocalDate.now().format(FMT_TGL));
            txtStatus.setText("Belum Lunas");
        });
    }

    // 7. DATE PICKER — jatuh tempo tidak boleh sebelum hari ini
    private void setupDatePicker() {
        dpTglJatuhTempo.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    // 8. TABLE SETUP
    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdTagihanPembayaran()));
        colPenyewaan.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewaan()));
        colKaryawan.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdKaryawan()));
        colTglBayar.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTglBayar() == null ? "" : d.getValue().getTglBayar().format(FMT_TGL)));
        colTglTempo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTglJatuhTempo() == null ? "" : d.getValue().getTglJatuhTempo().format(FMT_TGL)));
        colTotalTagihan.setCellValueFactory(d -> new SimpleStringProperty(
                "Rp " + FMT_RUPIAH.format((long) d.getValue().getTotalTagihan())));
        colTotalDibayar.setCellValueFactory(d -> new SimpleStringProperty(
                "Rp " + FMT_RUPIAH.format((long) d.getValue().getTotalDibayar())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStsTagihanPembayaran()));

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
            case "Lunas"      -> "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;" + base;
            case "Terlambat"  -> "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;" + base;
            case "Dibatalkan" -> "-fx-background-color:#EAEAEA;-fx-text-fill:#555555;" + base;
            default           -> "-fx-background-color:#FFF3D6;-fx-text-fill:#B8860B;" + base; // Belum Lunas
        };
    }

    // 9. LOAD DATA & PAGINATION
    private void loadData() {
        try {
            List<TagihanPembayaranSewa> list = CRUD_TagihanPembayaranSewa.getAll();
            masterList.setAll(list);
            currentPage = 1;
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Koneksi",
                    "Gagal memuat data tagihan. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    private void refreshTable() {
        int total = masterList.size();
        totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage;
        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        tabelTagihan.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        tabelTagihan.refresh();
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtIdTagihan.setText(CRUD_TagihanPembayaranSewa.generateNextId());
        } catch (Exception e) {
            txtIdTagihan.setText("TG001");
        }
    }

    private void isiKaryawanLogin() {
        Karyawan karyawanLogin = Session.getLoggedInUser();
        if (karyawanLogin != null) {
            txtNamaKaryawan.setText(karyawanLogin.getNamaKaryawan() + " (" + karyawanLogin.getIdKaryawan() + ")");
        }
    }

    // 10. FORM STATE
    // Form Tagihan hanya punya 2 mode: siap-input-baru, atau terkunci (baris terpilih ditampilkan read-only).
    // Tidak ada mode "edit" karena tidak ada operasi UPDATE bebas untuk data inti tagihan.
    private void setFormState(boolean adaBarisTerpilih) {
        btnSimpan.setDisable(adaBarisTerpilih);
        btnPilihPenyewaan.setDisable(adaBarisTerpilih);
        cbMetodeBayar.setDisable(adaBarisTerpilih);
        dpTglJatuhTempo.setDisable(adaBarisTerpilih);
        txtTotalDibayarAwal.setDisable(adaBarisTerpilih);

        boolean bisaDibatalkan = adaBarisTerpilih
                && selectedTagihan != null
                && ("Belum Lunas".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran())
                || "Terlambat".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran()))
                && selectedTagihan.getTotalDibayar() <= 0;
        btnBatalkan.setDisable(!bisaDibatalkan);

        boolean bisaDibayar = adaBarisTerpilih
                && selectedTagihan != null
                && ("Belum Lunas".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran())
                || "Terlambat".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran()));
        btnBayar.setDisable(!bisaDibayar);
        txtNominalBayar.setDisable(!bisaDibayar);
    }

    private void bersihForm() {
        txtPenyewaanTerpilih.clear();
        cbMetodeBayar.setValue(null);
        dpTglJatuhTempo.setValue(null);
        txtTglBayar.setText(LocalDate.now().format(FMT_TGL));
        txtTotalDibayarAwal.clear();
        txtStatus.setText("Belum Lunas");
        txtTotalTagihan.clear();
        lblInfoSewa.setText("-");
        txtSudahDibayar.clear();
        txtNominalBayar.clear();
        penyewaanTerpilih = null;
        selectedTagihan = null;
    }

    // 11. VALIDASI
    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        if (penyewaanTerpilih == null) sb.append("• Penyewaan wajib dipilih.\n");
        if (cbMetodeBayar.getValue() == null) sb.append("• Metode bayar wajib dipilih.\n");
        if (dpTglJatuhTempo.getValue() == null) sb.append("• Tanggal jatuh tempo wajib diisi.\n");

        String dpAwalText = txtTotalDibayarAwal.getText() == null ? "" : txtTotalDibayarAwal.getText().trim();
        if (!dpAwalText.isEmpty()) {
            try {
                double nilai = Double.parseDouble(dpAwalText);
                if (nilai < 0) sb.append("• Nominal dibayar di awal tidak boleh negatif.\n");
            } catch (NumberFormatException e) {
                sb.append("• Nominal dibayar di awal harus berupa angka.\n");
            }
        }

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
            if (txtIdTagihan != null && txtIdTagihan.getScene() != null)
                alert.initOwner(txtIdTagihan.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    // 13. EVENT HANDLER — PILIH PENYEWAAN (buka dialog modal)
    @FXML
    void onPilihPenyewaan(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/Tagihan Pembayaran/PilihPenyewaan.fxml"));
            Parent root = loader.load();

            PilihPenyewaanController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Pilih Penyewaan");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (txtPenyewaanTerpilih.getScene() != null) dialog.initOwner(txtPenyewaanTerpilih.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Penyewaan hasil = controller.getPenyewaanTerpilih();
            if (hasil != null) {
                penyewaanTerpilih = hasil;
                txtPenyewaanTerpilih.setText(hasil.getIdPenyewaan() + " - Kios " + hasil.getIdKios());
                lblInfoSewa.setText("Kios: " + hasil.getIdKios()
                        + "  |  Periode: " + hasil.getTglMulai().format(FMT_TGL)
                        + " s/d " + hasil.getTglSelesai().format(FMT_TGL)
                        + "  |  Status Sewa: " + hasil.getStsPenyewaan());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih penyewaan gagal dibuka. Silakan coba lagi.");
        }
    }

//    @FXML
//    void onPilihPenyewa(ActionEvent event) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/Penyewaan/PilihPenyewa.fxml"));
//            Parent root = loader.load();
//
//            PilihPenyewaController controller = loader.getController();
//
//            Stage dialog = new Stage();
//            dialog.setTitle("Pilih Penyewa");
//            dialog.initModality(Modality.APPLICATION_MODAL);
//            if (txtPenyewaTerpilih.getScene() != null) dialog.initOwner(txtPenyewaTerpilih.getScene().getWindow());
//            dialog.setScene(new Scene(root));
//            dialog.showAndWait();
//
//            Penyewa hasil = controller.getPenyewaTerpilih();
//            if (hasil != null) {
//                penyewaTerpilih = hasil;
//                txtPenyewaTerpilih.setText(hasil.getIdPenyewa() + " - " + hasil.getNamaPenyewa());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
//                    "Dialog pilih penyewa gagal dibuka. Silakan coba lagi.");
//        }
//    }

    // 14. EVENT HANDLER — SIMPAN (INSERT)
    // Total_Biaya_Sewa dan Total_Tagihan TIDAK dihitung di sini — nilai final dihitung ulang
    // oleh spInsertTagihanPembayaran di database (sumber kebenaran ada di server, bukan di klien).
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
            String dpAwalText = txtTotalDibayarAwal.getText() == null ? "" : txtTotalDibayarAwal.getText().trim();
            double dibayarAwal = dpAwalText.isEmpty() ? 0 : Double.parseDouble(dpAwalText);

            TagihanPembayaranSewa t = new TagihanPembayaranSewa(
                    txtIdTagihan.getText().trim(),
                    penyewaanTerpilih.getIdPenyewaan(),
                    karyawanLogin.getIdKaryawan(),
                    LocalDate.now(),
                    dpTglJatuhTempo.getValue(),
                    0, 0, 0,
                    dibayarAwal,
                    cbMetodeBayar.getValue(),
                    "Belum Lunas"
            );

            CRUD_TagihanPembayaranSewa.insert(t);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Tagihan pembayaran sewa berhasil disimpan.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan",
                    "Tagihan gagal disimpan. Pastikan data yang dimasukkan valid, lalu coba lagi.");
        }
    }

    // 15. EVENT HANDLER — BAYAR (menambah Total_Dibayar untuk tagihan yang dipilih)
    @FXML
    void onBayar(ActionEvent event) {
        if (selectedTagihan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tagihan yang ingin dibayar.");
            return;
        }
        if ("Lunas".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran())
                || "Dibatalkan".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran())) {
            showAlert(Alert.AlertType.WARNING, "Tidak Dapat Dibayar",
                    "Tagihan berstatus Lunas atau Dibatalkan tidak dapat dibayar lagi.");
            return;
        }

        String nominalText = txtNominalBayar.getText() == null ? "" : txtNominalBayar.getText().trim();
        double nominal;
        try {
            nominal = Double.parseDouble(nominalText);
            if (nominal <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validasi Input", "Nominal bayar harus lebih dari 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", "Nominal bayar harus berupa angka.");
            return;
        }

        String metode = cbMetodeBayar.getValue() != null ? cbMetodeBayar.getValue() : selectedTagihan.getMetodeBayar();
        String id = selectedTagihan.getIdTagihanPembayaran();

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Pembayaran");
        konfirmasi.setHeaderText("Tambah Pembayaran Tagihan");
        konfirmasi.setContentText("Tambah pembayaran Rp " + FMT_RUPIAH.format((long) nominal)
                + " untuk tagihan [" + id + "]?\nLanjutkan?");
        if (txtIdTagihan.getScene() != null)
            konfirmasi.initOwner(txtIdTagihan.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_TagihanPembayaranSewa.bayar(id, nominal, metode);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pembayaran berhasil dicatat.");
                loadData();
                onBersih(null);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal Membayar",
                        "Pembayaran gagal dicatat. Pastikan nominal tidak melebihi sisa tagihan, lalu coba lagi.");
            }
        }
    }

    // 16. EVENT HANDLER — BATALKAN (soft-cancel, bukan hapus, bukan update biasa)
    @FXML
    void onBatalkanTransaksi(ActionEvent event) {
        if (selectedTagihan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tagihan yang ingin dibatalkan.");
            return;
        }
        boolean statusBisaDibatalkan = "Belum Lunas".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran())
                || "Terlambat".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran());
        if (!statusBisaDibatalkan || selectedTagihan.getTotalDibayar() > 0) {
            showAlert(Alert.AlertType.WARNING, "Tidak Dapat Dibatalkan",
                    "Hanya tagihan yang belum menerima pembayaran sama sekali yang dapat dibatalkan.");
            return;
        }

        String id = selectedTagihan.getIdTagihanPembayaran();

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Pembatalan");
        konfirmasi.setHeaderText("Batalkan Tagihan Pembayaran");
        konfirmasi.setContentText("Tagihan [" + id + "] akan diubah statusnya menjadi Dibatalkan.\nLanjutkan?");
        if (txtIdTagihan.getScene() != null)
            konfirmasi.initOwner(txtIdTagihan.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_TagihanPembayaranSewa.batalkan(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Tagihan berhasil dibatalkan.");
                loadData();
                onBersih(null);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal Membatalkan",
                        "Tagihan gagal dibatalkan. Silakan coba lagi atau hubungi admin sistem.");
            }
        }
    }

    @FXML
    void onBersih(ActionEvent event) {
        bersihForm();
        setFormState(false);
        tabelTagihan.getSelectionModel().clearSelection();
        autoGenerateId();
        isiKaryawanLogin();
    }

    // 17. EVENT HANDLER — KLIK BARIS TABEL (tampilkan detail, siapkan aksi Bayar/Batalkan)
    @FXML
    void onTableClick(MouseEvent event) {
        TagihanPembayaranSewa t = tabelTagihan.getSelectionModel().getSelectedItem();
        if (t == null) return;

        selectedTagihan = t;
        penyewaanTerpilih = null;

        txtIdTagihan.setText(t.getIdTagihanPembayaran());
        txtNamaKaryawan.setText(t.getIdKaryawan());
        txtPenyewaanTerpilih.setText(t.getIdPenyewaan());
        cbMetodeBayar.setValue(t.getMetodeBayar());
        dpTglJatuhTempo.setValue(t.getTglJatuhTempo());
        txtTglBayar.setText(t.getTglBayar() == null ? "" : t.getTglBayar().format(FMT_TGL));
        txtStatus.setText(t.getStsTagihanPembayaran());
        txtTotalTagihan.setText(FMT_RUPIAH.format((long) t.getTotalTagihan()));
        txtSudahDibayar.setText(FMT_RUPIAH.format((long) t.getTotalDibayar()));
        lblInfoSewa.setText("Penyewaan: " + t.getIdPenyewaan());
        txtNominalBayar.clear();

        setFormState(true);
    }

    // 18. EVENT HANDLER — PENCARIAN
    @FXML
    void onCari(ActionEvent event) {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<TagihanPembayaranSewa> hasil = CRUD_TagihanPembayaranSewa.search(kw);
            masterList.setAll(hasil);
            currentPage = 1;
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Cari", "Error: " + e.getMessage());
        }
    }

    // 19. EVENT HANDLER — PAGINATION
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