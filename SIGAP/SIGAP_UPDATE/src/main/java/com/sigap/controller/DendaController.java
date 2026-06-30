package com.sigap.controller;

import com.sigap.ADT.Denda;
import com.sigap.APP.CRUD_Denda;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DendaController implements Initializable {

    // ── Form fields ───────────────────────────────────────────────────
    @FXML private TextField txtIdDenda;
    @FXML private TextField txtJenisPelannggaran;   // sesuai fx:id di FXML (typo intentional)
    @FXML private TextField txtNominalDenda;
    @FXML private TextArea  txtKeterangan;
    @FXML private TextField txtstatus;

    // ── Tombol ───────────────────────────────────────────────────────
    @FXML private Button btnSimpan;
    @FXML private Button btnUbah;
    @FXML private Button btnHapus;

    // ── Pencarian ────────────────────────────────────────────────────
    @FXML private TextField txtcari;

    // ── Tabel ────────────────────────────────────────────────────────
    @FXML private TableView<Denda>           tabelDenda;
    @FXML private TableColumn<Denda, String> colId;
    @FXML private TableColumn<Denda, String> coljenispelanggaran;
    @FXML private TableColumn<Denda, String> colnominal;
    @FXML private TableColumn<Denda, String> colketerangan;
    @FXML private TableColumn<Denda, String> colstatus;

    // ── Label info ───────────────────────────────────────────────────
    @FXML private Label lblTotal;
    @FXML private Label lblPage;

    // ── State ────────────────────────────────────────────────────────
    private ObservableList<Denda> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage   = 1;

    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    // ── Style konstanta (dipakai berulang) ──────────────────────────
    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;"
                    + "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;"
                    + "-fx-font-size:13px;-fx-text-fill:#888;";
    private static final String STYLE_NORMAL =
            "-fx-background-color:WHITE;-fx-border-color:#D0D8E8;"
                    + "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;"
                    + "-fx-font-size:13px;";

    // ─────────────────────────────────────────────────────────────────
    //  initialize
    // ─────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdDenda.setEditable(false);

        // Status TIDAK PERNAH bisa diedit user, di mode apapun.
        txtstatus.setEditable(false);
        txtstatus.setStyle(STYLE_READONLY);

        setupTable();
        setupListeners();
        setFormState(false, false);
        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
            txtstatus.setText("Aktif"); // default saat form awal = mode tambah
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Setup listener input real-time
    // ─────────────────────────────────────────────────────────────────
    private void setupListeners() {
        // Nominal: hanya angka (tidak boleh desimal — sesuai MONEY / setDouble)
        txtNominalDenda.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9]", "");
            if (!filtered.equals(newVal)) txtNominalDenda.setText(filtered);
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Setup kolom tabel
    // ─────────────────────────────────────────────────────────────────
    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdDenda()));
        coljenispelanggaran.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getJenisPelanggaran()));
        colnominal.setCellValueFactory(d ->
                new SimpleStringProperty("Rp " + FMT_RUPIAH.format((long) d.getValue().getNominalDenda())));
        colketerangan.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getKeterangan() == null ? "-" : d.getValue().getKeterangan()));
        colstatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStsDenda()));

        // Badge status: Aktif = hijau, Tidak Aktif = merah
        colstatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                badge.setStyle("Aktif".equalsIgnoreCase(val)
                        ? "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;"
                        : "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;");
                setGraphic(badge);
                setText(null);
            }
        });

        // ID biru
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

    // ─────────────────────────────────────────────────────────────────
    //  Load data
    // ─────────────────────────────────────────────────────────────────
    private void loadData() {
        try {
            List<Denda> list = CRUD_Denda.getAll();
            masterList.setAll(list);
            currentPage = 1;
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Koneksi",
                    "Gagal memuat data.\nDetail: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Refresh tabel + pagination
    // ─────────────────────────────────────────────────────────────────
    private void refreshTable() {
        int total = masterList.size();
        totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage;
        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        tabelDenda.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    // ─────────────────────────────────────────────────────────────────
    //  Auto generate ID → DN001, DN002, ...
    // ─────────────────────────────────────────────────────────────────
    private void autoGenerateId() {
        try {
            txtIdDenda.setText(CRUD_Denda.generateNextId());
        } catch (Exception e) {
            txtIdDenda.setText("DN001");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Klik baris tabel → isi form
    //  Jika status 'Tidak Aktif' → semua field di-lock
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onTableClick() {
        Denda d = tabelDenda.getSelectionModel().getSelectedItem();
        if (d == null) return;

        txtIdDenda.setText(d.getIdDenda());
        txtJenisPelannggaran.setText(d.getJenisPelanggaran());
        txtNominalDenda.setText(String.valueOf((long) d.getNominalDenda()));
        txtKeterangan.setText(d.getKeterangan() == null ? "" : d.getKeterangan());
        txtstatus.setText(d.getStsDenda()); // tampil saja, tetap read-only

        boolean isTidakAktif = "Tidak Aktif".equalsIgnoreCase(
                d.getStsDenda() != null ? d.getStsDenda().trim() : ""
        );
        setFormState(true, isTidakAktif);
    }

    // ─────────────────────────────────────────────────────────────────
    //  TAMBAH
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void ontambah() {
        bersihForm();
        setFormState(false, false);
        autoGenerateId();
        txtstatus.setText("Aktif"); // status data baru selalu Aktif
        txtJenisPelannggaran.requestFocus();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SIMPAN — insert (status selalu Aktif dari SP)
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onsimpan() {
        if (!validasi()) return;
        try {
            Denda d = new Denda(
                    txtIdDenda.getText().trim(),
                    txtJenisPelannggaran.getText().trim(),
                    Double.parseDouble(txtNominalDenda.getText().trim()),
                    txtKeterangan.getText().trim().isEmpty() ? null
                            : txtKeterangan.getText().trim()
            );
            CRUD_Denda.insert(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data denda berhasil disimpan.");
            loadData();
            ontambah();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  UBAH — update (hanya bisa jika status Aktif, status TIDAK ikut diubah)
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onubah() {
        if (!validasi()) return;
        try {
            Denda d = new Denda(
                    txtIdDenda.getText().trim(),
                    txtJenisPelannggaran.getText().trim(),
                    Double.parseDouble(txtNominalDenda.getText().trim()),
                    txtKeterangan.getText().trim().isEmpty() ? null
                            : txtKeterangan.getText().trim()
            );
            CRUD_Denda.update(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data denda berhasil diubah.");
            loadData();
            onbersih();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", "Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HAPUS — soft delete → Sts_Denda otomatis jadi 'Tidak Aktif' (sesuai SP)
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onhapus() {
        String id = txtIdDenda.getText().trim();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data denda yang ingin dinonaktifkan.");
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Nonaktifkan");
        konfirmasi.setHeaderText("Nonaktifkan Denda");
        konfirmasi.setContentText("Denda [" + id + "] akan diubah statusnya menjadi Tidak Aktif.\nLanjutkan?");
        if (txtIdDenda.getScene() != null)
            konfirmasi.initOwner(txtIdDenda.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_Denda.delete(id); // SP wajib men-set Sts_Denda = 'Tidak Aktif'
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Denda berhasil dinonaktifkan.");
                loadData();
                onbersih();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Error: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  BATAL
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onbersih() {
        bersihForm();
        setFormState(false, false);
        tabelDenda.getSelectionModel().clearSelection();
        autoGenerateId();
        txtstatus.setText("Aktif"); // balik ke kondisi default mode tambah
    }

    // ─────────────────────────────────────────────────────────────────
    //  CARI — berdasarkan ID, Jenis Pelanggaran, ATAU Status
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void oncari() {
        String kw = txtcari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Denda> all = CRUD_Denda.getAll();
            String kwLower = kw.toLowerCase();

            List<Denda> hasil = all.stream()
                    .filter(d ->
                            (d.getIdDenda() != null
                                    && d.getIdDenda().toLowerCase().contains(kwLower))
                                    || (d.getJenisPelanggaran() != null
                                    && d.getJenisPelanggaran().toLowerCase().contains(kwLower))
                                    || (d.getStsDenda() != null
                                    && d.getStsDenda().toLowerCase().contains(kwLower))
                    )
                    .collect(Collectors.toList());

            masterList.setAll(hasil);
            currentPage = 1;
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Cari", "Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Pagination
    // ─────────────────────────────────────────────────────────────────
    @FXML private void onFirstPage() { currentPage = 1;           refreshTable(); }
    @FXML private void onPrevPage()  { if (currentPage > 1)        { currentPage--; refreshTable(); } }
    @FXML private void onNextPage()  { if (currentPage < totalPage) { currentPage++; refreshTable(); } }
    @FXML private void onLastPage()  { currentPage = totalPage;    refreshTable(); }

    // ─────────────────────────────────────────────────────────────────
    //  setFormState
    //  editMode=true  → data dipilih dari tabel (mode edit)
    //  locked=true    → status Tidak Aktif, semua field di-lock
    //  Catatan: txtstatus TIDAK diatur di sini — selalu non-editable
    //           sejak initialize(), tidak peduli editMode/locked.
    // ─────────────────────────────────────────────────────────────────
    private void setFormState(boolean editMode, boolean locked) {
        btnSimpan.setDisable(editMode || locked);
        btnUbah.setDisable(!editMode || locked);
        btnHapus.setDisable(!editMode || locked);

        txtJenisPelannggaran.setEditable(!locked);
        txtNominalDenda.setEditable(!locked);
        txtKeterangan.setEditable(!locked);

        txtJenisPelannggaran.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtNominalDenda.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtKeterangan.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
    }

    // ─────────────────────────────────────────────────────────────────
    //  validasi input form
    // ─────────────────────────────────────────────────────────────────
    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        // Jenis Pelanggaran
        String jenis = txtJenisPelannggaran.getText().trim();
        if (jenis.isEmpty()) {
            sb.append("• Jenis Pelanggaran wajib diisi.\n");
        } else if (jenis.length() < 3) {
            sb.append("• Jenis Pelanggaran minimal 3 karakter.\n");
        } else if (jenis.length() > 100) {
            sb.append("• Jenis Pelanggaran maksimal 100 karakter.\n");
        }

        // Nominal Denda
        String nominalStr = txtNominalDenda.getText().trim();
        if (nominalStr.isEmpty()) {
            sb.append("• Nominal Denda wajib diisi.\n");
        } else {
            try {
                long nominal = Long.parseLong(nominalStr);
                if (nominal <= 0)
                    sb.append("• Nominal Denda harus lebih dari 0.\n");
                if (nominal > 99_999_999L)
                    sb.append("• Nominal Denda maksimal Rp 99.999.999.\n");
            } catch (NumberFormatException e) {
                sb.append("• Nominal Denda harus berupa angka bulat.\n");
            }
        }

        // Keterangan (opsional, jika diisi min 5 karakter)
        String ket = txtKeterangan.getText().trim();
        if (!ket.isEmpty() && ket.length() < 5) {
            sb.append("• Keterangan minimal 5 karakter jika diisi.\n");
        }

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────
    //  bersihForm
    // ─────────────────────────────────────────────────────────────────
    private void bersihForm() {
        txtIdDenda.clear();
        txtJenisPelannggaran.clear();
        txtNominalDenda.clear();
        txtKeterangan.clear();
        txtstatus.clear();
    }

    // ─────────────────────────────────────────────────────────────────
    //  showAlert — thread-safe
    // ─────────────────────────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (txtIdDenda != null && txtIdDenda.getScene() != null)
                alert.initOwner(txtIdDenda.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }
}