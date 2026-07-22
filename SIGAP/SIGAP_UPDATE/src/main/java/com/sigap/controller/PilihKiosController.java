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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PilihKiosController implements Initializable {

    @FXML
    private TextField txtCari;
    @FXML
    private TableView<Kios> tabelKios;
    @FXML
    private TableColumn<Kios, String> colId;
    @FXML
    private TableColumn<Kios, String> colHarga;
    @FXML
    private TableColumn<Kios, String> colUkuran;
    @FXML
    private TableColumn<Kios, String> colLuas;
    @FXML
    private TableColumn<Kios, String> colDeskripsi;

    private final ObservableList<Kios> masterList = FXCollections.observableArrayList();
    private Kios kiosTerpilih = null;

    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        Platform.runLater(this::loadData);
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdKios()));
        colHarga.setCellValueFactory(d -> new SimpleStringProperty(
                "Rp " + FMT_RUPIAH.format((long) d.getValue().getHargaKios())));
        colUkuran.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPanjangKios() + " x " + d.getValue().getLebarKios()));
        colLuas.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getLuasKios())));
        colDeskripsi.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDeskripsi() == null ? "" : d.getValue().getDeskripsi()));

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
            List<Kios> semua = CRUD_Kios.getAll();
            List<Kios> tersediaSaja = semua.stream()
                    .filter(k -> "Aktif".equalsIgnoreCase(k.getStsKios()))
                    .collect(Collectors.toList());
            masterList.setAll(tersediaSaja);
            tabelKios.setItems(masterList);
        } catch (Exception e) {
            showAlert("Gagal memuat data kios. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    private void showAlert(String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (tabelKios.getScene() != null) alert.initOwner(tabelKios.getScene().getWindow());
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
            List<Kios> hasil = CRUD_Kios.search(kw).stream()
                    .filter(k -> "Aktif".equalsIgnoreCase(k.getStsKios()))
                    .collect(Collectors.toList());
            tabelKios.setItems(FXCollections.observableArrayList(hasil));
        } catch (Exception e) {
            showAlert("Pencarian gagal. Error: " + e.getMessage());
        }
    }

    @FXML
    void onRowClicked(MouseEvent event) {
        if (event.getClickCount() < 1) return;
        Kios dipilih = tabelKios.getSelectionModel().getSelectedItem();
        if (dipilih == null) return;
        kiosTerpilih = dipilih;
        tutupDialog();
    }

    @FXML
    void onBatal(ActionEvent event) {
        kiosTerpilih = null;
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) tabelKios.getScene().getWindow();
        stage.close();
    }

    /**
     * Dipanggil oleh parent controller setelah dialog.showAndWait() selesai.
     * Mengembalikan null jika dialog dibatalkan / ditutup tanpa memilih.
     */
    public Kios getKiosTerpilih() {
        return kiosTerpilih;
    }
}