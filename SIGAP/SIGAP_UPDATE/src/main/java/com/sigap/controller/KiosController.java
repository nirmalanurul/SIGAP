package com.sigap.controller;

import com.sigap.ADT.DetailGambarKios;
import com.sigap.ADT.Kios;
import com.sigap.APP.CRUD_Kios;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
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
import java.text.NumberFormat;
import java.util.*;

import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

public class KiosController implements Initializable {

    // 1. FXML FIELDS — FORM INPUT
    @FXML
    private TextField txtIdKios;
    @FXML
    private TextField txtLebar;
    @FXML
    private TextField txtLuas;
    @FXML
    private TextField txtPanjang;
    @FXML
    private TextField txtStsKios;
    @FXML
    private TextArea txtDeskripsi;
    @FXML
    private TextField txtHarga;

    @FXML
    private Button btnHapus;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUbah;

    // 2. FXML FIELDS — FOTO
    @FXML
    private Button btnPilihFotoKios;
    @FXML
    private Label lblJumlahFoto;
    @FXML
    private FlowPane hboxPreviewFoto;

    // 3. FXML FIELDS — PENCARIAN & CARD GRID
    @FXML
    private TextField txtCari;
    @FXML
    private FlowPane flowKios;
    @FXML
    private MenuButton btnFilter;
    @FXML
    private RadioMenuItem rmHargaTermurah;
    @FXML
    private RadioMenuItem rmHargaTermahal;
    @FXML
    private RadioMenuItem rmLuasTerkecil;
    @FXML
    private RadioMenuItem rmLuasTerbesar;
    @FXML
    private RadioMenuItem rmStatusAktif;
    @FXML
    private RadioMenuItem rmStatusNonaktif;

    // 4. FXML FIELDS — PAGINATION
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private Button btnFirstPage;
    @FXML
    private Button btnPrevPage;
    @FXML
    private Button btnNextPage;
    @FXML
    private Button btnLastPage;

    // 5. STATE
    private List<Kios> semuaData = new ArrayList<>();
    private List<Kios> masterList = new ArrayList<>();
    private final List<String> daftarFotoDipilih = new ArrayList<>();

    private Kios selectedKios = null;
    private boolean updatingHarga = false;
    private int currentPage = 1;
    private int totalPage = 1;

    private static final int PAGE_SIZE = 12;
    private static final int MAKS_FOTO = 8;

    private String urutanHarga = null;
    private String urutanLuas = null;
    private String filterStatus = null;

    // 6. KONSTANTA
    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    private static final String FOLDER_FOTO_KIOS =
            new File("uploads" + File.separator + "kios").getAbsolutePath() + File.separator;

    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;-fx-text-fill:#888;";
    private static final String STYLE_NORMAL =
            "-fx-background-color:WHITE;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;";

    // 7. INITIALIZE
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdKios.setEditable(false);
        txtLuas.setEditable(false);
        txtStsKios.setEditable(false);
        txtStsKios.setText("Aktif");

