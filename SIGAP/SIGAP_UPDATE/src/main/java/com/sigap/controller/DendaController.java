package com.sigap.controller;

import com.sigap.ADT.Denda;
import com.sigap.APP.CRUD_Denda;

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
import java.util.stream.Collectors;

public class DendaController implements Initializable {
//

    @FXML
    private Button btnBersih;
    @FXML
    private Button btnHapus;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUbah;
    @FXML
    private TableColumn<Denda, String> colId;
    @FXML
    private TableColumn<Denda, String> coljenispelanggaran;
    @FXML
    private TableColumn<Denda, String> colketerangan;
    @FXML
    private TableColumn<Denda, String> colnominal;
    @FXML
    private TableColumn<Denda, String> colstatus;
    @FXML
    private Label iddenda;
    @FXML
    private Label jenispelanggaran;
    @FXML
    private Label keterangan;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private Label nominaldenda;
    @FXML
    private Label status;
    @FXML
    private TableView<Denda> tabelDenda;
    @FXML
    private TextField txtIdDenda;
    @FXML
    private TextField txtJenisPelannggaran;
    @FXML
    private TextArea txtKeterangan;
    @FXML
    private TextField txtNominalDenda;
    @FXML
    private TextField txtcari;
    @FXML
    private TextField txtstatus;

    private ObservableList<Denda> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;"
                    + "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;"
                    + "-fx-font-size:13px;-fx-text-fill:#888;";
    private static final String STYLE_NORMAL =
            "-fx-background-color:WHITE;-fx-border-color:#D0D8E8;"
                    + "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;"
                    + "-fx-font-size:13px;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtIdDenda.setEditable(false);
        txtstatus.setEditable(false);
        txtstatus.setStyle(STYLE_READONLY);

        setupTable();
        setupListeners();
        setFormState(false, false);
        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
            txtstatus.setText("Aktif");
        });
    }

    private void setupListeners() {
        txtNominalDenda.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9]", "");
            if (!filtered.equals(newVal)) txtNominalDenda.setText(filtered);
        });
    }

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
            List<Denda> list = CRUD_Denda.getAll();
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
        tabelDenda.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtIdDenda.setText(CRUD_Denda.generateNextId());
        } catch (Exception e) {
            txtIdDenda.setText("DN001");
        }
    }

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

    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        String jenis = txtJenisPelannggaran.getText().trim();
        if (jenis.isEmpty()) {
            sb.append("• Jenis Pelanggaran wajib diisi.\n");
        } else if (jenis.length() < 3) {
            sb.append("• Jenis Pelanggaran minimal 3 karakter.\n");
        } else if (jenis.length() > 100) {
            sb.append("• Jenis Pelanggaran maksimal 100 karakter.\n");
        }

        String nominalStr = txtNominalDenda.getText().trim();
        if (nominalStr.isEmpty()) {
            sb.append("• Nominal Denda wajib diisi.\n");
        } else {
            try {
                long nominal = Long.parseLong(nominalStr);
                if (nominal <= 0) sb.append("• Nominal Denda harus lebih dari 0.\n");
                if (nominal > 99_999_999L) sb.append("• Nominal Denda maksimal Rp 99.999.999.\n");
            } catch (NumberFormatException e) {
                sb.append("• Nominal Denda harus berupa angka bulat.\n");
            }
        }

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

    private void bersihForm() {
        txtIdDenda.clear();
        txtJenisPelannggaran.clear();
        txtNominalDenda.clear();
        txtKeterangan.clear();
        txtstatus.clear();
    }

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

    @FXML
    void onTableClick(MouseEvent event) {
        Denda d = tabelDenda.getSelectionModel().getSelectedItem();
        if (d == null) return;

        txtIdDenda.setText(d.getIdDenda());
        txtJenisPelannggaran.setText(d.getJenisPelanggaran());
        txtNominalDenda.setText(String.valueOf((long) d.getNominalDenda()));
        txtKeterangan.setText(d.getKeterangan() == null ? "" : d.getKeterangan());
        txtstatus.setText(d.getStsDenda());

        boolean isTidakAktif = "Tidak Aktif".equalsIgnoreCase(
                d.getStsDenda() != null ? d.getStsDenda().trim() : ""
        );
        setFormState(true, isTidakAktif);
    }

    @FXML
    void onbersih(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        tabelDenda.getSelectionModel().clearSelection();
        autoGenerateId();
        txtstatus.setText("Aktif");
    }

    @FXML
    void oncari(ActionEvent event) {
        String kw = txtcari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Denda> all = CRUD_Denda.getAll();
            String kwLower = kw.toLowerCase();
            List<Denda> hasil = all.stream()
                    .filter(d ->
                            (d.getIdDenda() != null && d.getIdDenda().toLowerCase().contains(kwLower))
                                    || (d.getJenisPelanggaran() != null && d.getJenisPelanggaran().toLowerCase().contains(kwLower))
                                    || (d.getStsDenda() != null && d.getStsDenda().toLowerCase().contains(kwLower))
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
    void onhapus(ActionEvent event) {
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
                CRUD_Denda.delete(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Denda berhasil dinonaktifkan.");
                loadData();
                onbersih(null);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Error: " + e.getMessage());
            }
        }
    }

    @FXML
    void onsimpan(ActionEvent event) {
        if (!validasi()) return;
        try {
            Denda d = new Denda(
                    txtIdDenda.getText().trim(),
                    txtJenisPelannggaran.getText().trim(),
                    Double.parseDouble(txtNominalDenda.getText().trim()),
                    txtKeterangan.getText().trim().isEmpty() ? null : txtKeterangan.getText().trim()
            );
            CRUD_Denda.insert(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data denda berhasil disimpan.");
            loadData();
            ontambah(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Error: " + e.getMessage());
        }
    }

    @FXML
    void ontambah(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        autoGenerateId();
        txtstatus.setText("Aktif");
        txtJenisPelannggaran.requestFocus();
    }

    @FXML
    void onubah(ActionEvent event) {
        if (!validasi()) return;
        try {
            Denda d = new Denda(
                    txtIdDenda.getText().trim(),
                    txtJenisPelannggaran.getText().trim(),
                    Double.parseDouble(txtNominalDenda.getText().trim()),
                    txtKeterangan.getText().trim().isEmpty() ? null : txtKeterangan.getText().trim()
            );
            CRUD_Denda.update(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data denda berhasil diubah.");
            loadData();
            onbersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", "Error: " + e.getMessage());
        }
    }
}