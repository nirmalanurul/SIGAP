package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.APP.CRUD_Karyawan;

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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class KaryawanController implements Initializable {
    @FXML
    private Button btnHapus;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUbah;
    @FXML
    private Button btnUbahPassword;
    @FXML
    private Button btnPilihFotoKtp;
    @FXML
    private ComboBox<String> cmbJabatan;
    @FXML
    private TableColumn<Karyawan, String> colEmail;
    @FXML
    private TableColumn<Karyawan, String> colId;
    @FXML
    private TableColumn<Karyawan, String> colJabatan;
    @FXML
    private TableColumn<Karyawan, String> colNama;
    @FXML
    private TableColumn<Karyawan, String> colNoTelp;
    @FXML
    private TableColumn<Karyawan, String> colStatus;
    @FXML
    private TableColumn<Karyawan, String> colUsername;
    @FXML
    private TableColumn<Karyawan, Void> colFoto;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblPassword;
    @FXML
    private Label lblFotoKtpNama;
    @FXML
    private TableView<Karyawan> tabelKaryawan;
    @FXML
    private TextField txtCari;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtIdKaryawan;
    @FXML
    private TextField txtNamaKaryawan;
    @FXML
    private TextField txtNoTelp;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtStatus;
    @FXML
    private TextField txtUsername;

    private final ObservableList<Karyawan> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    private static final String FOLDER_FOTO_KTP =
            new File("uploads" + File.separator + "ktp").getAbsolutePath() + File.separator;
    private String fotoKtpPath;

    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;-fx-text-fill:#888;";
    private static final String STYLE_NORMAL =
            "-fx-background-color:WHITE;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdKaryawan.setEditable(false);
        txtStatus.setEditable(false);
        txtStatus.setStyle(STYLE_READONLY);

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

    private void showPasswordField(boolean visible) {
        lblPassword.setVisible(visible);
        lblPassword.setManaged(visible);
        txtPassword.setVisible(visible);
        txtPassword.setManaged(visible);
        txtPassword.clear();
    }

    private void setupListeners() {
        txtNoTelp.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9+]", "");
            if (!filtered.equals(newVal)) txtNoTelp.setText(filtered);
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getIdKaryawan()));
        colNama.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getNamaKaryawan()));
        colJabatan.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getJabatanKaryawan()));
        colNoTelp.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getNoTelp()));
        colEmail.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getEmail()));
        colUsername.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getUsername()));
        colStatus.setCellValueFactory(k -> new SimpleStringProperty(k.getValue().getStsKaryawan()));

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

        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); setStyle(""); return; }
                setText(id);
                setStyle("-fx-text-fill:#1A3A8F;-fx-font-weight:600;");
            }
        });

        colFoto.setCellFactory(col -> new TableCell<>() {
            private final Button btnLihat = new Button("Lihat Gambar");
            {
                btnLihat.setStyle(
                        "-fx-background-color:#1A3A8F;-fx-text-fill:WHITE;-fx-font-size:11px;" +
                                "-fx-font-weight:700;-fx-background-radius:5;-fx-cursor:hand;-fx-padding:4 10;");
                btnLihat.setOnAction(e -> {
                    Karyawan k = getTableView().getItems().get(getIndex());
                    tampilkanFotoKtp(k.getFotoKtp());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnLihat);
            }
        });
    }

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

    private void refreshTable() {
        int total = masterList.size();
        totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage;
        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        tabelKaryawan.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtIdKaryawan.setText(CRUD_Karyawan.generateNextId());
        } catch (Exception e) {
            txtIdKaryawan.setText("KR001");
        }
    }

    private void setFormState(boolean editMode, boolean locked) {
        btnSimpan.setDisable(editMode || locked);
        btnUbah.setDisable(!editMode || locked);
        btnHapus.setDisable(!editMode || locked);
        btnUbahPassword.setDisable(!editMode || locked);

        txtNamaKaryawan.setEditable(!locked);
        cmbJabatan.setDisable(locked);
        txtNoTelp.setEditable(!locked);
        txtEmail.setEditable(!locked);
        txtUsername.setEditable(!locked);

        btnPilihFotoKtp.setDisable(locked);

        txtNamaKaryawan.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtNoTelp.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtEmail.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtUsername.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
    }

    private boolean validasi(boolean isInsert) {
        StringBuilder sb = new StringBuilder();

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

        if (cmbJabatan.getValue() == null || cmbJabatan.getValue().isEmpty()) {
            sb.append("• Jabatan wajib dipilih (Admin / Kasir / Manajer).\n");
        }

        String telp = txtNoTelp.getText().trim();
        if (telp.isEmpty()) {
            sb.append("• No. Telepon wajib diisi.\n");
        } else if (!telp.matches("(08[0-9].{7,12}|\\+62[0-9].{7,12})")) {
            sb.append("• No. Telepon harus diawali 08 atau +62 dan panjang 10–15 karakter.\n");
        }

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

        String password = txtPassword.getText().trim();
        if (isInsert && password.isEmpty()) {
            sb.append("• Password wajib diisi.\n");
        } else if (!password.isEmpty() && password.length() < 8) {
            sb.append("• Password minimal 8 karakter.\n");
        } else if (!password.isEmpty() && password.length() > 30) {
            sb.append("• Password maksimal 30 karakter.\n");
        }

        if (isInsert && (fotoKtpPath == null || fotoKtpPath.isBlank())) {
            sb.append("• Foto KTP wajib diunggah.\n");
        }

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return false;
        }
        return true;
    }

    private void bersihForm() {
        txtIdKaryawan.clear();
        txtNamaKaryawan.clear();
        cmbJabatan.setValue(null);
        txtNoTelp.clear();
        txtEmail.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtStatus.clear();
        fotoKtpPath = null;
        lblFotoKtpNama.setText("Belum ada gambar dipilih");
    }

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

    private void tampilkanFotoKtp(String pathFoto) {
        if (pathFoto == null || pathFoto.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Gambar", "Data karyawan ini belum memiliki foto KTP.");
            return;
        }
        File file = new File(pathFoto);
        if (!file.exists()) {
            showAlert(Alert.AlertType.ERROR, "Gambar Tidak Ditemukan",
                    "File gambar tidak ditemukan pada path:\n" + pathFoto);
            return;
        }
        try {
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(480);

            VBox box = new VBox(imageView);
            box.setStyle("-fx-padding:16;-fx-background-color:WHITE;-fx-alignment:CENTER;");

            Stage dialog = new Stage();
            dialog.setTitle("Foto KTP Karyawan");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tabelKaryawan.getScene() != null) dialog.initOwner(tabelKaryawan.getScene().getWindow());
            dialog.setScene(new Scene(box));
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Menampilkan Gambar", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onPilihFotoKtp(ActionEvent event) {
        String nama = txtNamaKaryawan.getText().trim();
        if (nama.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan",
                    "Isi Nama Karyawan terlebih dahulu sebelum mengunggah foto KTP.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Foto KTP");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gambar (*.jpg, *.jpeg, *.png)", "*.jpg", "*.jpeg", "*.png")
        );
        File file = chooser.showOpenDialog(btnPilihFotoKtp.getScene().getWindow());
        if (file == null) return;

        try {
            File folder = new File(FOLDER_FOTO_KTP);
            if (!folder.exists()) folder.mkdirs();

            String namaAsli = file.getName();
            String ekstensi = namaAsli.substring(namaAsli.lastIndexOf('.'));
            // Format penamaan: KTP_<Nama Karyawan>.ext
            // Nama file yang sama otomatis menimpa file lama, sehingga
            // jika salah unggah, pengguna cukup unggah ulang untuk memperbaikinya.
            String namaFile = "KTP_" + sanitizeNamaFile(nama) + ekstensi;

            Path target = Paths.get(FOLDER_FOTO_KTP + namaFile).toAbsolutePath().normalize();
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            fotoKtpPath = target.toString();
            lblFotoKtpNama.setText(namaAsli);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Upload", "Gagal menyimpan gambar: " + e.getMessage());
        }
    }

    private String sanitizeNamaFile(String nama) {
        return nama.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9_]", "");
    }

    @FXML
    void onBersih(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        showPasswordField(true);
        tabelKaryawan.getSelectionModel().clearSelection();
        autoGenerateId();
        txtStatus.setText("Aktif");
        txtNamaKaryawan.requestFocus();
    }

    @FXML
    void onTableClick(MouseEvent event) {
        Karyawan k = tabelKaryawan.getSelectionModel().getSelectedItem();
        if (k == null) return;

        txtIdKaryawan.setText(k.getIdKaryawan());
        txtNamaKaryawan.setText(k.getNamaKaryawan());
        cmbJabatan.setValue(k.getJabatanKaryawan());
        txtNoTelp.setText(k.getNoTelp());
        txtEmail.setText(k.getEmail());
        txtUsername.setText(k.getUsername());
        txtStatus.setText(k.getStsKaryawan());

        fotoKtpPath = k.getFotoKtp();
        lblFotoKtpNama.setText(
                (k.getFotoKtp() == null || k.getFotoKtp().isBlank())
                        ? "Belum ada gambar dipilih"
                        : new File(k.getFotoKtp()).getName()
        );

        showPasswordField(false);

        boolean isTidakAktif = "Tidak Aktif".equalsIgnoreCase(
                k.getStsKaryawan() != null ? k.getStsKaryawan().trim() : ""
        );
        setFormState(true, isTidakAktif);
    }

    @FXML
    void onCari(ActionEvent event) {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Karyawan> all = CRUD_Karyawan.getAll();
            String kwLower = kw.toLowerCase();
            List<Karyawan> hasil = all.stream()
                    .filter(k ->
                            (k.getIdKaryawan() != null && k.getIdKaryawan().toLowerCase().contains(kwLower))
                                    || (k.getNamaKaryawan() != null && k.getNamaKaryawan().toLowerCase().contains(kwLower))
                                    || (k.getJabatanKaryawan() != null && k.getJabatanKaryawan().toLowerCase().contains(kwLower))
                                    || (k.getUsername() != null && k.getUsername().toLowerCase().contains(kwLower))
                                    || (k.getStsKaryawan() != null && k.getStsKaryawan().toLowerCase().contains(kwLower))
                    )
                    .collect(Collectors.toList());
            masterList.setAll(hasil);
            currentPage = 1;
            refreshTable();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Cari", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onFirstPage(ActionEvent event) {
        currentPage = 1;
        refreshTable();
    }

    @FXML
    void onHapus(ActionEvent event) {
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
                onBersih(null);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Error: " + e.getMessage());
            }
        }
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

    @FXML
    void onSimpan(ActionEvent event) {
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
            k.setFotoKtp(fotoKtpPath);
            CRUD_Karyawan.insert(k);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data karyawan berhasil disimpan.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onUbah(ActionEvent event) {
        if (!validasi(false)) return;
        try {
            Karyawan k = new Karyawan(
                    txtIdKaryawan.getText().trim(),
                    txtNamaKaryawan.getText().trim(),
                    cmbJabatan.getValue(),
                    txtNoTelp.getText().trim(),
                    txtEmail.getText().trim(),
                    txtUsername.getText().trim(),
                    ""   // password tidak diubah lewat form ini
            );
            k.setFotoKtp(fotoKtpPath);
            CRUD_Karyawan.updateData(k);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data karyawan berhasil diubah.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onUbahPassword(ActionEvent event) {
        String id = txtIdKaryawan.getText().trim();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data karyawan yang ingin diubah password-nya.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/UbahPasswordDialog.fxml"));
            Parent root = loader.load();

            UbahPasswordController controller = loader.getController();
            controller.setData(id, txtNamaKaryawan.getText().trim());

            Stage dialog = new Stage();
            dialog.setTitle("Ubah Password Karyawan");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (txtIdKaryawan.getScene() != null) dialog.initOwner(txtIdKaryawan.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (controller.isBerhasil()) loadData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Dialog", "Error: " + e.getMessage());
        }


    }
}
//tes