        setupListeners();
        setupFilter();
        setFormState(false, false);

        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
        });
    }

    private void setupFilter() {
        ToggleGroup grupHarga = new ToggleGroup();
        rmHargaTermurah.setToggleGroup(grupHarga);
        rmHargaTermahal.setToggleGroup(grupHarga);

        ToggleGroup grupLuas = new ToggleGroup();
        rmLuasTerkecil.setToggleGroup(grupLuas);
        rmLuasTerbesar.setToggleGroup(grupLuas);

        ToggleGroup grupStatus = new ToggleGroup();
        rmStatusAktif.setToggleGroup(grupStatus);
        rmStatusNonaktif.setToggleGroup(grupStatus);
    }

    // 8. LISTENER INPUT FORM (Panjang, Lebar, Harga)
    private void setupListeners() {
        txtPanjang.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9.]", "");
            long dots = filtered.chars().filter(c -> c == '.').count();
            if (dots > 1) filtered = oldVal;
            if (!filtered.equals(newVal)) txtPanjang.setText(filtered);
            hitungLuas();
        });

        txtLebar.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9.]", "");
            long dots = filtered.chars().filter(c -> c == '.').count();
            if (dots > 1) filtered = oldVal;
            if (!filtered.equals(newVal)) txtLebar.setText(filtered);
            hitungLuas();
        });

        txtHarga.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingHarga) return;
            String digitsOnly = newVal.replaceAll("[^0-9]", "");

            if (digitsOnly.isEmpty()) {
                updatingHarga = true;
                txtHarga.setText("");
                updatingHarga = false;
                return;
            }
            if (digitsOnly.length() > 8) digitsOnly = digitsOnly.substring(0, 8);

            String formatted;
            try {
                long value = Long.parseLong(digitsOnly);
                formatted = FMT_RUPIAH.format(value);
            } catch (NumberFormatException e) {
                formatted = digitsOnly;
            }

            if (!formatted.equals(newVal)) {
                updatingHarga = true;
                txtHarga.setText(formatted);
                txtHarga.positionCaret(formatted.length());
                updatingHarga = false;
            }
        });
    }

    // 9. KALKULASI (Harga, Luas)
    private String rawHarga() {
        return txtHarga.getText().replaceAll("[^0-9]", "");
    }

    private void hitungLuas() {
        try {
            double p = Double.parseDouble(txtPanjang.getText().trim());
            double l = Double.parseDouble(txtLebar.getText().trim());
            double luas = Math.round(p * l * 100.0) / 100.0;
            txtLuas.setText(String.valueOf(luas));
        } catch (NumberFormatException e) {
            txtLuas.setText("");
        }
    }

    private double hitungLuasNilai() {
        double p = Double.parseDouble(txtPanjang.getText().trim());
        double l = Double.parseDouble(txtLebar.getText().trim());
        return Math.round(p * l * 100.0) / 100.0;
    }

    // 10. LOAD DATA & CARD GRID
    private void loadData() {
        try {
            semuaData = CRUD_Kios.getAll();
            masterList = new ArrayList<>(semuaData);
            currentPage = 1;
            refreshGrid();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Koneksi",
                    "Gagal memuat data kios. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    private void refreshGrid() {
        int total = masterList.size();
        totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);

        flowKios.getChildren().clear();
        for (Kios k : masterList.subList(from, to)) {
            try {
                flowKios.getChildren().add(buatCardKios(k));
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Gagal Menampilkan Kios",
                        "Kartu kios [" + k.getIdKios() + "] gagal ditampilkan. Silakan muat ulang halaman.");
            }
        }

        if (total == 0) {
            Label placeholder = new Label("Tidak ada data kios.");
            placeholder.setStyle("-fx-font-size:13px;-fx-text-fill:#AAA;-fx-padding:20;");
            flowKios.getChildren().add(placeholder);
        }

        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    // 11. CARD & DIALOG DETAIL
    private VBox buatCardKios(Kios k) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/KiosCard.fxml"));
        VBox card = loader.load();

        KiosCardController controller = loader.getController();
        controller.setData(k, kiosDiklik -> {
            selectedKios = kiosDiklik;
            isiFormDariKios(kiosDiklik);
            bukaDetailKios(kiosDiklik);
        });

        return card;
    }

    private void bukaDetailKios(Kios k) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigap/view/KiosDetailDialog.fxml"));
            Parent root = loader.load();

            KiosDetailController controller = loader.getController();
            controller.setData(k);

            Stage dialog = new Stage();
            dialog.setTitle("Detail Kios " + k.getIdKios());
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (flowKios.getScene() != null) dialog.initOwner(flowKios.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (controller.isPerluRefresh()) loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Detail",
                    "Detail kios gagal ditampilkan. Silakan coba lagi.");
        }
    }

    // 12. FORM: ISI DATA & RESET
    private void isiFormDariKios(Kios k) {
        txtIdKios.setText(k.getIdKios());
        txtHarga.setText(FMT_RUPIAH.format((long) k.getHargaKios()));
        txtPanjang.setText(String.valueOf(k.getPanjangKios()));
        txtLebar.setText(String.valueOf(k.getLebarKios()));
        txtLuas.setText(String.valueOf(k.getLuasKios()));
        txtDeskripsi.setText(k.getDeskripsi() == null ? "" : k.getDeskripsi());
        txtStsKios.setText(k.getStsKios());

        daftarFotoDipilih.clear();
        try {
            List<DetailGambarKios> fotoLama = CRUD_Kios.getFoto(k.getIdKios());
            for (DetailGambarKios g : fotoLama) daftarFotoDipilih.add(g.getNamaFileGambar());
        } catch (Exception ignored) {}
        refreshPreviewFoto();

        boolean nonaktif = "Nonaktif".equalsIgnoreCase(k.getStsKios());
        setFormState(true, nonaktif);
    }

    private void bersihForm() {
        txtIdKios.clear();
        txtHarga.clear();
        txtPanjang.clear();
        txtLebar.clear();
        txtLuas.clear();
        txtDeskripsi.clear();
        txtStsKios.setText("Aktif");
        daftarFotoDipilih.clear();
        refreshPreviewFoto();
    }

    private void setFormState(boolean editMode, boolean isNonaktif) {
        btnSimpan.setDisable(editMode || isNonaktif);
        btnUbah.setDisable(!editMode || isNonaktif);
        btnHapus.setDisable(!editMode || isNonaktif);

        txtHarga.setEditable(!isNonaktif);
        txtPanjang.setEditable(!isNonaktif);
        txtLebar.setEditable(!isNonaktif);
        txtDeskripsi.setEditable(!isNonaktif);
        btnPilihFotoKios.setDisable(isNonaktif);

        txtHarga.setStyle(isNonaktif ? STYLE_READONLY : STYLE_NORMAL);
        txtPanjang.setStyle(isNonaktif ? STYLE_READONLY : STYLE_NORMAL);
        txtLebar.setStyle(isNonaktif ? STYLE_READONLY : STYLE_NORMAL);
        txtDeskripsi.setStyle(isNonaktif ? STYLE_READONLY : STYLE_NORMAL);
    }

    // 13. UPLOAD & PREVIEW FOTO
    @FXML
    void onPilihFotoKios(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih Foto Kios (boleh lebih dari satu)");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gambar (*.jpg, *.jpeg, *.png)", "*.jpg", "*.jpeg", "*.png")
        );
        List<File> files = chooser.showOpenMultipleDialog(btnPilihFotoKios.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        if (daftarFotoDipilih.size() + files.size() > MAKS_FOTO) {
            int sisaSlot = MAKS_FOTO - daftarFotoDipilih.size();
            showAlert(Alert.AlertType.WARNING, "Batas Foto Terlampaui",
                    "Maksimal " + MAKS_FOTO + " foto per kios.\n" +
                            (sisaSlot > 0
                                    ? "Kamu masih bisa menambahkan " + sisaSlot + " foto lagi."
                                    : "Batas foto sudah tercapai, hapus foto lama untuk menambah yang baru."));
            return;
        }

        try {
            File folder = new File(FOLDER_FOTO_KIOS);
            if (!folder.exists()) folder.mkdirs();

            String idKiosSaatIni = txtIdKios.getText().trim().isEmpty() ? "TEMP" : txtIdKios.getText().trim();

            for (File file : files) {
                String namaAsli = file.getName();
                String ekstensi = namaAsli.substring(namaAsli.lastIndexOf('.'));
                String namaFile = "KIOS_" + idKiosSaatIni + "_" +
                        UUID.randomUUID().toString().substring(0, 8) + ekstensi;

                Path target = Paths.get(FOLDER_FOTO_KIOS + namaFile).toAbsolutePath().normalize();
                Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                daftarFotoDipilih.add(target.toString());
            }
            refreshPreviewFoto();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Upload",
                    "Gagal menyimpan gambar. Pastikan file tidak sedang digunakan aplikasi lain, lalu coba lagi.");
        }
    }

    private void refreshPreviewFoto() {
        lblJumlahFoto.setText(daftarFotoDipilih.size() + " gambar dipilih");
        hboxPreviewFoto.getChildren().clear();

        for (String path : daftarFotoDipilih) {
            File f = new File(path);
            if (!f.exists()) continue;

            ImageView iv = new ImageView(new Image(f.toURI().toString(), 100, 100, true, true));
            iv.setStyle("-fx-background-radius:6;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),3,0,0,1);");

            iv.setOnMouseClicked(e -> {
                daftarFotoDipilih.remove(path);
                refreshPreviewFoto();
            });

            hboxPreviewFoto.getChildren().add(iv);
        }
    }

    // 14. VALIDASI
    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        String hargaStr = rawHarga();
        if (hargaStr.isEmpty()) {
            sb.append("• Harga Kios wajib diisi.\n");
        } else {
            try {
                double harga = Double.parseDouble(hargaStr);
                if (harga < 1) sb.append("• Harga Kios minimal Rp 1.\n");
                if (harga > 99999999) sb.append("• Harga Kios maksimal Rp 99.999.999.\n");
            } catch (NumberFormatException e) {
                sb.append("• Harga Kios harus berupa angka.\n");
            }
        }

        String panjangStr = txtPanjang.getText().trim();
        if (panjangStr.isEmpty()) {
            sb.append("• Panjang Kios wajib diisi.\n");
        } else {
            try {
                double panjang = Double.parseDouble(panjangStr);
                if (panjang < 0.5) sb.append("• Panjang Kios minimal 0.5 meter.\n");
                if (panjang > 99.99) sb.append("• Panjang Kios maksimal 99.99 meter.\n");
            } catch (NumberFormatException e) {
                sb.append("• Panjang Kios harus berupa angka.\n");
            }
        }

        String lebarStr = txtLebar.getText().trim();
        if (lebarStr.isEmpty()) {
            sb.append("• Lebar Kios wajib diisi.\n");
        } else {
            try {
                double lebar = Double.parseDouble(lebarStr);
                if (lebar < 0.5) sb.append("• Lebar Kios minimal 0.5 meter.\n");
                if (lebar > 99.99) sb.append("• Lebar Kios maksimal 99.99 meter.\n");
            } catch (NumberFormatException e) {
                sb.append("• Lebar Kios harus berupa angka.\n");
            }
        }

        String deskripsi = txtDeskripsi.getText().trim();
        if (!deskripsi.isEmpty()) {
            if (deskripsi.length() < 5)
                sb.append("• Deskripsi minimal 5 karakter jika diisi.\n");
            else if (deskripsi.length() > 80)
                sb.append("• Deskripsi maksimal 80 karakter.\n");
            else if (!deskripsi.matches("^[A-Za-z0-9\\s,./\\-]+$"))
                sb.append("• Deskripsi hanya boleh mengandung huruf, angka, spasi, koma, titik, garis miring, dan tanda hubung.\n");
        }

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return false;
        }
        return true;
    }

    private boolean tidakAdaPerubahan(Kios lama, Kios baru, boolean fotoBerubah) {
        if (lama == null) return false;
        String deskLama = lama.getDeskripsi() == null ? "" : lama.getDeskripsi();
        String deskBaru = baru.getDeskripsi() == null ? "" : baru.getDeskripsi();

        boolean dataUtamaSama =
                Double.compare(lama.getHargaKios(), baru.getHargaKios()) == 0
                        && Double.compare(lama.getPanjangKios(), baru.getPanjangKios()) == 0
                        && Double.compare(lama.getLebarKios(), baru.getLebarKios()) == 0
                        && deskLama.equals(deskBaru)
                        && lama.getStsKios().equalsIgnoreCase(baru.getStsKios());

        return dataUtamaSama && !fotoBerubah;
    }

    // 15. UTILITAS (Alert, ID Generator)
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (txtIdKios != null && txtIdKios.getScene() != null)
                alert.initOwner(txtIdKios.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    private void autoGenerateId() {
        try {
            txtIdKios.setText(CRUD_Kios.generateNextId());
        } catch (Exception e) {
            txtIdKios.setText("KS001");
        }
    }

    // 16. EVENT HANDLER — FORM (Simpan, Ubah, Hapus, Bersih)
    @FXML
    void onSimpan(ActionEvent event) {
        if (!validasi()) return;

        if (daftarFotoDipilih.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", "• Minimal 1 foto kios wajib diunggah.\n");
            return;
        }

        try {
            Kios k = new Kios(
                    txtIdKios.getText().trim(),
                    Double.parseDouble(rawHarga()),
                    Double.parseDouble(txtPanjang.getText().trim()),
                    Double.parseDouble(txtLebar.getText().trim()),
                    hitungLuasNilai(),
                    txtDeskripsi.getText().trim().isEmpty() ? null : txtDeskripsi.getText().trim(),
                    "Aktif"
            );

            CRUD_Kios.insert(k, daftarFotoDipilih);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data kios berhasil disimpan.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan",
                    "Data kios gagal disimpan. Pastikan data yang dimasukkan valid, lalu coba lagi.");
        }
    }

    @FXML
    void onUbah(ActionEvent event) {
        if (!validasi()) return;

        if (daftarFotoDipilih.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", "• Kios wajib memiliki minimal 1 foto.\n");
            return;
        }

        try {
            Kios k = new Kios(
                    txtIdKios.getText().trim(),
                    Double.parseDouble(rawHarga()),
                    Double.parseDouble(txtPanjang.getText().trim()),
                    Double.parseDouble(txtLebar.getText().trim()),
                    hitungLuasNilai(),
                    txtDeskripsi.getText().trim().isEmpty() ? null : txtDeskripsi.getText().trim(),
                    txtStsKios.getText()
            );

            boolean fotoBerubah = true;
            try {
                List<DetailGambarKios> fotoLama = CRUD_Kios.getFoto(k.getIdKios());
                List<String> pathLama = new ArrayList<>();
                for (DetailGambarKios g : fotoLama) pathLama.add(g.getNamaFileGambar());
                fotoBerubah = !pathLama.equals(daftarFotoDipilih);
            } catch (Exception ignored) {}

            if (tidakAdaPerubahan(selectedKios, k, fotoBerubah)) {
                showAlert(Alert.AlertType.INFORMATION, "Tidak Ada Perubahan", "Tidak ada perubahan data.");
                return;
            }

            CRUD_Kios.update(k, daftarFotoDipilih);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data kios berhasil diubah.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah",
                    "Data kios gagal diubah. Pastikan data yang dimasukkan valid, lalu coba lagi.");
        }
    }

    @FXML
    void onHapus(ActionEvent event) {
        String id = txtIdKios.getText().trim();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data kios yang ingin dinonaktifkan.");
            return;
        }

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Nonaktifkan");
        konfirmasi.setHeaderText("Nonaktifkan Kios");
        konfirmasi.setContentText("Kios [" + id + "] akan diubah statusnya menjadi Nonaktif.\nLanjutkan?");
        if (txtIdKios.getScene() != null)
            konfirmasi.initOwner(txtIdKios.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_Kios.delete(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Kios berhasil dinonaktifkan.");
                loadData();
                onBersih(null);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Gagal Nonaktifkan",
                        "Kios gagal dinonaktifkan. Silakan coba lagi atau hubungi admin sistem.");
            }
        }
    }

    @FXML
    void onBersih(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        selectedKios = null;
        autoGenerateId();
        txtHarga.requestFocus();
    }

    // 17. EVENT HANDLER — PENCARIAN
    @FXML
    void onCari(ActionEvent event) {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }

        String kwLower = kw.toLowerCase();
        masterList = semuaData.stream()
                .filter(k ->
                        (k.getIdKios() != null && k.getIdKios().toLowerCase().contains(kwLower))
                                || (k.getDeskripsi() != null && k.getDeskripsi().toLowerCase().contains(kwLower))
                                || (k.getStsKios() != null && k.getStsKios().toLowerCase().contains(kwLower))
                                || String.valueOf((long) k.getHargaKios()).contains(kwLower)
                                || String.valueOf(k.getPanjangKios()).contains(kwLower)
                                || String.valueOf(k.getLebarKios()).contains(kwLower)
                                || String.valueOf(k.getLuasKios()).contains(kwLower)
                )
                .collect(java.util.stream.Collectors.toList());

        currentPage = 1;
        refreshGrid();
    }

    // 18. EVENT HANDLER — PAGINATION
    @FXML
    void onFirstPage(ActionEvent event) {
        currentPage = 1;
        refreshGrid();
    }

    @FXML
    void onLastPage(ActionEvent event) {
        currentPage = totalPage;
        refreshGrid();
    }

    @FXML
    void onNextPage(ActionEvent event) {
        if (currentPage < totalPage) { currentPage++; refreshGrid(); }
    }

    @FXML
    void onPrevPage(ActionEvent event) {
        if (currentPage > 1) { currentPage--; refreshGrid(); }
    }

    // 19. EVENT HANDLER — FILTER
    @FXML
    void onFilterHarga(ActionEvent event) {
        urutanHarga = rmHargaTermurah.isSelected() ? "asc"
                : rmHargaTermahal.isSelected() ? "desc" : null;
        terapkanFilter();
    }

    @FXML
    void onFilterLuas(ActionEvent event) {
        urutanLuas = rmLuasTerkecil.isSelected() ? "asc"
                : rmLuasTerbesar.isSelected() ? "desc" : null;
        terapkanFilter();
    }

    @FXML
    void onFilterStatus(ActionEvent event) {
        filterStatus = rmStatusAktif.isSelected() ? "Aktif"
                : rmStatusNonaktif.isSelected() ? "Nonaktif" : null;
        terapkanFilter();
    }

    private void terapkanFilter() {
        List<Kios> hasil = new ArrayList<>(masterList);

        if (urutanHarga != null) {
            Comparator<Kios> byHarga = Comparator.comparingDouble(Kios::getHargaKios);
            if (urutanHarga.equals("desc")) byHarga = byHarga.reversed();
            hasil.sort(byHarga);
        }

        if (urutanLuas != null) {
            Comparator<Kios> byLuas = Comparator.comparingDouble(Kios::getLuasKios);
            if (urutanLuas.equals("desc")) byLuas = byLuas.reversed();
            hasil.sort(byLuas);
        }

        if (filterStatus != null) {
            hasil = hasil.stream()
                    .filter(k -> filterStatus.equalsIgnoreCase(k.getStsKios()))
                    .collect(java.util.stream.Collectors.toList());
        }

        List<Kios> tampung = masterList;
        masterList = hasil;
        currentPage = 1;
        refreshGrid();
        masterList = tampung;
    }

    @FXML
    void onResetFilter(ActionEvent event) {
        urutanHarga = null;
        urutanLuas = null;
        filterStatus = null;
        rmHargaTermurah.setSelected(false);
        rmHargaTermahal.setSelected(false);
        rmLuasTerkecil.setSelected(false);
        rmLuasTerbesar.setSelected(false);
        rmStatusAktif.setSelected(false);
        rmStatusNonaktif.setSelected(false);
        loadData();
    }
}