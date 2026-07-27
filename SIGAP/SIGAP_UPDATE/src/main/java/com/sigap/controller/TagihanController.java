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
import com.sigap.APP.CRUD_Penyewa;
import com.sigap.APP.CRUD_Penyewaan;
import com.sigap.APP.CRUD_TagihanPembayaranSewa;
import com.sigap.util.Session;

import java.sql.SQLException;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TagihanController implements Initializable {

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

    @FXML
    private TextField txtSudahDibayar;
    @FXML
    private TextField txtNominalBayar;
    @FXML
    private Button btnOtomatis;
    @FXML
    private Button btnBayar;

    @FXML
    private TextField txtCari;
    @FXML
    private MenuButton btnFilter;
    @FXML
    private Menu menuPenyewaan;
    @FXML
    private RadioMenuItem rmStatusBelumLunas;
    @FXML
    private RadioMenuItem rmStatusLunas;
    @FXML
    private RadioMenuItem rmStatusTerlambat;
    @FXML
    private RadioMenuItem rmStatusDibatalkan;
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

    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;

    private final ObservableList<TagihanPembayaranSewa> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    private List<TagihanPembayaranSewa> daftarLengkap = List.of();
    private Map<String, Penyewaan> petaPenyewaan = Map.of();
    private Map<String, Penyewa> petaPenyewa = Map.of();

    private String filterIdPenyewaan = null;
    private String filterStatus = null;

    private Penyewaan penyewaanTerpilih = null;
    private double hargaSewaTerpilih = 0;

    private LocalDate tglJatuhTempoTerpilih = null;

    private TagihanPembayaranSewa selectedTagihan = null;

    private final ObservableList<DetailTagihanBiaya> daftarBiayaTambahan = FXCollections.observableArrayList();
    private List<BiayaTambahan> masterBiayaTambahan = List.of();

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    private static final NumberFormat FMT_RUPIAH = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    private static final String KATA_KUNCI_KETERLAMBATAN = "keterlambatan";
    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;-fx-text-fill:#888;";

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
        txtTglJatuhTempo.setEditable(false);
        txtTglJatuhTempo.setStyle(STYLE_READONLY);

        cbMetodeBayar.setItems(FXCollections.observableArrayList(
                "Tunai", "Transfer Bank", "Kartu Debit"));

        setupTable();
        setupTabelBiayaTambahan();
        setupNominalBayarFormatter();
        setupTotalDibayarAwalFormatter();
        setupFilterStatus();
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

    private void refreshPreviewTotalTagihan() {
        double totalBiayaTambahan = daftarBiayaTambahan.stream().mapToDouble(DetailTagihanBiaya::getSubTotal).sum();
        double preview = hargaSewaTerpilih + totalBiayaTambahan;
        txtTotalTagihan.setText(FMT_RUPIAH.format((long) preview));
        txtTotalBiayaSewa.setText(FMT_RUPIAH.format((long) hargaSewaTerpilih));
    }

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

    private boolean sedangMemformatDpAwal = false;

    private void setupTotalDibayarAwalFormatter() {
        txtTotalDibayarAwal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (sedangMemformatDpAwal) return;

            String digitsOnly = newVal == null ? "" : newVal.replaceAll("[^0-9]", "");
            String formatted = digitsOnly.isEmpty() ? "" : FMT_RUPIAH.format(Long.parseLong(digitsOnly));

            if (!formatted.equals(newVal)) {
                sedangMemformatDpAwal = true;
                txtTotalDibayarAwal.setText(formatted);
                txtTotalDibayarAwal.positionCaret(formatted.length());
                sedangMemformatDpAwal = false;
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdTagihanPembayaran()));
        colPenyewaan.setCellValueFactory(d -> new SimpleStringProperty(labelPenyewaan(d.getValue().getIdPenyewaan())));
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

    private String labelPenyewaan(String idPenyewaan) {
        if (idPenyewaan == null) return "";
        Penyewaan penyewaan = petaPenyewaan.get(idPenyewaan);
        if (penyewaan == null) return idPenyewaan;
        Penyewa penyewa = petaPenyewa.get(penyewaan.getIdPenyewa());
        if (penyewa == null || penyewa.getNamaPenyewa() == null || penyewa.getNamaPenyewa().isBlank()) {
            return idPenyewaan;
        }
        return idPenyewaan + " - " + penyewa.getNamaPenyewa();
    }

    private void muatPetaPenyewaan() {
        try {
            petaPenyewaan = CRUD_Penyewaan.getAll().stream()
                    .collect(Collectors.toMap(Penyewaan::getIdPenyewaan, p -> p, (a, b) -> a));
        } catch (Exception e) {
            petaPenyewaan = Map.of();
        }
        try {
            petaPenyewa = CRUD_Penyewa.getAll().stream()
                    .collect(Collectors.toMap(Penyewa::getIdPenyewa, p -> p, (a, b) -> a));
        } catch (Exception e) {
            petaPenyewa = Map.of();
        }
    }

    private String styleBadgeStatus(String status) {
        String base = "-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;";
        return switch (status == null ? "" : status) {
            case "Lunas"      -> "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;" + base;
            case "Terlambat"  -> "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;" + base;
            case "Dibatalkan" -> "-fx-background-color:#EAEAEA;-fx-text-fill:#555555;" + base;
            default           -> "-fx-background-color:#FFF3D6;-fx-text-fill:#B8860B;" + base;
        };
    }

    private void setupFilterStatus() {
        ToggleGroup grupStatus = new ToggleGroup();
        rmStatusBelumLunas.setToggleGroup(grupStatus);
        rmStatusLunas.setToggleGroup(grupStatus);
        rmStatusTerlambat.setToggleGroup(grupStatus);
        rmStatusDibatalkan.setToggleGroup(grupStatus);
    }

    private void populateFilterMenus() {
        menuPenyewaan.getItems().clear();
        ToggleGroup grupPenyewaan = new ToggleGroup();
        RadioMenuItem rmSemuaPenyewaan = new RadioMenuItem("Semua Penyewaan");
        rmSemuaPenyewaan.setToggleGroup(grupPenyewaan);
        rmSemuaPenyewaan.setSelected(filterIdPenyewaan == null);
        rmSemuaPenyewaan.setOnAction(e -> { filterIdPenyewaan = null; terapkanFilterDanCari(); });
        menuPenyewaan.getItems().add(rmSemuaPenyewaan);
        petaPenyewaan.values().forEach(p -> {
            RadioMenuItem rmi = new RadioMenuItem(labelPenyewaan(p.getIdPenyewaan()));
            rmi.setToggleGroup(grupPenyewaan);
            rmi.setSelected(p.getIdPenyewaan().equalsIgnoreCase(filterIdPenyewaan));
            rmi.setOnAction(e -> { filterIdPenyewaan = p.getIdPenyewaan(); terapkanFilterDanCari(); });
            menuPenyewaan.getItems().add(rmi);
        });
    }

    private void loadData() {
        try {
            muatPetaPenyewaan();
            daftarLengkap = CRUD_TagihanPembayaranSewa.getAll();
            populateFilterMenus();
            terapkanFilterDanCari();
            tabelTagihan.refresh();
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

    private void setFormState(boolean adaBarisTerpilih) {
        btnSimpan.setDisable(adaBarisTerpilih);
        btnPilihPenyewaan.setDisable(adaBarisTerpilih);
        boolean bolehUbahBiayaTambahan = !adaBarisTerpilih
                || (selectedTagihan != null && "Belum Lunas".equalsIgnoreCase(selectedTagihan.getStsTagihanPembayaran()));
        btnPilihBiayaTambahan.setDisable(!bolehUbahBiayaTambahan);
        cbMetodeBayar.setDisable(adaBarisTerpilih);

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

    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        if (penyewaanTerpilih == null) sb.append("• Penyewaan wajib dipilih.\n");
        if (cbMetodeBayar.getValue() == null) sb.append("• Metode bayar wajib dipilih.\n");
        if (tglJatuhTempoTerpilih == null) sb.append("• Tanggal jatuh tempo wajib diisi.\n");

        if (!txtTotalDibayarAwal.isDisabled()) {
            String dpAwalText = txtTotalDibayarAwal.getText() == null ? "" : txtTotalDibayarAwal.getText().trim();
            if (!dpAwalText.isEmpty()) {
                try {
                    double nilai = parseNominal(dpAwalText);
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

    @FXML
    void onPilihPenyewaan(ActionEvent event) {
        try {
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
            if (hasilPenyewaan == null) return;

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
            if (jatuhTempoTerpilih == null) return;

            penyewaanTerpilih = hasilPenyewaan;
            txtPenyewaanTerpilih.setText(labelPenyewaanTerpilih(hasilPenyewaan));
            tglJatuhTempoTerpilih = jatuhTempoTerpilih;
            txtTglJatuhTempo.setText(jatuhTempoTerpilih.format(FMT_TGL));

            try {
                Kios kios = CRUD_Kios.getById(hasilPenyewaan.getIdKios());
                hargaSewaTerpilih = (kios != null) ? kios.getHargaKios() : 0;
            } catch (Exception exKios) {
                hargaSewaTerpilih = 0;
            }
            refreshPreviewTotalTagihan();

            setFormState(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih penyewaan gagal dibuka. Silakan coba lagi.");
        }
    }

    private String labelPenyewaanTerpilih(Penyewaan p) {
        String namaPenyewa = null;
        try {
            Penyewa penyewa = CRUD_Penyewa.getById(p.getIdPenyewa());
            namaPenyewa = (penyewa != null) ? penyewa.getNamaPenyewa() : null;
        } catch (Exception ex) {
        }
        String bagianPenyewa = (namaPenyewa == null || namaPenyewa.isBlank())
                ? p.getIdPenyewa()
                : p.getIdPenyewa() + " - " + namaPenyewa;
        return p.getIdPenyewaan() + " (" + bagianPenyewa + ") - Kios " + p.getIdKios();
    }

    @FXML
    void onPilihBiayaTambahan(ActionEvent event) {
        try {
            FXMLLoader loaderBiaya = new FXMLLoader(getClass().getResource("/com/sigap/view/Tagihan Pembayaran/PilihBiayaTambahan.fxml"));
            Parent rootBiaya = loaderBiaya.load();
            PilihBiayaTambahanController controllerBiaya = loaderBiaya.getController();
            controllerBiaya.setDaftarAwal(daftarBiayaTambahan, masterBiayaTambahan);
            controllerBiaya.setInfoJatuhTempo(tglJatuhTempoTerpilih, LocalDate.now());

            Stage dialogBiaya = new Stage();
            dialogBiaya.setTitle("Tambah Biaya Tambahan");
            dialogBiaya.initModality(Modality.APPLICATION_MODAL);
            if (txtPenyewaanTerpilih.getScene() != null) dialogBiaya.initOwner(txtPenyewaanTerpilih.getScene().getWindow());
            dialogBiaya.setScene(new Scene(rootBiaya));
            dialogBiaya.showAndWait();

            if (!controllerBiaya.isSelesaiDiklik()) return;

            List<DetailTagihanBiaya> hasilDialog = controllerBiaya.getDaftarBiayaTerpilih();

            if (selectedTagihan == null) {
                daftarBiayaTambahan.setAll(hasilDialog);
                refreshTabelBiayaTambahan();
                refreshPreviewTotalTagihan();
            } else {
                simpanPerubahanBiayaTambahanTersimpan(selectedTagihan.getIdTagihanPembayaran(), hasilDialog);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih biaya tambahan gagal dibuka. Silakan coba lagi.");
        }
    }

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

        if (ditambahkan.isEmpty() && dihapus.isEmpty()) return;

        boolean semuaSukses = true;
        for (DetailTagihanBiaya d : ditambahkan) {
            try {
                CRUD_DetailTagihanBiaya.insert(new DetailTagihanBiaya(
                        idTagihan, d.getIdBiayaTambahan(), d.getJumlahHari(), d.getSubTotal()));
            } catch (Exception e) {
                e.printStackTrace();
                semuaSukses = false;
            }
        }
        for (DetailTagihanBiaya d : dihapus) {
            try {
                CRUD_DetailTagihanBiaya.delete(idTagihan, d.getIdBiayaTambahan());
            } catch (Exception e) {
                e.printStackTrace();
                semuaSukses = false;
            }
        }

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

    @FXML
    void onSimpan(ActionEvent event) {
        if (!validasi()) return;

        Karyawan karyawanLogin = Session.getLoggedInUser();
        if (karyawanLogin == null) {
            showAlert(Alert.AlertType.ERROR, "Sesi Tidak Valid",
                    "Sesi login karyawan tidak ditemukan. Silakan login ulang.");
            return;
        }

        if (LocalDate.now().isAfter(tglJatuhTempoTerpilih)) {
            boolean sudahAdaBiayaKeterlambatan;
            try {
                Map<String, BiayaTambahan> masterBiaya = CRUD_BiayaTambahan.getAll().stream()
                        .collect(Collectors.toMap(BiayaTambahan::getIdBiayaTambahan, b -> b));

                sudahAdaBiayaKeterlambatan = daftarBiayaTambahan.stream()
                        .anyMatch(d -> {
                            BiayaTambahan master = masterBiaya.get(d.getIdBiayaTambahan());
                            return master != null
                                    && master.getJenisBiayaTambahan() != null
                                    && master.getJenisBiayaTambahan().toLowerCase().contains(KATA_KUNCI_KETERLAMBATAN);
                        });
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Gagal Memeriksa Biaya Tambahan",
                        "Terjadi kesalahan saat memeriksa data biaya tambahan. Coba lagi.");
                return;
            }

            if (!sudahAdaBiayaKeterlambatan) {
                showAlert(Alert.AlertType.WARNING, "Biaya Keterlambatan Diperlukan",
                        "Pembayaran ini melewati tanggal jatuh tempo (" + tglJatuhTempoTerpilih + "). "
                                + "Tambahkan biaya tambahan jenis 'Keterlambatan Sewa' terlebih dahulu "
                                + "sebelum menyimpan.");
                return;
            }
        }

        try {
            double dibayarAwal = 0;
            if (!txtTotalDibayarAwal.isDisabled()) {
                String dpAwalText = txtTotalDibayarAwal.getText() == null ? "" : txtTotalDibayarAwal.getText().trim();
                dibayarAwal = dpAwalText.isEmpty() ? 0 : parseNominal(dpAwalText);
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

        double sisaTagihan = selectedTagihan.getTotalTagihan() - selectedTagihan.getTotalDibayar();
        if (Math.abs(nominal - sisaTagihan) > 0.5) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input",
                    "Tagihan pembayaran sewa tidak bisa dicicil. Nominal bayar harus persis Rp "
                            + FMT_RUPIAH.format((long) sisaTagihan) + " (sisa tagihan).\n"
                            + "Klik tombol \"Refresh\" untuk mengisi nominal secara otomatis.");
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

    @FXML
    void onIsiOtomatis(ActionEvent event) {
        if (selectedTagihan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tagihan yang ingin dibayar.");
            return;
        }
        double sisaTagihan = selectedTagihan.getTotalTagihan() - selectedTagihan.getTotalDibayar();
        txtNominalBayar.setText(FMT_RUPIAH.format((long) sisaTagihan));
    }

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

    private void muatBiayaTambahanUntukTagihan(String idTagihanPembayaran) {
        try {
            List<DetailTagihanBiaya> existing = CRUD_DetailTagihanBiaya.getByIdTagihanPembayaran(idTagihanPembayaran);
            daftarBiayaTambahan.setAll(existing);
        } catch (Exception e) {
            daftarBiayaTambahan.clear();
        }
        refreshTabelBiayaTambahan();
    }

    @FXML
    void onCari(ActionEvent event) {
        terapkanFilterDanCari();
    }

    @FXML
    void onFilterStatus(ActionEvent event) {
        filterStatus = rmStatusBelumLunas.isSelected() ? "Belum Lunas"
                : rmStatusLunas.isSelected() ? "Lunas"
                : rmStatusTerlambat.isSelected() ? "Terlambat"
                : rmStatusDibatalkan.isSelected() ? "Dibatalkan" : null;
        terapkanFilterDanCari();
    }

    @FXML
    void onResetFilter(ActionEvent event) {
        filterIdPenyewaan = null;
        filterStatus = null;
        rmStatusBelumLunas.setSelected(false);
        rmStatusLunas.setSelected(false);
        rmStatusTerlambat.setSelected(false);
        rmStatusDibatalkan.setSelected(false);
        populateFilterMenus();
        terapkanFilterDanCari();
    }

    private void terapkanFilterDanCari() {
        String kw = txtCari.getText() == null ? "" : txtCari.getText().trim().toLowerCase();

        List<TagihanPembayaranSewa> hasil = daftarLengkap.stream()
                .filter(t -> filterIdPenyewaan == null || filterIdPenyewaan.equalsIgnoreCase(t.getIdPenyewaan()))
                .filter(t -> filterStatus == null || filterStatus.equalsIgnoreCase(t.getStsTagihanPembayaran()))
                .filter(t -> kw.isEmpty() || cocokKeyword(t, kw))
                .collect(Collectors.toList());

        masterList.setAll(hasil);
        currentPage = 1;
        refreshTable();
    }

    private boolean cocokKeyword(TagihanPembayaranSewa t, String kwLower) {
        if (mengandung(t.getIdTagihanPembayaran(), kwLower)) return true;
        if (mengandung(t.getIdPenyewaan(), kwLower)) return true;
        if (mengandung(t.getIdKaryawan(), kwLower)) return true;
        if (mengandung(t.getStsTagihanPembayaran(), kwLower)) return true;

        Penyewaan penyewaan = petaPenyewaan.get(t.getIdPenyewaan());
        if (penyewaan != null) {
            Penyewa penyewa = petaPenyewa.get(penyewaan.getIdPenyewa());
            if (penyewa != null && mengandung(penyewa.getNamaPenyewa(), kwLower)) return true;
        }
        return false;
    }

    private boolean mengandung(String value, String kwLower) {
        return value != null && value.toLowerCase().contains(kwLower);
    }

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