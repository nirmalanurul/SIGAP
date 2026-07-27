package com.sigap.controller;

import com.sigap.ADT.Kios;
import com.sigap.ADT.Penyewaan;
import com.sigap.APP.CRUD_Kios;
import com.sigap.APP.CRUD_Penyewaan;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
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

    /** Rentang tanggal sewa yang diminta oleh form Penyewaan; dipakai untuk menyaring kios yang bentrok jadwal. */
    private LocalDate tglMulaiFilter = null;
    private LocalDate tglSelesaiFilter = null;

    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        Platform.runLater(this::loadData);
    }

    /**
     * Dipanggil oleh parent controller (PenyewaanController) SEBELUM dialog.showAndWait(),
     * supaya loadData() -- yang baru jalan lewat Platform.runLater setelah dialog tampil --
     * sudah tahu rentang tanggal yang perlu difilter.
     */
    public void setRentangTanggal(LocalDate tglMulai, LocalDate tglSelesai) {
        this.tglMulaiFilter = tglMulai;
        this.tglSelesaiFilter = tglSelesai;
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
            List<Kios> tersedia = terapkanFilterKetersediaan(semua);
            masterList.setAll(tersedia);
            tabelKios.setItems(masterList);
        } catch (Exception e) {
            showAlert("Gagal memuat data kios. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    /**
     * Hanya kios berstatus Tersedia DAN tidak bentrok jadwal dengan penyewaan lain
     * (yang belum Dibatalkan) pada rentang tglMulaiFilter..tglSelesaiFilter.
     * Kalau rentang tanggal belum diset, hanya filter status Tersedia seperti semula.
     */
    private List<Kios> terapkanFilterKetersediaan(List<Kios> semua) {
        List<Kios> tersediaSaja = semua.stream()
                .filter(k -> "Tersedia".equalsIgnoreCase(k.getStsKios()))
                .collect(Collectors.toList());

        if (tglMulaiFilter == null || tglSelesaiFilter == null) {
            return tersediaSaja;
        }

        Set<String> idKiosBentrok;
        try {
            List<Penyewaan> semuaPenyewaan = CRUD_Penyewaan.getAll();
            idKiosBentrok = semuaPenyewaan.stream()
                    .filter(p -> !"Dibatalkan".equalsIgnoreCase(p.getStsPenyewaan()))
                    .filter(p -> bentrok(tglMulaiFilter, tglSelesaiFilter, p.getTglMulai(), p.getTglSelesai()))
                    .map(Penyewaan::getIdKios)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            // Kalau gagal ambil data penyewaan, jangan blokir semua kios -- tampilkan yang Tersedia saja.
            idKiosBentrok = Set.of();
        }

        final Set<String> bentrokFinal = idKiosBentrok;
        return tersediaSaja.stream()
                .filter(k -> !bentrokFinal.contains(k.getIdKios()))
                .collect(Collectors.toList());
    }

    /** Dua rentang tanggal [mulai1,selesai1] dan [mulai2,selesai2] tumpang tindih? */
    private boolean bentrok(LocalDate mulai1, LocalDate selesai1, LocalDate mulai2, LocalDate selesai2) {
        if (mulai1 == null || selesai1 == null || mulai2 == null || selesai2 == null) return false;
        return !mulai1.isAfter(selesai2) && !mulai2.isAfter(selesai1);
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
            List<Kios> hasil = terapkanFilterKetersediaan(CRUD_Kios.search(kw));
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