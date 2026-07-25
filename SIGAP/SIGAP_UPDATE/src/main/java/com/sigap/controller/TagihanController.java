package com.sigap.controller;

import com.sigap.ADT.BiayaTambahan;
import com.sigap.ADT.DetailTagihanBiaya;
import com.sigap.ADT.Karyawan;
import com.sigap.ADT.Kios;
import com.sigap.ADT.Penyewa;
import com.sigap.ADT.Penyewaan;
import com.sigap.ADT.TagihanPembayaranSewa;
import com.sigap.APP.CRUD_BiayaTambahan;
import com.sigap.APP.CRUD_DetailTagihanBiaya;
import com.sigap.APP.CRUD_Kios;
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
    private TextField txtTglJatuhTempo;
    @FXML
    private TextField txtTglBayar;
    @FXML
    private TextField txtTotalDibayarAwal;
    @FXML
    private TextField txtStatus;
    @FXML
    private TextField txtTotalTagihan;
    @FXML
    private TextField txtTotalBiayaSewa;

    @FXML
    private Button btnPilihPenyewaan;
    @FXML
    private Button btnSimpan;

    // 2b. FXML FIELDS — PANEL BIAYA TAMBAHAN (Biaya_Tambahan / Detail_Tagihan_Biaya)
    @FXML
    private Button btnPilihBiayaTambahan;
    @FXML
    private TableView<DetailTagihanBiaya> tabelBiayaTambahan;
    @FXML
    private TableColumn<DetailTagihanBiaya, String> colJenisBiayaTambahan;
    @FXML
    private TableColumn<DetailTagihanBiaya, String> colJumlahHariBiaya;
    @FXML
    private TableColumn<DetailTagihanBiaya, String> colSubtotalBiaya;
    @FXML
    private Label lblTotalBiayaTambahan;
    @FXML
    private TextField txtTotalBiayaTambahan;

    // 2. FXML FIELDS — PANEL TAMBAH PEMBAYARAN (kini menyatu di sel grid "Total Dibayar")
    @FXML
    private TextField txtSudahDibayar;
    @FXML
    private TextField txtNominalBayar;
    @FXML
    private Button btnOtomatis;
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
    // Harga_Kios dari Kios milik penyewaanTerpilih -- diambil sekali pas
    // Penyewaan dipilih, dipakai HANYA untuk preview Total Tagihan di klien
    // (Rp 0 kalau belum ada penyewaan terpilih). Nilai final tetap dihitung
    // ulang di server oleh spInsertTagihanPembayaran; ini bukan sumber
    // kebenaran, cuma biar kasir bisa lihat estimasi sebelum klik Simpan.
    private double hargaSewaTerpilih = 0;

    // Nilai LocalDate jatuh tempo yang sedang aktif di form (dari Pilih Penyewaan
    // -> Pilih Bulan Tagihan, atau dari baris tagihan tersimpan yang diklik).
    // txtTglJatuhTempo hanya menampilkan teksnya (readonly); ini sumber kebenarannya
    // di sisi klien -- dulu disimpan langsung oleh dpTglJatuhTempo.getValue().
    private LocalDate tglJatuhTempoTerpilih = null;

    // Baris yang sedang dipilih di tabel (untuk aksi Bayar/Batalkan)
    private TagihanPembayaranSewa selectedTagihan = null;

    // Daftar Detail_Tagihan_Biaya yang sedang disiapkan untuk tagihan baru
    // (atau daftar existing kalau sedang melihat tagihan yang sudah tersimpan).
    private final ObservableList<DetailTagihanBiaya> daftarBiayaTambahan = FXCollections.observableArrayList();
    // Master data Biaya_Tambahan, dimuat sekali untuk keperluan lookup nama Jenis
    // di tabel mini pada form ini (Detail_Tagihan_Biaya sendiri cuma simpan Id).
    private List<BiayaTambahan> masterBiayaTambahan = List.of();

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
        txtTotalBiayaSewa.setEditable(false);
        txtTotalBiayaSewa.setStyle(STYLE_READONLY);
        txtTotalBiayaTambahan.setEditable(false);
        txtTotalBiayaTambahan.setStyle(STYLE_READONLY);
        txtSudahDibayar.setEditable(false);
        // Tgl. Jatuh Tempo sekarang selalu otomatis dari slot bulan yang dipilih
        // lewat dialog Pilih Penyewaan -> Pilih Bulan Tagihan, tidak diketik manual lagi.
        txtTglJatuhTempo.setEditable(false);
        txtTglJatuhTempo.setStyle(STYLE_READONLY);

        cbMetodeBayar.setItems(FXCollections.observableArrayList(
                "Tunai", "Transfer Bank", "Kartu Debit"));

        setupTable();
        setupTabelBiayaTambahan();
        setupNominalBayarFormatter();
        setFormState(false);

        Platform.runLater(() -> {
            loadData();
            muatMasterBiayaTambahan();
            autoGenerateId();
            isiKaryawanLogin();
            txtTglBayar.setText(LocalDate.now().format(FMT_TGL));
            txtStatus.setText("Belum Lunas");
        });
    }

    // 8b. TABEL MINI — BIAYA TAMBAHAN (Detail_Tagihan_Biaya untuk tagihan ini)
    private void setupTabelBiayaTambahan() {
        colJenisBiayaTambahan.setCellValueFactory(d -> new SimpleStringProperty(
                cariJenisBiaya(d.getValue().getIdBiayaTambahan())));
        colJumlahHariBiaya.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getJumlahHari())));
        colSubtotalBiaya.setCellValueFactory(d -> new SimpleStringProperty(
                "Rp " + FMT_RUPIAH.format((long) d.getValue().getSubTotal())));
        tabelBiayaTambahan.setItems(daftarBiayaTambahan);
    }

    private void muatMasterBiayaTambahan() {
        try {
            // SESUAIKAN: nama method di CRUD_BiayaTambahan kalau berbeda.
            masterBiayaTambahan = CRUD_BiayaTambahan.getAll();
        } catch (Exception e) {
            masterBiayaTambahan = List.of();
        }
    }

    private String cariJenisBiaya(String idBiayaTambahan) {
        return masterBiayaTambahan.stream()
                .filter(b -> b.getIdBiayaTambahan().equals(idBiayaTambahan))
                .map(BiayaTambahan::getJenisBiayaTambahan)
                .findFirst()
                .orElse(idBiayaTambahan);
    }

    private void refreshTabelBiayaTambahan() {
        tabelBiayaTambahan.refresh();
        double total = daftarBiayaTambahan.stream().mapToDouble(DetailTagihanBiaya::getSubTotal).sum();
        lblTotalBiayaTambahan.setText("Rp " + FMT_RUPIAH.format((long) total));
        txtTotalBiayaTambahan.setText(FMT_RUPIAH.format((long) total));
    }

    /**
     * Preview Total Tagihan = Harga_Kios (hargaSewaTerpilih) + jumlah Sub_total
     * semua biaya tambahan yang sudah ditambahkan. HANYA dipakai untuk tampilan
     * di form tagihan BARU (belum Simpan) -- untuk tagihan yang sudah tersimpan
     * (onTableClick), txtTotalTagihan tetap diisi dari t.getTotalTagihan() yang
     * merupakan nilai final dari server, bukan hasil method ini.
     */
    private void refreshPreviewTotalTagihan() {
        double totalBiayaTambahan = daftarBiayaTambahan.stream().mapToDouble(DetailTagihanBiaya::getSubTotal).sum();
        double preview = hargaSewaTerpilih + totalBiayaTambahan;
        txtTotalTagihan.setText(FMT_RUPIAH.format((long) preview));
        txtTotalBiayaSewa.setText(FMT_RUPIAH.format((long) hargaSewaTerpilih));
    }

    // 7b. FORMAT OTOMATIS — nominal bayar diketik manual otomatis dapat pemisah
    // ribuan (mis. "50000" -> "50.000") supaya sama enaknya dibaca dengan hasil
    // tombol Otomatis. Pakai flag sedangMemformat supaya setText() di dalam
    // listener tidak memicu listener ini lagi (infinite loop).
    private boolean sedangMemformatNominal = false;

    private void setupNominalBayarFormatter() {
        txtNominalBayar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (sedangMemformatNominal) return;

            String digitsOnly = newVal == null ? "" : newVal.replaceAll("[^0-9]", "");
            String formatted = digitsOnly.isEmpty() ? "" : FMT_RUPIAH.format(Long.parseLong(digitsOnly));

            if (!formatted.equals(newVal)) {
                sedangMemformatNominal = true;
                txtNominalBayar.setText(formatted);
                txtNominalBayar.positionCaret(formatted.length());
                sedangMemformatNominal = false;
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
        // Biaya tambahan boleh disusun bebas untuk tagihan BARU (belum Simpan).
        // Untuk tagihan yang SUDAH tersimpan, tetap boleh ditambah/dikurangi
        // SELAMA statusnya masih "Belum Lunas" -- begitu Lunas/Terlambat/
        // Dibatalkan, dikunci total (lihat onPilihBiayaTambahan() untuk alur
        // insert/delete langsung ke DB pada mode tagihan tersimpan).
        boolean bolehUbahBiayaTambahan = !adaBarisTerpilih
                || (selectedTagihan != null && "Belum Lunas".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran()));
        btnPilihBiayaTambahan.setDisable(!bolehUbahBiayaTambahan);
        cbMetodeBayar.setDisable(adaBarisTerpilih);
        // Tgl. Jatuh Tempo tidak lagi diketik manual — nilainya selalu datang dari
        // slot bulan yang dipilih di dialog Pilih Penyewaan -> Pilih Bulan Tagihan.
        // txtTglJatuhTempo sudah readonly permanen (editable=false), tidak perlu disable lagi.

        // Dibayar di Awal (DP) hanya boleh diisi kalau penyewaan yang dipilih
        // berstatus "Menunggu". Probis tidak mengizinkan cicilan/DP untuk
        // penyewaan yang sudah Berlangsung/Selesai/Dibatalkan.
        boolean bolehDp = !adaBarisTerpilih
                && penyewaanTerpilih != null
                && "Menunggu".equalsIgnoreCase(penyewaanTerpilih.getStsPenyewaan());
        txtTotalDibayarAwal.setDisable(!bolehDp);
        if (bolehDp) {
            txtTotalDibayarAwal.setPromptText("0");
        } else {
            txtTotalDibayarAwal.clear();
            txtTotalDibayarAwal.setPromptText(adaBarisTerpilih ? "-" : "Hanya untuk penyewaan berstatus Menunggu");
        }

        boolean bisaDibayar = adaBarisTerpilih
                && selectedTagihan != null
                && ("Belum Lunas".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran())
                || "Terlambat".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran()));
        btnBayar.setDisable(!bisaDibayar);
        btnOtomatis.setDisable(!bisaDibayar);
        txtNominalBayar.setDisable(!bisaDibayar);
    }

    private void bersihForm() {
        txtPenyewaanTerpilih.clear();
        cbMetodeBayar.setValue(null);
        tglJatuhTempoTerpilih = null;
        txtTglJatuhTempo.clear();
        txtTglBayar.setText(LocalDate.now().format(FMT_TGL));
        txtTotalDibayarAwal.clear();
        txtStatus.setText("Belum Lunas");
        txtTotalTagihan.clear();
        txtTotalBiayaSewa.clear();
        txtTotalBiayaTambahan.clear();
        txtSudahDibayar.clear();
        txtNominalBayar.clear();
        penyewaanTerpilih = null;
        selectedTagihan = null;
        hargaSewaTerpilih = 0;
        daftarBiayaTambahan.clear();
        refreshTabelBiayaTambahan();
    }

    // 11. VALIDASI
    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        if (penyewaanTerpilih == null) sb.append("• Penyewaan wajib dipilih.\n");
        if (cbMetodeBayar.getValue() == null) sb.append("• Metode bayar wajib dipilih.\n");
        if (tglJatuhTempoTerpilih == null) sb.append("• Tanggal jatuh tempo wajib diisi.\n");

        if (!txtTotalDibayarAwal.isDisabled()) {
            String dpAwalText = txtTotalDibayarAwal.getText() == null ? "" : txtTotalDibayarAwal.getText().trim();
            if (!dpAwalText.isEmpty()) {
                try {
                    double nilai = Double.parseDouble(dpAwalText);
                    if (nilai < 0) sb.append("• Nominal dibayar di awal tidak boleh negatif.\n");
                } catch (NumberFormatException e) {
                    sb.append("• Nominal dibayar di awal harus berupa angka.\n");
                }
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

    // 13. EVENT HANDLER — PILIH PENYEWAAN (2 tahap: pilih Penyewaan, lalu pilih bulan tagihan)
    @FXML
    void onPilihPenyewaan(ActionEvent event) {
        try {
            // Tahap 1: pilih Penyewaan (yang masih punya sisa bulan belum ditagih).
            FXMLLoader loaderPenyewaan = new FXMLLoader(getClass().getResource("/com/sigap/view/Tagihan Pembayaran/PilihPenyewaan.fxml"));
            Parent rootPenyewaan = loaderPenyewaan.load();
            PilihPenyewaanController controllerPenyewaan = loaderPenyewaan.getController();

            Stage dialogPenyewaan = new Stage();
            dialogPenyewaan.setTitle("Pilih Penyewaan");
            dialogPenyewaan.initModality(Modality.APPLICATION_MODAL);
            if (txtPenyewaanTerpilih.getScene() != null) dialogPenyewaan.initOwner(txtPenyewaanTerpilih.getScene().getWindow());
            dialogPenyewaan.setScene(new Scene(rootPenyewaan));
            dialogPenyewaan.showAndWait();

            Penyewaan hasilPenyewaan = controllerPenyewaan.getPenyewaanTerpilih();
            if (hasilPenyewaan == null) return; // dibatalkan di tahap 1

            // Tahap 2: dari penyewaan itu, pilih 1 slot bulan (virtual) yang belum ditagih.
            FXMLLoader loaderBulan = new FXMLLoader(getClass().getResource("/com/sigap/view/Tagihan Pembayaran/PilihBulanTagihan.fxml"));
            Parent rootBulan = loaderBulan.load();
            PilihBulanTagihanController controllerBulan = loaderBulan.getController();
            controllerBulan.setPenyewaan(hasilPenyewaan);

            Stage dialogBulan = new Stage();
            dialogBulan.setTitle("Pilih Bulan Tagihan");
            dialogBulan.initModality(Modality.APPLICATION_MODAL);
            if (txtPenyewaanTerpilih.getScene() != null) dialogBulan.initOwner(txtPenyewaanTerpilih.getScene().getWindow());
            dialogBulan.setScene(new Scene(rootBulan));
            dialogBulan.showAndWait();

            LocalDate jatuhTempoTerpilih = controllerBulan.getJatuhTempoTerpilih();
            if (jatuhTempoTerpilih == null) return; // dibatalkan di tahap 2, jangan ubah apa pun di form

            penyewaanTerpilih = hasilPenyewaan;
            txtPenyewaanTerpilih.setText(hasilPenyewaan.getIdPenyewaan() + " - Kios " + hasilPenyewaan.getIdKios());
            tglJatuhTempoTerpilih = jatuhTempoTerpilih;
            txtTglJatuhTempo.setText(jatuhTempoTerpilih.format(FMT_TGL));

            // Ambil Harga_Kios buat preview Total Tagihan -- SESUAIKAN nama
            // method di CRUD_Kios kalau beda dari getById(idKios).
            try {
                Kios kios = CRUD_Kios.getById(hasilPenyewaan.getIdKios());
                hargaSewaTerpilih = (kios != null) ? kios.getHargaKios() : 0;
            } catch (Exception exKios) {
                hargaSewaTerpilih = 0;
            }
            refreshPreviewTotalTagihan();

            // Refresh status field DP: hanya terbuka jika status penyewaan "Menunggu".
            setFormState(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih penyewaan gagal dibuka. Silakan coba lagi.");
        }
    }

    // 13b. EVENT HANDLER — PILIH BIAYA TAMBAHAN (buka dialog PilihBiayaTambahan)
    @FXML
    void onPilihBiayaTambahan(ActionEvent event) {
        try {
            FXMLLoader loaderBiaya = new FXMLLoader(getClass().getResource("/com/sigap/view/Tagihan Pembayaran/PilihBiayaTambahan.fxml"));
            Parent rootBiaya = loaderBiaya.load();
            PilihBiayaTambahanController controllerBiaya = loaderBiaya.getController();
            controllerBiaya.setDaftarAwal(daftarBiayaTambahan, masterBiayaTambahan);
            // WAJIB: tanpa ini, Jumlah_Hari untuk jenis "Keterlambatan Bayar Sewa"
            // di dalam dialog tidak akan pernah terhitung otomatis (tetap 0),
            // karena hitungHariTerlambat() di sana butuh Tgl_Jatuh_Tempo.
            // Tgl_Bayar dianggap hari ini karena txtTglBayar selalu diisi
            // LocalDate.now() dan tidak bisa diedit untuk tagihan baru.
            controllerBiaya.setInfoJatuhTempo(tglJatuhTempoTerpilih, LocalDate.now());

            Stage dialogBiaya = new Stage();
            dialogBiaya.setTitle("Tambah Biaya Tambahan");
            dialogBiaya.initModality(Modality.APPLICATION_MODAL);
            if (txtPenyewaanTerpilih.getScene() != null) dialogBiaya.initOwner(txtPenyewaanTerpilih.getScene().getWindow());
            dialogBiaya.setScene(new Scene(rootBiaya));
            dialogBiaya.showAndWait();

            // isSelesaiDiklik() dipakai supaya "Selesai tanpa pilih apa-apa" (list kosong)
            // tidak salah dianggap sama dengan "Batal" (list juga kosong).
            if (!controllerBiaya.isSelesaiDiklik()) return;

            List<DetailTagihanBiaya> hasilDialog = controllerBiaya.getDaftarBiayaTerpilih();

            if (selectedTagihan == null) {
                // Tagihan BARU (belum Simpan): tetap staging seperti biasa di
                // klien, baru benar-benar di-insert ke DB pas onSimpan().
                daftarBiayaTambahan.setAll(hasilDialog);
                refreshTabelBiayaTambahan();
                refreshPreviewTotalTagihan();
            } else {
                // Tagihan SUDAH TERSIMPAN (status Belum Lunas -- tombol ini
                // cuma aktif di kondisi itu, lihat setFormState()): tidak ada
                // "staging" lagi, langsung sinkronkan ke DB (insert baris baru,
                // delete baris yang dihapus kasir di dialog).
                simpanPerubahanBiayaTambahanTersimpan(selectedTagihan.getIdTagihanPembayaran(), hasilDialog);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih biaya tambahan gagal dibuka. Silakan coba lagi.");
        }
    }

    /**
     * Sinkronkan perubahan biaya tambahan dari dialog ke DB untuk tagihan yang
     * SUDAH TERSIMPAN (status Belum Lunas). Dibandingkan terhadap data terbaru
     * dari DB (bukan daftarBiayaTambahan di klien) supaya perbandingan lama-vs-
     * baru akurat. Baris baru di-insert, baris yang dihapus kasir di dialog
     * di-delete (lihat catatan di CRUD_DetailTagihanBiaya soal delete manual).
     */
    private void simpanPerubahanBiayaTambahanTersimpan(String idTagihan, List<DetailTagihanBiaya> hasilDialog) {
        List<DetailTagihanBiaya> lamaDariDb;
        try {
            lamaDariDb = CRUD_DetailTagihanBiaya.getByIdTagihanPembayaran(idTagihan);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Memuat",
                    "Gagal memuat ulang biaya tambahan tagihan ini. Perubahan belum disimpan, coba lagi.");
            return;
        }

        List<DetailTagihanBiaya> ditambahkan = hasilDialog.stream()
                .filter(baru -> lamaDariDb.stream().noneMatch(l -> l.getIdBiayaTambahan().equals(baru.getIdBiayaTambahan())))
                .toList();
        List<DetailTagihanBiaya> dihapus = lamaDariDb.stream()
                .filter(lama -> hasilDialog.stream().noneMatch(b -> b.getIdBiayaTambahan().equals(lama.getIdBiayaTambahan())))
                .toList();

        if (ditambahkan.isEmpty() && dihapus.isEmpty()) return; // gak ada perubahan

        boolean semuaSukses = true;
        for (DetailTagihanBiaya d : ditambahkan) {
            try {
                CRUD_DetailTagihanBiaya.insert(new DetailTagihanBiaya(
                        idTagihan, d.getIdBiayaTambahan(), d.getJumlahHari(), d.getSubTotal()));
            } catch (Exception e) {
                e.printStackTrace(); // SEMENTARA: biar error SQL asli kelihatan di console
                semuaSukses = false;
            }
        }
        for (DetailTagihanBiaya d : dihapus) {
            try {
                CRUD_DetailTagihanBiaya.delete(idTagihan, d.getIdBiayaTambahan());
            } catch (Exception e) {
                e.printStackTrace(); // SEMENTARA: biar error SQL asli kelihatan di console
                semuaSukses = false;
            }
        }

        // Refresh semua tampilan dari sumber kebenaran (DB): panel biaya
        // tambahan, tabel daftar tagihan, dan field Total_Tagihan tagihan ini.
        muatBiayaTambahanUntukTagihan(idTagihan);
        loadData();
        masterList.stream()
                .filter(t -> t.getIdTagihanPembayaran().equals(idTagihan))
                .findFirst()
                .ifPresent(t -> {
                    selectedTagihan = t;
                    txtTotalTagihan.setText(FMT_RUPIAH.format((long) t.getTotalTagihan()));
                    txtTotalBiayaTambahan.setText(FMT_RUPIAH.format((long) t.getTotalBiayaTambahan()));
                    txtSudahDibayar.setText(FMT_RUPIAH.format((long) t.getTotalDibayar()));
                    txtStatus.setText(t.getStsTagihanPembayaran());
                });

        if (semuaSukses) {
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Biaya tambahan tagihan berhasil diperbarui.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Sebagian Gagal",
                    "Sebagian perubahan biaya tambahan gagal disimpan ke database. Periksa kembali daftar biaya tagihan ini.");
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
            double dibayarAwal = 0;
            if (!txtTotalDibayarAwal.isDisabled()) {
                String dpAwalText = txtTotalDibayarAwal.getText() == null ? "" : txtTotalDibayarAwal.getText().trim();
                dibayarAwal = dpAwalText.isEmpty() ? 0 : Double.parseDouble(dpAwalText);
            }

            TagihanPembayaranSewa t = new TagihanPembayaranSewa(
                    txtIdTagihan.getText().trim(),
                    penyewaanTerpilih.getIdPenyewaan(),
                    karyawanLogin.getIdKaryawan(),
                    LocalDate.now(),
                    tglJatuhTempoTerpilih,
                    0, 0, 0,
                    dibayarAwal,
                    cbMetodeBayar.getValue(),
                    "Belum Lunas"
            );

            CRUD_TagihanPembayaranSewa.insert(t);

            // Simpan Detail_Tagihan_Biaya SETELAH Id_Tagihan_Pembayaran tersimpan --
            // FK Detail_Tagihan_Biaya.Id_Tagihan_Pembayaran butuh baris induknya ada dulu.
            // SESUAIKAN: kalau spInsertDetailTagihanBiaya kamu juga bertugas
            // mengisi ulang Total_Biaya_Tambahan & Total_Tagihan di
            // Tagihan_Pembayaran_Sewa, tidak perlu langkah tambahan apa pun di
            // sini. Kalau totalnya masih perlu di-refresh terpisah, tambahkan
            // pemanggilannya di sini juga.
            if (!daftarBiayaTambahan.isEmpty()) {
                boolean semuaBiayaSukses = true;
                for (DetailTagihanBiaya d : daftarBiayaTambahan) {
                    try {
                        DetailTagihanBiaya detailFinal = new DetailTagihanBiaya(
                                t.getIdTagihanPembayaran(), d.getIdBiayaTambahan(),
                                d.getJumlahHari(), d.getSubTotal());
                        CRUD_DetailTagihanBiaya.insert(detailFinal);
                    } catch (Exception exDetail) {
                        semuaBiayaSukses = false;
                    }
                }
                if (!semuaBiayaSukses) {
                    showAlert(Alert.AlertType.WARNING, "Sebagian Biaya Tambahan Gagal",
                            "Tagihan tersimpan, tapi ada biaya tambahan yang gagal dicatat. "
                                    + "Periksa kembali tagihan [" + t.getIdTagihanPembayaran() + "].");
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Tagihan pembayaran sewa berhasil disimpan.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            e.printStackTrace();
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
            nominal = parseNominal(nominalText);
            if (nominal <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validasi Input", "Nominal bayar harus lebih dari 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", "Nominal bayar harus berupa angka.");
            return;
        }

        // Tidak ada cicilan untuk tagihan pembayaran sewa: sekali Bayar, harus
        // langsung melunasi sisa tagihan. Nominal WAJIB persis sama dengan sisa
        // (Total_Tagihan - Total_Dibayar) -- tidak boleh kurang (baru sebagian)
        // ataupun lebih (kelebihan bayar). Pakai toleransi kecil untuk floating
        // point, karena Rupiah pada dasarnya bilangan bulat.
        double sisaTagihan = selectedTagihan.getTotalTagihan() - selectedTagihan.getTotalDibayar();
        if (Math.abs(nominal - sisaTagihan) > 0.5) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input",
                    "Tagihan pembayaran sewa tidak bisa dicicil. Nominal bayar harus persis Rp "
                            + FMT_RUPIAH.format((long) sisaTagihan) + " (sisa tagihan).\n"
                            + "Klik tombol \"Otomatis\" untuk mengisi nominal secara otomatis.");
            return;
        }

        String metode = cbMetodeBayar.getValue() != null ? cbMetodeBayar.getValue() : selectedTagihan.getMetodeBayar();
        String id = selectedTagihan.getIdTagihanPembayaran();

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Pembayaran");
        konfirmasi.setHeaderText("Lunasi Tagihan");
        konfirmasi.setContentText("Lunasi tagihan [" + id + "] dengan pembayaran Rp "
                + FMT_RUPIAH.format((long) nominal) + "?\nLanjutkan?");
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

    // 15b. EVENT HANDLER — OTOMATIS (isi nominal bayar = sisa tagihan yang dipilih)
    @FXML
    void onIsiOtomatis(ActionEvent event) {
        if (selectedTagihan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tagihan yang ingin dibayar.");
            return;
        }
        double sisaTagihan = selectedTagihan.getTotalTagihan() - selectedTagihan.getTotalDibayar();
        txtNominalBayar.setText(FMT_RUPIAH.format((long) sisaTagihan));
    }

    /**
     * Parsing nominal yang toleran terhadap pemisah ribuan (mis. "50.000" dari
     * tombol Otomatis) maupun angka polos yang diketik manual (mis. "50000").
     * Rupiah tidak punya desimal, jadi semua karakter selain digit dibuang
     * dulu sebelum di-parse.
     */
    private double parseNominal(String text) throws NumberFormatException {
        String digitsOnly = text.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) throw new NumberFormatException("Nominal kosong");
        return Double.parseDouble(digitsOnly);
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
        tglJatuhTempoTerpilih = t.getTglJatuhTempo();
        txtTglJatuhTempo.setText(t.getTglJatuhTempo() == null ? "" : t.getTglJatuhTempo().format(FMT_TGL));
        txtTglBayar.setText(t.getTglBayar() == null ? "" : t.getTglBayar().format(FMT_TGL));
        txtStatus.setText(t.getStsTagihanPembayaran());
        txtTotalTagihan.setText(FMT_RUPIAH.format((long) t.getTotalTagihan()));
        txtTotalBiayaSewa.setText(FMT_RUPIAH.format((long) t.getTotalBiayaSewa()));
        txtTotalBiayaTambahan.setText(FMT_RUPIAH.format((long) t.getTotalBiayaTambahan()));
        txtSudahDibayar.setText(FMT_RUPIAH.format((long) t.getTotalDibayar()));
        txtNominalBayar.clear();

        muatBiayaTambahanUntukTagihan(t.getIdTagihanPembayaran());
        setFormState(true);
    }

    /** Tampilkan Detail_Tagihan_Biaya yang sudah tersimpan untuk tagihan yang sedang dilihat. */
    private void muatBiayaTambahanUntukTagihan(String idTagihanPembayaran) {
        try {
            // SESUAIKAN: nama method di CRUD_DetailTagihanBiaya kalau berbeda
            // (mis. getByIdTagihan / findByTagihan).
            List<DetailTagihanBiaya> existing = CRUD_DetailTagihanBiaya.getByIdTagihanPembayaran(idTagihanPembayaran);
            daftarBiayaTambahan.setAll(existing);
        } catch (Exception e) {
            daftarBiayaTambahan.clear();
        }
        refreshTabelBiayaTambahan();
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