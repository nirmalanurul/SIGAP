package com.sigap.controller;

import com.sigap.ADT.Penyewa;
import com.sigap.APP.CRUD_Penyewa;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PilihPenyewaController implements Initializable {

    @FXML
    private TextField txtCari;
    @FXML
    private TableView<Penyewa> tabelPenyewa;
    @FXML
    private TableColumn<Penyewa, String> colId;
    @FXML
    private TableColumn<Penyewa, String> colNama;
    @FXML
    private TableColumn<Penyewa, String> colNIK;
    @FXML
    private TableColumn<Penyewa, String> colNoTelp;
    @FXML
    private TableColumn<Penyewa, String> colStatus;

    private final ObservableList<Penyewa> masterList = FXCollections.observableArrayList();
    private Penyewa penyewaTerpilih = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        Platform.runLater(this::loadData);
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewa()));
        colNama.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNamaPenyewa()));
        colNIK.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNik()));
        colNoTelp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNoTelp()));
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
            // Hanya penyewa berstatus Aktif yang boleh dipilih untuk transaksi baru
            List<Penyewa> semua = CRUD_Penyewa.getAll();
            List<Penyewa> aktifSaja = semua.stream()
                    .filter(p -> "Aktif".equalsIgnoreCase(p.getStsPenyewa()))
                    .collect(Collectors.toList());
            masterList.setAll(aktifSaja);
            tabelPenyewa.setItems(masterList);
        } catch (Exception e) {
            showAlert("Gagal memuat data penyewa. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    private void showAlert(String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (tabelPenyewa.getScene() != null) alert.initOwner(tabelPenyewa.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    @FXML
    void onCari(ActionEvent event) {
        String kw = txtCari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<Penyewa> hasil = CRUD_Penyewa.search(kw).stream()
                    .filter(p -> "Aktif".equalsIgnoreCase(p.getStsPenyewa()))
                    .collect(Collectors.toList());
            tabelPenyewa.setItems(FXCollections.observableArrayList(hasil));
        } catch (Exception e) {
            showAlert("Pencarian gagal. Error: " + e.getMessage());
        }
    }

    @FXML
    void onRowClicked(MouseEvent event) {
        if (event.getClickCount() < 1) return;
        Penyewa dipilih = tabelPenyewa.getSelectionModel().getSelectedItem();
        if (dipilih == null) return;
        penyewaTerpilih = dipilih;
        tutupDialog();
    }

    @FXML
    void onBatal(ActionEvent event) {
        penyewaTerpilih = null;
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) tabelPenyewa.getScene().getWindow();
        stage.close();
    }

    /**
     * Dipanggil oleh parent controller setelah dialog.showAndWait() selesai.
     * Mengembalikan null jika dialog dibatalkan / ditutup tanpa memilih.
     */
    public Penyewa getPenyewaTerpilih() {
        return penyewaTerpilih;
    }
}