package com.sigap.controller;

import com.sigap.ADT.Penyewaan;
import com.sigap.ADT.TagihanPembayaranSewa;
import com.sigap.APP.CRUD_Penyewaan;
import com.sigap.APP.CRUD_TagihanPembayaranSewa;

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class PilihPenyewaanController implements Initializable {

    @FXML
    private TextField txtCari;
    @FXML
    private TableView<Penyewaan> tabelPenyewaan;
    @FXML
    private TableColumn<Penyewaan, String> colId;
    @FXML
    private TableColumn<Penyewaan, String> colKios;
    @FXML
    private TableColumn<Penyewaan, String> colPenyewa;
    @FXML
    private TableColumn<Penyewaan, String> colTglMulai;
    @FXML
    private TableColumn<Penyewaan, String> colTglSelesai;
    @FXML
    private TableColumn<Penyewaan, String> colStatus;

    private final ObservableList<Penyewaan> masterList = FXCollections.observableArrayList();
    private Penyewaan penyewaanTerpilih = null;

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        Platform.runLater(this::loadData);
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewaan()));
        colKios.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdKios()));
        colPenyewa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewa()));
        colTglMulai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTglMulai() == null ? "" : d.getValue().getTglMulai().format(FMT_TGL)));
        colTglSelesai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTglSelesai() == null ? "" : d.getValue().getTglSelesai().format(FMT_TGL)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStsPenyewaan()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle("Berlangsung".equalsIgnoreCase(status)
                        ? "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;"
                        : "-fx-background-color:#FFF3D6;-fx-text-fill:#B8860B;-fx-font-weight:700;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;");
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
            List<Penyewaan> semua = CRUD_Penyewaan.getAll();
            masterList.setAll(filterBelumDitagih(semua));
            tabelPenyewaan.setItems(masterList);
        } catch (Exception e) {
            showAlert("Gagal memuat data penyewaan. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    /**
     * Hanya menampilkan penyewaan yang:
     *  1) statusnya belum 'Dibatalkan', dan
     *  2) belum memiliki tagihan pembayaran aktif (selain yang sudah 'Dibatalkan').
     * Konsisten dengan aturan spInsertTagihanPembayaran di database (1 penyewaan -> maksimal 1 tagihan aktif).
     */
    private List<Penyewaan> filterBelumDitagih(List<Penyewaan> semua) {
        Set<String> sudahDitagih;
        try {
            List<TagihanPembayaranSewa> tagihanList = CRUD_TagihanPembayaranSewa.getAll();
            sudahDitagih = tagihanList.stream()
                    .filter(t -> !"Dibatalkan".equalsIgnoreCase(t.getStsTagihanPembayaran()))
                    .map(TagihanPembayaranSewa::getIdPenyewaan)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            sudahDitagih = Set.of();
        }
        final Set<String> finalSudahDitagih = sudahDitagih;
        return semua.stream()
                .filter(p -> !"Dibatalkan".equalsIgnoreCase(p.getStsPenyewaan()))
                .filter(p -> !finalSudahDitagih.contains(p.getIdPenyewaan()))
                .collect(Collectors.toList());
    }

    private void showAlert(String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (tabelPenyewaan.getScene() != null) alert.initOwner(tabelPenyewaan.getScene().getWindow());
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
            List<Penyewaan> hasil = filterBelumDitagih(CRUD_Penyewaan.search(kw));
            tabelPenyewaan.setItems(FXCollections.observableArrayList(hasil));
        } catch (Exception e) {
            showAlert("Pencarian gagal. Error: " + e.getMessage());
        }
    }

    @FXML
    void onRowClicked(MouseEvent event) {
        if (event.getClickCount() < 1) return;
        Penyewaan dipilih = tabelPenyewaan.getSelectionModel().getSelectedItem();
        if (dipilih == null) return;
        penyewaanTerpilih = dipilih;
        tutupDialog();
    }

    @FXML
    void onBatal(ActionEvent event) {
        penyewaanTerpilih = null;
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) tabelPenyewaan.getScene().getWindow();
        stage.close();
    }

    /**
     * Dipanggil oleh parent controller setelah dialog.showAndWait() selesai.
     * Mengembalikan null jika dialog dibatalkan / ditutup tanpa memilih.
     */
    public Penyewaan getPenyewaanTerpilih() {
        return penyewaanTerpilih;
    }
}