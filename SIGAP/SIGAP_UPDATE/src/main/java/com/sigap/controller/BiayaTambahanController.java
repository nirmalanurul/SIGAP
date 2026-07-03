package com.sigap.controller;

import com.sigap.ADT.BiayaTambahan;
import com.sigap.APP.CRUD_BiayaTambahan;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BiayaTambahanController implements Initializable {

    @FXML
    private Button btnBersih;
    @FXML
    private Button btnHapus;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUbah;
    @FXML
    private ComboBox<String> cmbjenis;
    @FXML
    private TableColumn<BiayaTambahan, String> colId;
    @FXML
    private TableColumn<BiayaTambahan, String> coljenispelanggaran;
    @FXML
    private TableColumn<BiayaTambahan, String> colketerangan;
    @FXML
    private TableColumn<BiayaTambahan, Double> colnominal;
    @FXML
    private TableColumn<BiayaTambahan, String> colstatus;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private TableView<BiayaTambahan> tabelBiayaTambahan;
    @FXML
    private TextField txtBiayaTambahan;
    @FXML
    private TextArea txtKeterangan;
    @FXML
    private TextField txtNominal;
    @FXML
    private TextField txtcari;
    @FXML
    private TextField txtstatus;

    private final ObservableList<BiayaTambahan> masterList = FXCollections.observableArrayList();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

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
        txtBiayaTambahan.setEditable(false);
        txtstatus.setEditable(false);
        txtstatus.setStyle(STYLE_READONLY);

        cmbjenis.setItems(FXCollections.observableArrayList(
                "Kerusakan Fasilitas", "Keterlambatan Bayar Sewa"
        ));

        setupTable();
        setFormState(false, false);

        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
            txtstatus.setText("Aktif");
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdBiayaTambahan()));
        coljenispelanggaran.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJenisBiayaTambahan()));
        colnominal.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(
                d.getValue().getNominalDenda()).asObject());
        colketerangan.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKeterangan()));
        colstatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStsDenda()));

        colstatus.setCellFactory(col -> new TableCell<>() {
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

        colnominal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %,.0f", val));
            }
        });
    }

    private void loadData() {
        try {
            List<BiayaTambahan> list = CRUD_BiayaTambahan.getAll();
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
        tabelBiayaTambahan.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtBiayaTambahan.setText(CRUD_BiayaTambahan.generateNextId());
        } catch (Exception e) {
            txtBiayaTambahan.setText("BY001");
        }
    }

    private void setFormState(boolean editMode, boolean locked) {
        btnSimpan.setDisable(editMode || locked);
        btnUbah.setDisable(!editMode || locked);
        btnHapus.setDisable(!editMode || locked);

        cmbjenis.setDisable(locked);
        txtNominal.setEditable(!locked);
        txtKeterangan.setEditable(!locked);

        txtNominal.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtKeterangan.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
    }

    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        if (cmbjenis.getValue() == null || cmbjenis.getValue().isEmpty()) {
            sb.append("• Jenis Biaya Tambahan wajib dipilih (Kerusakan Fasilitas / Keterlambatan Bayar Sewa).\n");
        }

        String nominalStr = txtNominal.getText().trim();
        if (nominalStr.isEmpty()) {
            sb.append("• Nominal wajib diisi.\n");
        } else {
            try {
                double nominal = Double.parseDouble(nominalStr);
                if (nominal <= 0) {
                    sb.append("• Nominal harus lebih besar dari 0.\n");
                }
            } catch (NumberFormatException e) {
                sb.append("• Nominal harus berupa angka.\n");
            }
        }

        String keterangan = txtKeterangan.getText() == null ? "" : txtKeterangan.getText().trim();
        if (keterangan.length() > 80) {
            sb.append("• Keterangan maksimal 80 karakter.\n");
        }

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return false;
        }
        return true;
    }

    private void bersihForm() {
        txtBiayaTambahan.clear();
        cmbjenis.setValue(null);
        txtNominal.clear();
        txtKeterangan.clear();
        txtstatus.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (txtBiayaTambahan != null && txtBiayaTambahan.getScene() != null)
                alert.initOwner(txtBiayaTambahan.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    @FXML
    void onbersih(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        tabelBiayaTambahan.getSelectionModel().clearSelection();
        autoGenerateId();
        txtstatus.setText("Aktif");
        cmbjenis.requestFocus();
    }

    @FXML
    void onTableClick(MouseEvent event) {
        BiayaTambahan d = tabelBiayaTambahan.getSelectionModel().getSelectedItem();
        if (d == null) return;

        txtBiayaTambahan.setText(d.getIdBiayaTambahan());
        cmbjenis.setValue(d.getJenisBiayaTambahan());
        txtNominal.setText(String.valueOf(d.getNominalDenda()));
        txtKeterangan.setText(d.getKeterangan());
        txtstatus.setText(d.getStsDenda());

        boolean isTidakAktif = "Tidak Aktif".equalsIgnoreCase(
                d.getStsDenda() != null ? d.getStsDenda().trim() : ""
        );
        setFormState(true, isTidakAktif);
    }

    @FXML
    void oncari(ActionEvent event) {
        String kw = txtcari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<BiayaTambahan> hasil = CRUD_BiayaTambahan.search(kw);
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
    void onhapus(ActionEvent event) {
        String id = txtBiayaTambahan.getText().trim();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data biaya tambahan yang ingin dinonaktifkan.");
            return;
        }

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Nonaktifkan");
        konfirmasi.setHeaderText("Nonaktifkan Biaya Tambahan");
        konfirmasi.setContentText("Data [" + id + "] akan diubah statusnya menjadi Tidak Aktif.\nLanjutkan?");
        if (txtBiayaTambahan.getScene() != null)
            konfirmasi.initOwner(txtBiayaTambahan.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_BiayaTambahan.delete(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data berhasil dinonaktifkan.");
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
            BiayaTambahan d = new BiayaTambahan(
                    txtBiayaTambahan.getText().trim(),
                    cmbjenis.getValue(),
                    Double.parseDouble(txtNominal.getText().trim()),
                    txtKeterangan.getText() == null ? "" : txtKeterangan.getText().trim()
            );
            CRUD_BiayaTambahan.insert(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data biaya tambahan berhasil disimpan.");
            loadData();
            onbersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onubah(ActionEvent event) {
        if (!validasi()) return;
        try {
            BiayaTambahan d = new BiayaTambahan(
                    txtBiayaTambahan.getText().trim(),
                    cmbjenis.getValue(),
                    Double.parseDouble(txtNominal.getText().trim()),
                    txtKeterangan.getText() == null ? "" : txtKeterangan.getText().trim()
            );
            CRUD_BiayaTambahan.update(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data biaya tambahan berhasil diubah.");
            loadData();
            onbersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", "Error: " + e.getMessage());
        }
    }
}