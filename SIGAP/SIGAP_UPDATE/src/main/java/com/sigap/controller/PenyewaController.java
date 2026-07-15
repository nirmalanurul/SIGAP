package com.sigap.controller;

import com.sigap.ADT.Penyewa;
import com.sigap.APP.CRUD_Penyewa;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.scene.control.TextArea;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class PenyewaController implements Initializable {

    @FXML
    private Button btnHapus;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUbah;
    @FXML
    private Button btnPilihFotoKtp;
    @FXML
    private TableColumn<Penyewa, String> colAlamat;
    @FXML
    private TableColumn<Penyewa, String> colId;
    @FXML
    private TableColumn<Penyewa, String> colNIK;
    @FXML
    private TableColumn<Penyewa, String> colNama;
    @FXML
    private TableColumn<Penyewa, String> colNoTelp;
    @FXML
    private TableColumn<Penyewa, String> colStatus;
    @FXML
    private TableColumn<Penyewa, String> colTgl;
    @FXML
    private TableColumn<Penyewa, Penyewa> colFoto;
    @FXML
    private DatePicker dpTglDaftar;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblFotoKtpNama;
    @FXML
    private TableView<Penyewa> tabelPenyewa;
    @FXML
    private TextArea txtAlamat;
    @FXML
    private TextField txtCari;
    @FXML
    private TextField txtIdPenyewa;
    @FXML
    private TextField txtNIK;
    @FXML
    private TextField txtNama;
    @FXML
    private TextField txtNoTelp;
    @FXML
    private TextField txtStatus;

    private ObservableList<Penyewa> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    // Formatter tanggal dengan nama bulan berbahasa Indonesia, contoh: 14 Juli 2026
    private static final DateTimeFormatter FORMAT_TGL =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));

    private static final String FOLDER_FOTO_KTP =
            new File("uploads" + File.separator + "ktp_penyewa").getAbsolutePath() + File.separator;
    private String fotoKtpPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtStatus.setEditable(false);
        txtStatus.setText("Aktif");

        setupTable();
        setupListeners();
        setFormState(false, false);
        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
        });
    }

    private void setupListeners() {
        txtNIK.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9]", "");
            if (filtered.length() > 16) filtered = filtered.substring(0, 16);
            if (!filtered.equals(newVal)) txtNIK.setText(filtered);
        });

        txtNoTelp.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) return;
            String filtered;
            if (newVal.startsWith("+")) {
                filtered = "+" + newVal.substring(1).replaceAll("[^0-9]", "");
            } else {
                filtered = newVal.replaceAll("[^0-9]", "");
            }
            if (filtered.length() > 15) filtered = filtered.substring(0, 15);
            if (!filtered.equals(newVal)) txtNoTelp.setText(filtered);
        });

        dpTglDaftar.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty
                        || date.isAfter(LocalDate.now())
                        || date.isBefore(LocalDate.of(2000, 1, 1)));
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue().getIdPenyewa()));
        colNama.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().getNamaPenyewa()));
        colNIK.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue().getNik()));
        colNoTelp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNoTelp()));
        colAlamat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlamat()));
        colTgl.setCellValueFactory(d    -> new SimpleStringProperty(
                d.getValue().getTglDaftar() == null ? "" :
                        d.getValue().getTglDaftar().format(FORMAT_TGL)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStsPenyewa()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle("Aktif".equalsIgnoreCase(status)
                        ? "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;"
                        : "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;");
                setGraphic(badge);
                setText(null);
            }
        });

        colNama.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String nama, boolean empty) {
                super.updateItem(nama, empty);
                if (empty || nama == null) { setText(null); setStyle(""); return; }
                setText(nama);
                setStyle("-fx-text-fill:#1A3A8F;-fx-font-weight:600;");
            }
        });

        // Kolom Foto KTP: tombol "Lihat Gambar" di setiap baris
        colFoto.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        colFoto.setCellFactory(col -> new TableCell<>() {
            private static final String STYLE_NORMAL =
                    "-fx-background-color: linear-gradient(to bottom, #2647B8, #1A3A8F);" +
                            "-fx-text-fill:WHITE;-fx-font-size:11px;-fx-font-weight:700;" +
                            "-fx-background-radius:20;-fx-cursor:hand;-fx-padding:6 14 6 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(26,58,143,0.35), 6, 0, 0, 2);";
            private static final String STYLE_HOVER =
                    "-fx-background-color: linear-gradient(to bottom, #3355CC, #1F45A8);" +
                            "-fx-text-fill:WHITE;-fx-font-size:11px;-fx-font-weight:700;" +
                            "-fx-background-radius:20;-fx-cursor:hand;-fx-padding:6 14 6 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(26,58,143,0.55), 9, 0, 0, 3);" +
                            "-fx-scale-x:1.05;-fx-scale-y:1.05;";

            private final Button btnLihat = new Button("\uD83D\uDDBC  Lihat Gambar");
            {
                btnLihat.setStyle(STYLE_NORMAL);
                btnLihat.setOnMouseEntered(e -> btnLihat.setStyle(STYLE_HOVER));
                btnLihat.setOnMouseExited(e -> btnLihat.setStyle(STYLE_NORMAL));
                btnLihat.setOnAction(e -> {
                    Penyewa p = getItem();
                    if (p != null) tampilkanFotoKtp(p.getFotoKtp());
                });
            }

            @Override
            protected void updateItem(Penyewa item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic((empty || item == null) ? null : btnLihat);
            }
        });
    }

    private void loadData() {
        try {
            List<Penyewa> list = CRUD_Penyewa.getAll();
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
        tabelPenyewa.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtIdPenyewa.setText(CRUD_Penyewa.generateNextId());
        } catch (Exception e) {
            txtIdPenyewa.setText("PW001");
        }
    }

    private void setFormState(boolean editMode, boolean isNonaktif) {
        btnSimpan.setDisable(editMode || isNonaktif);
        btnUbah.setDisable(!editMode || isNonaktif);
        btnHapus.setDisable(!editMode || isNonaktif);
        btnPilihFotoKtp.setDisable(isNonaktif);

        txtNama.setEditable(!isNonaktif);
        txtNIK.setEditable(!isNonaktif);
        txtNoTelp.setEditable(!isNonaktif);
        txtAlamat.setEditable(!isNonaktif);
        dpTglDaftar.setDisable(isNonaktif);

        String styleReadOnly = "-fx-background-color: #F0F0F0; -fx-border-color: #D0D8E8; "
                + "-fx-border-radius: 6; -fx-background-radius: 6; "
                + "-fx-padding: 6 12; -fx-font-size: 13px; -fx-text-fill: #888;";
        String styleNormal = "-fx-background-color: WHITE; -fx-border-color: #D0D8E8; "
                + "-fx-border-radius: 6; -fx-background-radius: 6; "
                + "-fx-padding: 6 12; -fx-font-size: 13px;";

        txtNama.setStyle(isNonaktif ? styleReadOnly : styleNormal);
        txtNIK.setStyle(isNonaktif ? styleReadOnly : styleNormal);
        txtNoTelp.setStyle(isNonaktif ? styleReadOnly : styleNormal);
        txtAlamat.setStyle(isNonaktif ? styleReadOnly : styleNormal);
    }

    private String pesanErrorRamah(Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

        if (msg.contains("uq_nik") || (msg.contains("unique") && msg.contains("nik"))) {
            return "NIK sudah terdaftar atas nama penyewa lain. Silakan periksa kembali data Anda.";
        }
        if (msg.contains("unique") || msg.contains("duplicate key")) {
            return "Data yang Anda masukkan sudah terdaftar sebelumnya. Silakan periksa kembali data Anda.";
        }
        if (msg.contains("foreign key") || msg.contains("reference constraint")) {
            return "Data ini masih terhubung dengan data lain sehingga tidak dapat diproses.";
        }
        if (msg.contains("connection") || msg.contains("timeout")) {
            return "Koneksi ke database bermasalah. Silakan periksa jaringan Anda dan coba lagi.";
        }
        return "Terjadi kesalahan saat memproses data. Silakan coba lagi atau hubungi admin sistem.";
    }

    private boolean validasi(boolean isInsert) {
        StringBuilder sb = new StringBuilder();

        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            sb.append("• Nama Penyewa wajib diisi.\n");
        } else if (nama.length() < 3) {
            sb.append("• Nama Penyewa minimal 3 karakter.\n");
        } else if (nama.length() > 30) {
            sb.append("• Nama Penyewa maksimal 30 karakter.\n");
        } else if (!nama.matches("^[A-Za-z\\s]+$")) {
            sb.append("• Nama Penyewa hanya boleh mengandung huruf dan spasi.\n");
        }

        String nik = txtNIK.getText().trim();
        if (nik.isEmpty()) {
            sb.append("• NIK wajib diisi.\n");
        } else if (!nik.matches("^[0-9]{16}$")) {
            sb.append("• NIK harus terdiri dari tepat 16 digit angka.\n");
        }

        String noTelp = txtNoTelp.getText().trim();
        if (noTelp.isEmpty()) {
            sb.append("• No Telp wajib diisi.\n");
        } else if (!noTelp.matches("^(08|\\+62)[0-9]{8,13}$")) {
            sb.append("• No Telp harus diawali 08 atau +62 dan terdiri dari 10–15 digit angka.\n");
        }

        String alamat = txtAlamat.getText().trim();
        if (alamat.isEmpty()) {
            sb.append("• Alamat wajib diisi.\n");
        } else if (alamat.length() < 10) {
            sb.append("• Alamat minimal 10 karakter.\n");
        } else if (alamat.length() > 50) {
            sb.append("• Alamat maksimal 50 karakter.\n");
        } else if (!alamat.matches("^[A-Za-z0-9\\s,./\\-]+$")) {
            sb.append("• Alamat hanya boleh mengandung huruf, angka, spasi, koma, titik, garis miring, dan tanda hubung.\n");
        }

        if (dpTglDaftar.getValue() == null) {
            sb.append("• Tanggal Daftar wajib diisi.\n");
        } else if (dpTglDaftar.getValue().isAfter(LocalDate.now())) {
            sb.append("• Tanggal Daftar tidak boleh melebihi tanggal hari ini.\n");
        } else if (dpTglDaftar.getValue().isBefore(LocalDate.of(2000, 1, 1))) {
            sb.append("• Tanggal Daftar tidak boleh sebelum tahun 2000.\n");
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
        txtIdPenyewa.clear();
        txtNama.clear();
        txtNIK.clear();
        txtNoTelp.clear();
        txtAlamat.clear();
        dpTglDaftar.setValue(LocalDate.now());
        txtStatus.setText("Aktif");
        fotoKtpPath = null;
        lblFotoKtpNama.setText("Belum ada gambar dipilih");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (txtIdPenyewa != null && txtIdPenyewa.getScene() != null)
                alert.initOwner(txtIdPenyewa.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    private void tampilkanFotoKtp(String pathFoto) {
        if (pathFoto == null || pathFoto.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Gambar", "Data penyewa ini belum memiliki foto KTP.");
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
            dialog.setTitle("Foto KTP Penyewa");
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tabelPenyewa.getScene() != null) dialog.initOwner(tabelPenyewa.getScene().getWindow());
            dialog.setScene(new Scene(box));
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Menampilkan Gambar", "Error: " + e.getMessage());
        }
    }

    private String sanitizeNamaFile(String nama) {
        return nama.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9_]", "");
    }

    @FXML
    void onPilihFotoKtp(ActionEvent event) {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan",
                    "Isi Nama Penyewa terlebih dahulu sebelum mengunggah foto KTP.");
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
            String namaFile = "KTP_" + sanitizeNamaFile(nama) + ekstensi;

            Path target = Paths.get(FOLDER_FOTO_KTP + namaFile).toAbsolutePath().normalize();
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            fotoKtpPath = target.toString();
            lblFotoKtpNama.setText(namaAsli);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Upload", "Gagal menyimpan gambar: " + e.getMessage());
        }
    }

    @FXML
    void onBersih(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        tabelPenyewa.getSelectionModel().clearSelection();
        autoGenerateId();
    }

    @FXML
    void onCari(ActionEvent event) {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Penyewa> hasil = CRUD_Penyewa.search(kw);
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
        String id = txtIdPenyewa.getText().trim();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data yang ingin dinonaktifkan.");
            return;
        }

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Nonaktifkan");
        konfirmasi.setHeaderText("Nonaktifkan Penyewa");
        konfirmasi.setContentText("Penyewa [" + id + "] akan diubah statusnya menjadi Tidak Aktif.\nLanjutkan?");
        if (txtIdPenyewa.getScene() != null)
            konfirmasi.initOwner(txtIdPenyewa.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_Penyewa.delete(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Penyewa berhasil dinonaktifkan.");
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
            Penyewa p = new Penyewa(
                    txtIdPenyewa.getText().trim(),
                    txtNama.getText().trim(),
                    txtNIK.getText().trim(),
                    txtNoTelp.getText().trim(),
                    txtAlamat.getText().trim(),
                    dpTglDaftar.getValue(),
                    "Aktif"
            );
            p.setFotoKtp(fotoKtpPath);
            CRUD_Penyewa.insert(p);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data penyewa berhasil disimpan.");
            loadData();
            onTambah(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", pesanErrorRamah(e));
        }
    }

    @FXML
    void onTableClick(MouseEvent event) {
        Penyewa p = tabelPenyewa.getSelectionModel().getSelectedItem();
        if (p == null) return;

        txtIdPenyewa.setText(p.getIdPenyewa());
        txtNama.setText(p.getNamaPenyewa());
        txtNIK.setText(p.getNik());
        txtNoTelp.setText(p.getNoTelp());
        txtAlamat.setText(p.getAlamat());
        dpTglDaftar.setValue(p.getTglDaftar());
        txtStatus.setText(p.getStsPenyewa() != null ? p.getStsPenyewa().trim() : "Aktif");

        fotoKtpPath = p.getFotoKtp();
        lblFotoKtpNama.setText(
                (p.getFotoKtp() == null || p.getFotoKtp().isBlank())
                        ? "Belum ada gambar dipilih"
                        : new File(p.getFotoKtp()).getName()
        );

        boolean isNonaktif = "Tidak Aktif".equalsIgnoreCase(
                p.getStsPenyewa() != null ? p.getStsPenyewa().trim() : ""
        );
        setFormState(true, isNonaktif);
    }

    @FXML
    void onTambah(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        autoGenerateId();
        txtNama.requestFocus();
    }

    @FXML
    void onUbah(ActionEvent event) {
        if (!validasi(false)) return;
        try {
            Penyewa p = new Penyewa(
                    txtIdPenyewa.getText().trim(),
                    txtNama.getText().trim(),
                    txtNIK.getText().trim(),
                    txtNoTelp.getText().trim(),
                    txtAlamat.getText().trim(),
                    dpTglDaftar.getValue(),
                    txtStatus.getText()
            );
            p.setFotoKtp(fotoKtpPath);
            CRUD_Penyewa.update(p);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data penyewa berhasil diubah.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", pesanErrorRamah(e));
        }
    }
}