package com.sigap.controller;

import com.sigap.ADT.*;
import com.sigap.APP.*;
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
import javafx.scene.control.*;
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


    @FXML private TableView<TagihanPembayaranSewa> tabelTagihan;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colId;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colPenyewaan;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colKaryawan;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colTglBayar;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colTglTempo;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colTotalTagihan;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colTotalDibayar;
    @FXML private TableColumn<TagihanPembayaranSewa, String> colStatus;


    @FXML private TextField txtIdTagihan;
    @FXML private TextField txtKaryawan;
    @FXML private TextField txtPenyewaan;
    @FXML private TextField txtTglBayar;
    @FXML private TextField txtTglJatuhTempo;
    @FXML private TextField txtTotalBiayaSewa;
    @FXML private TextField txtTotalBiayaTambahan;
    @FXML private TextField txtTotalTagihan;
    @FXML private TextField txtStatus;


    @FXML private TextField txtNominalBayar;
    @FXML private ComboBox<String> cbMetodeBayar;
    @FXML private Button btnOtomatis;
    @FXML private Button btnBayar;


    @FXML private TableView<DetailTagihanBiaya> tabelBiayaTambahan;
    @FXML private TableColumn<DetailTagihanBiaya, String> colJenisBiayaTambahan;
    @FXML private TableColumn<DetailTagihanBiaya, String> colJumlahHariBiaya;
    @FXML private TableColumn<DetailTagihanBiaya, String> colSubtotalBiaya;
    @FXML private Label lblTotalBiayaTambahan;
    @FXML private Button btnPilihBiayaTambahan;


    @FXML private TextField txtCari;
    @FXML private MenuButton btnFilter;
    @FXML private Menu menuPenyewaan;
    @FXML private RadioMenuItem rmStatusBelumLunas;
    @FXML private RadioMenuItem rmStatusLunas;
    @FXML private RadioMenuItem rmStatusTerlambat;
    @FXML private RadioMenuItem rmStatusDibatalkan;
    @FXML private Label lblPage;
    @FXML private Label lblTotal;

    private final ObservableList<TagihanPembayaranSewa> masterList = FXCollections.observableArrayList();
    private final ObservableList<DetailTagihanBiaya> daftarBiayaTambahan = FXCollections.observableArrayList();
    private List<BiayaTambahan> masterBiayaTambahan = List.of();

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    private List<TagihanPembayaranSewa> daftarLengkap = List.of();
    private Map<String, Penyewaan> petaPenyewaan = Map.of();
    private Map<String, Penyewa> petaPenyewa = Map.of();
    private Map<String, Karyawan> petaKaryawan = Map.of();

    private String filterIdPenyewaan = null;
    private String filterStatus = null;

    private TagihanPembayaranSewa selectedTagihan = null;

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    private static final NumberFormat FMT_RUPIAH = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupTabelBiayaTambahan();
        setupFilterStatus();
        setupComboMetodeBayar();
        setupNominalBayarFormatter();
        setFormState(false);


        setTextFieldsReadOnly();

        Platform.runLater(() -> {
            loadData();
            muatMasterBiayaTambahan();
            clearSelection();
        });
    }

    private void setTextFieldsReadOnly() {
        txtIdTagihan.setEditable(false);
        txtKaryawan.setEditable(false);
        txtPenyewaan.setEditable(false);
        txtTglBayar.setEditable(false);
        txtTglJatuhTempo.setEditable(false);
        txtTotalBiayaSewa.setEditable(false);
        txtTotalBiayaTambahan.setEditable(false);
        txtTotalTagihan.setEditable(false);
        txtStatus.setEditable(false);

        String style = "-fx-background-color: #F0F0F0; -fx-border-color: #D0D8E8; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12; " +
                "-fx-font-size: 13px; -fx-text-fill: #333;";
        txtIdTagihan.setStyle(style);
        txtKaryawan.setStyle(style);
        txtPenyewaan.setStyle(style);
        txtTglBayar.setStyle(style);
        txtTglJatuhTempo.setStyle(style);
        txtTotalBiayaSewa.setStyle(style);
        txtTotalBiayaTambahan.setStyle(style);
        txtTotalTagihan.setStyle(style);
        txtStatus.setStyle(style);
    }


    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdTagihanPembayaran()));
        colPenyewaan.setCellValueFactory(d -> new SimpleStringProperty(labelPenyewaan(d.getValue().getIdPenyewaan())));
        colKaryawan.setCellValueFactory(d -> new SimpleStringProperty(labelKaryawan(d.getValue().getIdKaryawan())));
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

        tabelTagihan.setItems(masterList);
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

    private String cariJenisBiaya(String idBiayaTambahan) {
        return masterBiayaTambahan.stream()
                .filter(b -> b.getIdBiayaTambahan().equals(idBiayaTambahan))
                .map(BiayaTambahan::getJenisBiayaTambahan)
                .findFirst()
                .orElse(idBiayaTambahan);
    }

    private void muatMasterBiayaTambahan() {
        try {
            masterBiayaTambahan = CRUD_BiayaTambahan.getAll();
        } catch (Exception e) {
            masterBiayaTambahan = List.of();
        }
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

    private void refreshTabelBiayaTambahan() {
        tabelBiayaTambahan.refresh();
        double total = daftarBiayaTambahan.stream().mapToDouble(DetailTagihanBiaya::getSubTotal).sum();
        lblTotalBiayaTambahan.setText("Rp " + FMT_RUPIAH.format((long) total));
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

    private void setupComboMetodeBayar() {
        cbMetodeBayar.setItems(FXCollections.observableArrayList(
                "Tunai", "Transfer Bank", "Kartu Debit"));
    }

    private void setupNominalBayarFormatter() {
        txtNominalBayar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String digits = newVal.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                String formatted = FMT_RUPIAH.format(Long.parseLong(digits));
                if (!formatted.equals(newVal)) {
                    txtNominalBayar.setText(formatted);
                    txtNominalBayar.positionCaret(formatted.length());
                }
            }
        });
    }


    private void muatPetaMaster() {
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
        try {
            petaKaryawan = CRUD_Karyawan.getAll().stream()
                    .collect(Collectors.toMap(Karyawan::getIdKaryawan, k -> k, (a, b) -> a));
        } catch (Exception e) {
            petaKaryawan = Map.of();
        }
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

    private String labelKaryawan(String idKaryawan) {
        if (idKaryawan == null) return "";
        Karyawan k = petaKaryawan.get(idKaryawan);
        if (k == null || k.getNamaKaryawan() == null || k.getNamaKaryawan().isBlank()) {
            return idKaryawan;
        }
        return idKaryawan + " - " + k.getNamaKaryawan();
    }

    private void loadData() {
        try {
            CRUD_TagihanPembayaranSewa.refreshStatus();
            muatPetaMaster();
            daftarLengkap = CRUD_TagihanPembayaranSewa.getAll();
            populateFilterMenus();
            terapkanFilterDanCari();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Koneksi",
                    "Gagal memuat data tagihan. Periksa koneksi ke database atau hubungi admin sistem.");
        }
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
        clearSelection();
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

    private void clearSelection() {
        selectedTagihan = null;
        txtIdTagihan.clear();
        txtKaryawan.clear();
        txtPenyewaan.clear();
        txtTglBayar.clear();
        txtTglJatuhTempo.clear();
        txtTotalBiayaSewa.clear();
        txtTotalBiayaTambahan.clear();
        txtTotalTagihan.clear();
        txtStatus.clear();
        txtNominalBayar.clear();
        cbMetodeBayar.setValue(null);
        btnBayar.setDisable(true);
        btnOtomatis.setDisable(true);
        txtNominalBayar.setDisable(true);
        daftarBiayaTambahan.clear();
        refreshTabelBiayaTambahan();
        tabelTagihan.getSelectionModel().clearSelection();
        setFormState(false);
    }

    private void setFormState(boolean adaTagihanTerpilih) {
        btnPilihBiayaTambahan.setDisable(!adaTagihanTerpilih);

    }


    @FXML
    void onTableClick(MouseEvent event) {
        TagihanPembayaranSewa t = tabelTagihan.getSelectionModel().getSelectedItem();
        if (t == null) {
            clearSelection();
            return;
        }

        selectedTagihan = t;


        txtIdTagihan.setText(t.getIdTagihanPembayaran());
        txtKaryawan.setText(labelKaryawan(t.getIdKaryawan()));
        txtPenyewaan.setText(labelPenyewaan(t.getIdPenyewaan()));
        txtTglBayar.setText(t.getTglBayar() == null ? "" : t.getTglBayar().format(FMT_TGL));
        txtTglJatuhTempo.setText(t.getTglJatuhTempo() == null ? "" : t.getTglJatuhTempo().format(FMT_TGL));
        txtTotalBiayaSewa.setText(FMT_RUPIAH.format((long) t.getTotalBiayaSewa()));
        txtTotalBiayaTambahan.setText(FMT_RUPIAH.format((long) t.getTotalBiayaTambahan()));
        txtTotalTagihan.setText(FMT_RUPIAH.format((long) t.getTotalTagihan()));
        txtStatus.setText(t.getStsTagihanPembayaran());


        muatBiayaTambahanUntukTagihan(t.getIdTagihanPembayaran());


        Penyewaan penyewaan = petaPenyewaan.get(t.getIdPenyewaan());
        boolean isPenyewaanDibatalkan = (penyewaan != null && "Dibatalkan".equalsIgnoreCase(penyewaan.getStsPenyewaan()));


        boolean bisaBayar = !"Lunas".equalsIgnoreCase(t.getStsTagihanPembayaran())
                && !"Dibatalkan".equalsIgnoreCase(t.getStsTagihanPembayaran())
                && !isPenyewaanDibatalkan;
        btnBayar.setDisable(!bisaBayar);
        btnOtomatis.setDisable(!bisaBayar);
        txtNominalBayar.setDisable(!bisaBayar);

        if (bisaBayar) {
            cbMetodeBayar.setValue(t.getMetodeBayar());
            txtNominalBayar.requestFocus();
        } else {
            cbMetodeBayar.setValue(null);
            txtNominalBayar.clear();
        }

        setFormState(true);
    }

    @FXML
    void onPilihBiayaTambahan(ActionEvent event) {
        if (selectedTagihan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tagihan terlebih dahulu.");
            return;
        }
        try {
            FXMLLoader loaderBiaya = new FXMLLoader(getClass().getResource("/com/sigap/view/Tagihan Pembayaran/PilihBiayaTambahan.fxml"));
            Parent rootBiaya = loaderBiaya.load();
            PilihBiayaTambahanController controllerBiaya = loaderBiaya.getController();
            controllerBiaya.setDaftarAwal(daftarBiayaTambahan, masterBiayaTambahan);
            controllerBiaya.setInfoJatuhTempo(selectedTagihan.getTglJatuhTempo(), LocalDate.now());

            Stage dialogBiaya = new Stage();
            dialogBiaya.setTitle("Tambah Biaya Tambahan");
            dialogBiaya.initModality(Modality.APPLICATION_MODAL);
            if (tabelTagihan.getScene() != null) dialogBiaya.initOwner(tabelTagihan.getScene().getWindow());
            dialogBiaya.setScene(new Scene(rootBiaya));
            dialogBiaya.showAndWait();

            if (!controllerBiaya.isSelesaiDiklik()) return;

            List<DetailTagihanBiaya> hasilDialog = controllerBiaya.getDaftarBiayaTerpilih();
            simpanPerubahanBiayaTambahan(selectedTagihan.getIdTagihanPembayaran(), hasilDialog);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog",
                    "Dialog pilih biaya tambahan gagal dibuka. Silakan coba lagi.");
        }
    }

    private void simpanPerubahanBiayaTambahan(String idTagihan, List<DetailTagihanBiaya> hasilDialog) {
        List<DetailTagihanBiaya> lama;
        try {
            lama = CRUD_DetailTagihanBiaya.getByIdTagihanPembayaran(idTagihan);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Memuat",
                    "Gagal memuat ulang biaya tambahan. Coba lagi.");
            return;
        }

        List<DetailTagihanBiaya> ditambahkan = hasilDialog.stream()
                .filter(baru -> lama.stream().noneMatch(l -> l.getIdBiayaTambahan().equals(baru.getIdBiayaTambahan())))
                .toList();
        List<DetailTagihanBiaya> dihapus = lama.stream()
                .filter(lamaItem -> hasilDialog.stream().noneMatch(b -> b.getIdBiayaTambahan().equals(lamaItem.getIdBiayaTambahan())))
                .toList();

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


        try {
            CRUD_TagihanPembayaranSewa.updateStatusTagihan(idTagihan);
        } catch (Exception e) {
            e.printStackTrace();
        }


        muatBiayaTambahanUntukTagihan(idTagihan);
        loadData();


        masterList.stream()
                .filter(t -> t.getIdTagihanPembayaran().equals(idTagihan))
                .findFirst()
                .ifPresent(t -> {
                    selectedTagihan = t;
                    txtTotalBiayaSewa.setText(FMT_RUPIAH.format((long) t.getTotalBiayaSewa()));
                    txtTotalBiayaTambahan.setText(FMT_RUPIAH.format((long) t.getTotalBiayaTambahan()));
                    txtTotalTagihan.setText(FMT_RUPIAH.format((long) t.getTotalTagihan()));
                    txtStatus.setText(t.getStsTagihanPembayaran());
                });

        if (semuaSukses) {
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Biaya tambahan tagihan berhasil diperbarui.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Sebagian Gagal",
                    "Sebagian perubahan biaya tambahan gagal disimpan. Periksa kembali.");
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

    @FXML
    void onBayar(ActionEvent event) {
        if (selectedTagihan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tagihan yang ingin dibayar.");
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
                    "Tagihan tidak bisa dicicil. Nominal harus Rp " + FMT_RUPIAH.format((long) sisaTagihan) + ".\n" +
                            "Gunakan 'Isi Otomatis'.");
            return;
        }

        String metode = cbMetodeBayar.getValue() != null ? cbMetodeBayar.getValue() : selectedTagihan.getMetodeBayar();
        String id = selectedTagihan.getIdTagihanPembayaran();

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Pembayaran");
        konfirmasi.setHeaderText("Lunasi Tagihan");
        konfirmasi.setContentText("Lunasi tagihan [" + id + "] dengan pembayaran Rp "
                + FMT_RUPIAH.format((long) nominal) + "?\nLanjutkan?");
        if (tabelTagihan.getScene() != null)
            konfirmasi.initOwner(tabelTagihan.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_TagihanPembayaranSewa.bayar(id, nominal, metode);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pembayaran berhasil dicatat.");
                loadData();
                clearSelection();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Gagal Membayar",
                        "Pembayaran gagal dicatat. Pastikan nominal tidak melebihi sisa tagihan.");
            }
        }
    }

    private double parseNominal(String text) throws NumberFormatException {
        String digitsOnly = text.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) throw new NumberFormatException("Nominal kosong");
        return Double.parseDouble(digitsOnly);
    }


    @FXML void onCari(ActionEvent event) { terapkanFilterDanCari(); }
    @FXML void onFilterStatus(ActionEvent event) {
        filterStatus = rmStatusBelumLunas.isSelected() ? "Belum Lunas"
                : rmStatusLunas.isSelected() ? "Lunas"
                : rmStatusTerlambat.isSelected() ? "Terlambat"
                : rmStatusDibatalkan.isSelected() ? "Dibatalkan" : null;
        terapkanFilterDanCari();
    }
    @FXML void onResetFilter(ActionEvent event) {
        filterIdPenyewaan = null;
        filterStatus = null;
        rmStatusBelumLunas.setSelected(false);
        rmStatusLunas.setSelected(false);
        rmStatusTerlambat.setSelected(false);
        rmStatusDibatalkan.setSelected(false);
        txtCari.clear();
        populateFilterMenus();
        terapkanFilterDanCari();
    }
    @FXML void onFirstPage(ActionEvent event) { currentPage = 1; refreshTable(); }
    @FXML void onLastPage(ActionEvent event) { currentPage = totalPage; refreshTable(); }
    @FXML void onNextPage(ActionEvent event) { if (currentPage < totalPage) { currentPage++; refreshTable(); } }
    @FXML void onPrevPage(ActionEvent event) { if (currentPage > 1) { currentPage--; refreshTable(); } }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (tabelTagihan.getScene() != null)
                alert.initOwner(tabelTagihan.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }
}