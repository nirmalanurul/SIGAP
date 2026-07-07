package com.sigap.controller;

import com.sigap.ADT.Kios;
import com.sigap.APP.CRUD_Kios;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class KiosController implements Initializable {

    @FXML
    private Button btnHapus;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUbah;
    @FXML
    private TableColumn<Kios, String> colDeskripsi;
    @FXML
    private TableColumn<Kios, String> colHarga;
    @FXML
    private TableColumn<Kios, String> colId;
    @FXML
    private TableColumn<Kios, String> colLebar;
    @FXML
    private TableColumn<Kios, String> colLuas;
    @FXML
    private TableColumn<Kios, String> colPanjang;
    @FXML
    private TableColumn<Kios, String> colStsKios;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private TableView<Kios> tabelKios;
    @FXML
    private TextField txtCari;
    @FXML
    private TextArea txtDeskripsi;
    @FXML
    private TextField txtHarga;
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

    private ObservableList<Kios> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;
    private boolean updatingHarga = false;
    private Kios selectedKios = null;

    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdKios.setEditable(false);
        txtLuas.setEditable(false);
        txtStsKios.setEditable(false);

        txtStsKios.setText("Aktif");

        setupTable();
        setupListeners();
        setFormState(false, false);

        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
        });
    }

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

            if (digitsOnly.length() > 8) {
                digitsOnly = digitsOnly.substring(0, 8);
            }

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

    private String rawHarga() {
        return txtHarga.getText().replaceAll("[^0-9]", "");
    }

    private void hitungLuas() {
        try {
            double p = Double.parseDouble(txtPanjang.getText().trim());
            double l = Double.parseDouble(txtLebar.getText().trim());
            double luas = Math.round(p * l * 100.0) / 100.0;
            txtLuas.setText(luas + " m²");
        } catch (NumberFormatException e) {
            txtLuas.setText("");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdKios()));

        colHarga.setCellValueFactory(d -> new SimpleStringProperty(
                "Rp " + FMT_RUPIAH.format(d.getValue().getHargaKios())));

        colPanjang.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPanjangKios() + " m"));

        colLebar.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getLebarKios() + " m"));

        colLuas.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getLuasKios() + " m²"));

        colDeskripsi.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDeskripsi() == null ? "-" : d.getValue().getDeskripsi()));

        colStsKios.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStsKios()));

        colStsKios.setCellFactory(col -> new TableCell<>() {
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

    private void loadData() {
        try {
            List<Kios> list = CRUD_Kios.getAll();
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

        tabelKios.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtIdKios.setText(CRUD_Kios.generateNextId());
        } catch (Exception e) {
            txtIdKios.setText("KS001");
        }
    }

    private void setFormState(boolean editMode, boolean isNonaktif) {
        btnSimpan.setDisable(editMode || isNonaktif);
        btnUbah.setDisable(!editMode || isNonaktif);
        btnHapus.setDisable(!editMode || isNonaktif);

        txtHarga.setEditable(!isNonaktif);
        txtPanjang.setEditable(!isNonaktif);
        txtLebar.setEditable(!isNonaktif);
        txtDeskripsi.setEditable(!isNonaktif);

        String styleReadOnly = "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;"
                + "-fx-font-size:13px;-fx-text-fill:#888;";
        String styleNormal = "-fx-background-color:WHITE;-fx-border-color:#D0D8E8;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;"
                + "-fx-font-size:13px;";

        txtHarga.setStyle(isNonaktif ? styleReadOnly : styleNormal);
        txtPanjang.setStyle(isNonaktif ? styleReadOnly : styleNormal);
        txtLebar.setStyle(isNonaktif ? styleReadOnly : styleNormal);
        txtDeskripsi.setStyle(isNonaktif ? styleReadOnly : styleNormal);
    }

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

    private void bersihForm() {
        txtIdKios.clear();
        txtHarga.clear();
        txtPanjang.clear();
        txtLebar.clear();
        txtLuas.clear();
        txtDeskripsi.clear();
        txtStsKios.setText("Aktif");
    }

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

    private boolean tidakAdaPerubahan(Kios lama, Kios baru) {
        if (lama == null) return false;
        String deskLama = lama.getDeskripsi() == null ? "" : lama.getDeskripsi();
        String deskBaru = baru.getDeskripsi() == null ? "" : baru.getDeskripsi();

        return Double.compare(lama.getHargaKios(), baru.getHargaKios()) == 0
                && Double.compare(lama.getPanjangKios(), baru.getPanjangKios()) == 0
                && Double.compare(lama.getLebarKios(), baru.getLebarKios()) == 0
                && deskLama.equals(deskBaru)
                && lama.getStsKios().equalsIgnoreCase(baru.getStsKios());
    }

    @FXML
    void onBersih(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        tabelKios.getSelectionModel().clearSelection();
        selectedKios = null;
        autoGenerateId();
        txtHarga.requestFocus();
    }

    @FXML
    void onCari(ActionEvent event) {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Kios> hasil = CRUD_Kios.search(kw);
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
        if (!validasi()) return;
        try {
            double panjang = Double.parseDouble(txtPanjang.getText().trim());
            double lebar = Double.parseDouble(txtLebar.getText().trim());
            double luas = Math.round(panjang * lebar * 100.0) / 100.0;
            Kios k = new Kios(
                    txtIdKios.getText().trim(),
                    Double.parseDouble(rawHarga()),
                    panjang,
                    lebar,
                    luas,
                    txtDeskripsi.getText().trim().isEmpty() ? null : txtDeskripsi.getText().trim(),
                    "Aktif"
            );
            CRUD_Kios.insert(k);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data kios berhasil disimpan.");
            loadData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Error : " + e.getMessage());
        }
    }

    @FXML
    void onTableClick(MouseEvent event) {
        Kios k = tabelKios.getSelectionModel().getSelectedItem();
        if (k == null)
            return;
        selectedKios = k;
        txtIdKios.setText(k.getIdKios());
        txtHarga.setText(FMT_RUPIAH.format((long) k.getHargaKios()));
        txtPanjang.setText(String.valueOf(k.getPanjangKios()));
        txtLebar.setText(String.valueOf(k.getLebarKios()));
        txtLuas.setText(k.getLuasKios() + " m²");
        txtDeskripsi.setText(k.getDeskripsi() == null ? "" : k.getDeskripsi());
        txtStsKios.setText(k.getStsKios());
        boolean nonaktif = "Nonaktif".equalsIgnoreCase(k.getStsKios());
        setFormState(true, nonaktif);
    }

    @FXML
    void onUbah(ActionEvent event) {
        if (!validasi())
            return;
        try {
            double panjang = Double.parseDouble(txtPanjang.getText().trim());
            double lebar = Double.parseDouble(txtLebar.getText().trim());
            double luas = Math.round(panjang * lebar * 100.0) / 100.0;

            Kios k = new Kios(
                    txtIdKios.getText().trim(),
                    Double.parseDouble(rawHarga()),
                    panjang,
                    lebar,
                    luas,
                    txtDeskripsi.getText().trim().isEmpty() ? null : txtDeskripsi.getText().trim(),
                    txtStsKios.getText()
            );

            if (tidakAdaPerubahan(selectedKios, k)) {
                showAlert(Alert.AlertType.INFORMATION, "Tidak Ada Perubahan", "Tidak ada perubahan data.");
                return;
            }

            CRUD_Kios.update(k);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data kios berhasil diubah.");
            loadData();
            onBersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", "Error : " + e.getMessage());
        }
    }
}