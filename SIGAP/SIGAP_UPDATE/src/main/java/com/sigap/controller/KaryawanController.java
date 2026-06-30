package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.APP.CRUD_Karyawan;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class KaryawanController implements Initializable {

    // ── Form fields ───────────────────────────────────────────────────
    @FXML private TextField   txtIdKaryawan;
    @FXML private TextField   txtNamaKaryawan;
    @FXML private ComboBox<String> cmbJabatan;
    @FXML private TextField   txtNoTelp;
    @FXML private TextField   txtEmail;
    @FXML private TextField   txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField   txtStatus;

    // ── Tombol ────────────────────────────────────────────────────────
    @FXML private Button btnSimpan;
    @FXML private Button btnUbah;
    @FXML private Button btnHapus;

    // ── Pencarian ─────────────────────────────────────────────────────
    @FXML private TextField txtCari;

    // ── Tabel ─────────────────────────────────────────────────────────
    @FXML private TableView<Karyawan>           tabelKaryawan;
    @FXML private TableColumn<Karyawan, String> colId;
    @FXML private TableColumn<Karyawan, String> colNama;
    @FXML private TableColumn<Karyawan, String> colJabatan;
    @FXML private TableColumn<Karyawan, String> colNoTelp;
    @FXML private TableColumn<Karyawan, String> colEmail;
    @FXML private TableColumn<Karyawan, String> colUsername;
    @FXML private TableColumn<Karyawan, String> colStatus;

    // ── Label info ────────────────────────────────────────────────────
    @FXML private Label lblTotal;
    @FXML private Label lblPage;

    // ── State ─────────────────────────────────────────────────────────
    private ObservableList<Karyawan> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage   = 1;

    // ── Style konstanta ───────────────────────────────────────────────
    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;-fx-text-fill:#888;";
    private static final String STYLE_NORMAL =
            "-fx-background-color:WHITE;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;";

    // ─────────────────────────────────────────────────────────────────
    //  initialize
    // ─────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ID dan Status tidak bisa diedit user
        txtIdKaryawan.setEditable(false);
        txtStatus.setEditable(false);
        txtStatus.setStyle(STYLE_READONLY);

        // Isi pilihan jabatan sesuai CHECK constraint di DB
        cmbJabatan.setItems(FXCollections.observableArrayList(
                "Admin", "Kasir", "Manajer"
        ));

        setupTable();
        setupListeners();
        setFormState(false, false);

        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
            txtStatus.setText("Aktif");
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Setup listener input real-time
    // ─────────────────────────────────────────────────────────────────
    private void setupListeners() {
        // No Telp: hanya angka dan tanda '+'
        txtNoTelp.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9+]", "");
            if (!filtered.equals(newVal)) txtNoTelp.setText(filtered);
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Setup kolom tabel
    // ─────────────────────────────────────────────────────────────────
    private void setupTable() {
        colId.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getIdKaryawan()));
        colNama.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getNamaKaryawan()));
        colJabatan.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getJabatanKaryawan()));
        colNoTelp.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getNoTelp()));
        colEmail.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getEmail()));
        colUsername.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getUsername()));
        colStatus.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getStsKaryawan()));

        // Badge warna status: Aktif = hijau, Tidak Aktif = merah
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                badge.setStyle("Aktif".equalsIgnoreCase(val)
                        ? "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;-fx-font-weight:700;" +
                        "-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;"
                        : "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;-fx-font-weight:700;" +
                        "-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;");
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
            List<Karyawan> list = CRUD_Karyawan.getAll();
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
        tabelKaryawan.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    // ─────────────────────────────────────────────────────────────────
    //  Auto generate ID → KR001, KR002, ...
    // ─────────────────────────────────────────────────────────────────
    private void autoGenerateId() {
        try {
            txtIdKaryawan.setText(CRUD_Karyawan.generateNextId());
        } catch (Exception e) {
            txtIdKaryawan.setText("KR001");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Klik baris tabel → isi form
    //  Jika status 'Tidak Aktif' → semua field di-lock
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onTableClick() {
        Karyawan k = tabelKaryawan.getSelectionModel().getSelectedItem();
        if (k == null) return;

        txtIdKaryawan.setText(k.getIdKaryawan());
        txtNamaKaryawan.setText(k.getNamaKaryawan());
        cmbJabatan.setValue(k.getJabatanKaryawan());
        txtNoTelp.setText(k.getNoTelp());
        txtEmail.setText(k.getEmail());
        txtUsername.setText(k.getUsername());
        txtPassword.setText("");          // Password tidak ada di view, user perlu re-input
        txtStatus.setText(k.getStsKaryawan());

        boolean isTidakAktif = "Tidak Aktif".equalsIgnoreCase(
                k.getStsKaryawan() != null ? k.getStsKaryawan().trim() : ""
        );
        setFormState(true, isTidakAktif);
    }

    // ─────────────────────────────────────────────────────────────────
    //  TAMBAH
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onTambah() {
        bersihForm();
        setFormState(false, false);
        autoGenerateId();
        txtStatus.setText("Aktif");
        txtNamaKaryawan.requestFocus();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SIMPAN — insert
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onSimpan() {
        if (!validasi(true)) return;
        try {
            Karyawan k = new Karyawan(
                    txtIdKaryawan.getText().trim(),
                    txtNamaKaryawan.getText().trim(),
                    cmbJabatan.getValue(),
                    txtNoTelp.getText().trim(),
                    txtEmail.getText().trim(),
                    txtUsername.getText().trim(),
                    txtPassword.getText().trim()
            );
            CRUD_Karyawan.insert(k);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data karyawan berhasil disimpan.");
            loadData();
            onTambah();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  UBAH — update (hanya jika status Aktif)
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onUbah() {
        if (!validasi(false)) return;
        try {
            Karyawan k = new Karyawan(
                    txtIdKaryawan.getText().trim(),
                    txtNamaKaryawan.getText().trim(),
                    cmbJabatan.getValue(),
                    txtNoTelp.getText().trim(),
                    txtEmail.getText().trim(),
                    txtUsername.getText().trim(),
                    txtPassword.getText().trim()
            );
            CRUD_Karyawan.update(k);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data karyawan berhasil diubah.");
            loadData();
            onBersih();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", "Error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HAPUS — soft delete → Sts_Karyawan = 'Tidak Aktif'
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onHapus() {
        String id = txtIdKaryawan.getText().trim();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data karyawan yang ingin dinonaktifkan.");
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Nonaktifkan");
        konfirmasi.setHeaderText("Nonaktifkan Karyawan");
        konfirmasi.setContentText("Karyawan [" + id + "] akan diubah statusnya menjadi Tidak Aktif.\nLanjutkan?");
        if (txtIdKaryawan.getScene() != null)
            konfirmasi.initOwner(txtIdKaryawan.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_Karyawan.delete(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Karyawan berhasil dinonaktifkan.");
                loadData();
                onBersih();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Error: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  BATAL
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onBersih() {
        bersihForm();
        setFormState(false, false);
        tabelKaryawan.getSelectionModel().clearSelection();
        autoGenerateId();
        txtStatus.setText("Aktif");
    }

    // ─────────────────────────────────────────────────────────────────
    //  CARI
    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void onCari() {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Karyawan> all = CRUD_Karyawan.getAll();
            String kwLower = kw.toLowerCase();

            List<Karyawan> hasil = all.stream()
                    .filter(k ->
                            (k.getIdKaryawan()      != null && k.getIdKaryawan().toLowerCase().contains(kwLower))
                                    || (k.getNamaKaryawan()    != null && k.getNamaKaryawan().toLowerCase().contains(kwLower))
                                    || (k.getJabatanKaryawan() != null && k.getJabatanKaryawan().toLowerCase().contains(kwLower))
                                    || (k.getUsername()        != null && k.getUsername().toLowerCase().contains(kwLower))
                                    || (k.getStsKaryawan()     != null && k.getStsKaryawan().toLowerCase().contains(kwLower))
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
    @FXML private void onFirstPage() { currentPage = 1;            refreshTable(); }
    @FXML private void onPrevPage()  { if (currentPage > 1)         { currentPage--; refreshTable(); } }
    @FXML private void onNextPage()  { if (currentPage < totalPage)  { currentPage++; refreshTable(); } }
    @FXML private void onLastPage()  { currentPage = totalPage;     refreshTable(); }

    // ─────────────────────────────────────────────────────────────────
    //  setFormState
    //  editMode=true  → mode edit (row dipilih dari tabel)
    //  locked=true    → status Tidak Aktif, semua field di-lock
    // ─────────────────────────────────────────────────────────────────
    private void setFormState(boolean editMode, boolean locked) {
        btnSimpan.setDisable(editMode || locked);
        btnUbah.setDisable(!editMode || locked);
        btnHapus.setDisable(!editMode || locked);

        txtNamaKaryawan.setEditable(!locked);
        cmbJabatan.setDisable(locked);
        txtNoTelp.setEditable(!locked);
        txtEmail.setEditable(!locked);
        txtUsername.setEditable(!locked);
        txtPassword.setEditable(!locked);

        txtNamaKaryawan.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtNoTelp.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtEmail.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtUsername.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtPassword.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Validasi input form
    //  isInsert=true → password wajib diisi; false → boleh kosong (tidak ganti password)
    // ─────────────────────────────────────────────────────────────────
    private boolean validasi(boolean isInsert) {
        StringBuilder sb = new StringBuilder();

        // Nama Karyawan — min 3 karakter, hanya huruf dan spasi
        String nama = txtNamaKaryawan.getText().trim();
        if (nama.isEmpty()) {
            sb.append("• Nama Karyawan wajib diisi.\n");
        } else if (nama.length() < 3) {
            sb.append("• Nama Karyawan minimal 3 karakter.\n");
        } else if (nama.length() > 30) {
            sb.append("• Nama Karyawan maksimal 30 karakter.\n");
        } else if (!nama.matches("[A-Za-z ]+")) {
            sb.append("• Nama Karyawan hanya boleh huruf dan spasi (tanpa angka/simbol).\n");
        }

        // Jabatan — wajib dipilih
        if (cmbJabatan.getValue() == null || cmbJabatan.getValue().isEmpty()) {
            sb.append("• Jabatan wajib dipilih (Admin / Kasir / Manajer).\n");
        }

        // No Telepon — diawali 08 atau +62, panjang 10–15 digit
        String telp = txtNoTelp.getText().trim();
        if (telp.isEmpty()) {
            sb.append("• No. Telepon wajib diisi.\n");
        } else if (!telp.matches("(08[0-9].{7,12}|\\+62[0-9].{7,12})")) {
            sb.append("• No. Telepon harus diawali 08 atau +62 dan panjang 10–15 karakter.\n");
        }

        // Email — format sederhana, min 8 karakter, mengandung @
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            sb.append("• Email wajib diisi.\n");
        } else if (email.length() < 8) {
            sb.append("• Email minimal 8 karakter.\n");
        } else if (email.length() > 30) {
            sb.append("• Email maksimal 30 karakter.\n");
        } else if (!email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            sb.append("• Format Email tidak valid (contoh: nama@email.com).\n");
        }

        // Username — min 5 karakter, tanpa spasi
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            sb.append("• Username wajib diisi.\n");
        } else if (username.length() < 5) {
            sb.append("• Username minimal 5 karakter.\n");
        } else if (username.length() > 30) {
            sb.append("• Username maksimal 30 karakter.\n");
        } else if (username.contains(" ")) {
            sb.append("• Username tidak boleh mengandung spasi.\n");
        }

        // Password — min 8 karakter (wajib saat insert, opsional saat update)
        String password = txtPassword.getText().trim();
        if (isInsert && password.isEmpty()) {
            sb.append("• Password wajib diisi.\n");
        } else if (!password.isEmpty() && password.length() < 8) {
            sb.append("• Password minimal 8 karakter.\n");
        } else if (!password.isEmpty() && password.length() > 30) {
            sb.append("• Password maksimal 30 karakter.\n");
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
        txtIdKaryawan.clear();
        txtNamaKaryawan.clear();
        cmbJabatan.setValue(null);
        txtNoTelp.clear();
        txtEmail.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtStatus.clear();
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
            if (txtIdKaryawan != null && txtIdKaryawan.getScene() != null)
                alert.initOwner(txtIdKaryawan.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }
}