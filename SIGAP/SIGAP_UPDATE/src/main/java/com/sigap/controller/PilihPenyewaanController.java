package com.sigap.controller;

import com.sigap.ADT.Penyewa;
import com.sigap.ADT.Penyewaan;
import com.sigap.ADT.TagihanPembayaranSewa;
import com.sigap.APP.CRUD_Penyewa;
import com.sigap.APP.CRUD_Penyewaan;
import com.sigap.APP.CRUD_TagihanPembayaranSewa;
import com.sigap.util.PeriodeTagihanUtil;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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

    /** Seluruh penyewaan yang lolos filter (sebelum kata kunci pencarian diterapkan). */
    private List<Penyewaan> daftarLengkap = List.of();
    /** Peta Id_Penyewa -> data Penyewa, dipakai untuk tampilkan & cari berdasarkan nama. */
    private Map<String, Penyewa> petaPenyewa = Map.of();

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        Platform.runLater(this::loadData);
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdPenyewaan()));
        colKios.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdKios()));
        colPenyewa.setCellValueFactory(d -> new SimpleStringProperty(labelPenyewa(d.getValue().getIdPenyewa())));
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
            muatPetaPenyewa();
            List<Penyewaan> semua = CRUD_Penyewaan.getAll();
            daftarLengkap = filterMasihAdaSisaBulan(semua);
            masterList.setAll(daftarLengkap);
            tabelPenyewaan.setItems(masterList);
            tabelPenyewaan.refresh();
        } catch (Exception e) {
            showAlert("Gagal memuat data penyewaan. Periksa koneksi ke database atau hubungi admin sistem.");
        }
    }

    /** Memuat data Nama_Penyewa dkk sekali di awal, dipakai untuk tampilan kolom & pencarian nama. */
    private void muatPetaPenyewa() {
        try {
            petaPenyewa = CRUD_Penyewa.getAll().stream()
                    .collect(Collectors.toMap(Penyewa::getIdPenyewa, p -> p, (a, b) -> a));
        } catch (Exception e) {
            petaPenyewa = Map.of();
        }
    }

    /** Label kolom Penyewa: "ID - Nama" jika nama diketahui, jatuh ke ID saja jika tidak. */
    private String labelPenyewa(String idPenyewa) {
        if (idPenyewa == null) return "";
        Penyewa p = petaPenyewa.get(idPenyewa);
        if (p == null || p.getNamaPenyewa() == null || p.getNamaPenyewa().isBlank()) {
            return idPenyewa;
        }
        return idPenyewa + " - " + p.getNamaPenyewa();
    }

    /**
     * Hanya menampilkan penyewaan yang:
     *  1) statusnya belum 'Dibatalkan', dan
     *  2) masih punya minimal satu slot bulan (dari Tgl_Mulai s/d Tgl_Selesai)
     *     yang belum punya tagihan aktif (selain yang sudah 'Dibatalkan').
     * Satu penyewaan sekarang bisa punya banyak tagihan aktif sekaligus (satu per
     * bulan kontrak) — bukan lagi "belum pernah ditagih sama sekali" seperti
     * sebelumnya, tapi "masih ada sisa bulan yang belum ditagih". Bulan mana saja
     * yang tersedia baru ditentukan di dialog Pilih Bulan Tagihan (tahap 2).
     */
    private List<Penyewaan> filterMasihAdaSisaBulan(List<Penyewaan> semua) {
        Map<String, List<TagihanPembayaranSewa>> tagihanPerPenyewaan;
        try {
            List<TagihanPembayaranSewa> tagihanList = CRUD_TagihanPembayaranSewa.getAll();
            tagihanPerPenyewaan = tagihanList.stream()
                    .filter(t -> !"Dibatalkan".equalsIgnoreCase(t.getStsTagihanPembayaran()))
                    .collect(Collectors.groupingBy(TagihanPembayaranSewa::getIdPenyewaan));
        } catch (Exception e) {
            tagihanPerPenyewaan = Map.of();
        }
        final Map<String, List<TagihanPembayaranSewa>> finalTagihanPerPenyewaan = tagihanPerPenyewaan;

        return semua.stream()
                .filter(p -> !"Dibatalkan".equalsIgnoreCase(p.getStsPenyewaan()))
                .filter(p -> {
                    List<LocalDate> slotBulanan = PeriodeTagihanUtil.generateJatuhTempoBulanan(
                            p.getTglMulai(), p.getTglSelesai());
                    List<TagihanPembayaranSewa> tagihanAktif =
                            finalTagihanPerPenyewaan.getOrDefault(p.getIdPenyewaan(), List.of());
                    return slotBulanan.stream().anyMatch(slot -> !PeriodeTagihanUtil.sudahDitagih(slot, tagihanAktif));
                })
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
        if (kw.isEmpty()) {
            masterList.setAll(daftarLengkap);
            return;
        }
        String kwLower = kw.toLowerCase();
        List<Penyewaan> hasil = daftarLengkap.stream()
                .filter(p -> cocokKeyword(p, kwLower))
                .collect(Collectors.toList());
        masterList.setAll(hasil);
    }

    /** Cocokkan kata kunci ke ID Penyewaan, Kios, ID Penyewa, Nama Penyewa, dan Status. */
    private boolean cocokKeyword(Penyewaan p, String kwLower) {
        if (mengandung(p.getIdPenyewaan(), kwLower)) return true;
        if (mengandung(p.getIdKios(), kwLower)) return true;
        if (mengandung(p.getIdPenyewa(), kwLower)) return true;
        if (mengandung(p.getStsPenyewaan(), kwLower)) return true;
        Penyewa penyewa = petaPenyewa.get(p.getIdPenyewa());
        return penyewa != null && mengandung(penyewa.getNamaPenyewa(), kwLower);
    }

    private boolean mengandung(String value, String kwLower) {
        return value != null && value.toLowerCase().contains(kwLower);
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