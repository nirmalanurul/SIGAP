package com.sigap.controller;

import com.sigap.ADT.Penyewaan;
import com.sigap.ADT.TagihanPembayaranSewa;
import com.sigap.APP.CRUD_TagihanPembayaranSewa;
import com.sigap.util.PeriodeTagihanUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Dialog tahap ke-2 dari alur "Pilih Penyewaan": menampilkan slot bulan
 * tagihan (virtual, belum ada di DB) untuk satu Penyewaan yang sudah dipilih
 * di tahap 1, hanya bulan yang BELUM punya tagihan aktif. Kasir memilih satu
 * bulan, dan tanggal jatuh tempo bulan itu dikembalikan ke TagihanController.
 */
public class PilihBulanTagihanController implements Initializable {

    @FXML
    private Label lblInfoPenyewaan;
    @FXML
    private TableView<SlotBulan> tabelBulan;
    @FXML
    private TableColumn<SlotBulan, String> colBulanKe;
    @FXML
    private TableColumn<SlotBulan, String> colJatuhTempo;

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FMT_BULAN = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID"));

    private Penyewaan penyewaan;
    private LocalDate jatuhTempoTerpilih = null;

    /** Baris tabel: satu slot bulan tagihan yang masih tersedia (virtual). */
    public static class SlotBulan {
        final int bulanKe;
        final LocalDate jatuhTempo;

        SlotBulan(int bulanKe, LocalDate jatuhTempo) {
            this.bulanKe = bulanKe;
            this.jatuhTempo = jatuhTempo;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBulanKe.setCellValueFactory(d -> new SimpleStringProperty("Bulan ke-" + d.getValue().bulanKe));
        colJatuhTempo.setCellValueFactory(d -> new SimpleStringProperty(
                capitalize(d.getValue().jatuhTempo.format(FMT_BULAN))
                        + "  (jatuh tempo " + d.getValue().jatuhTempo.format(FMT_TGL) + ")"));
    }

    /** Dipanggil oleh TagihanController sebelum dialog ditampilkan (showAndWait). */
    public void setPenyewaan(Penyewaan p) {
        this.penyewaan = p;
        lblInfoPenyewaan.setText("Penyewaan " + p.getIdPenyewaan() + "  |  Kios " + p.getIdKios()
                + "  |  Periode " + p.getTglMulai().format(FMT_TGL) + " s/d " + p.getTglSelesai().format(FMT_TGL));
        muatSlotBulan();
    }

    private void muatSlotBulan() {
        List<LocalDate> semuaSlot = PeriodeTagihanUtil.generateJatuhTempoBulanan(
                penyewaan.getTglMulai(), penyewaan.getTglSelesai());

        List<TagihanPembayaranSewa> tagihanAktif;
        try {
            tagihanAktif = CRUD_TagihanPembayaranSewa.getByIdPenyewaan(penyewaan.getIdPenyewaan()).stream()
                    .filter(t -> !"Dibatalkan".equalsIgnoreCase(t.getStsTagihanPembayaran()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            tagihanAktif = List.of();
        }

        ObservableList<SlotBulan> slotBelumDitagih = FXCollections.observableArrayList();
        int bulanKe = 0;
        for (LocalDate slot : semuaSlot) {
            bulanKe++;
            if (!PeriodeTagihanUtil.sudahDitagih(slot, tagihanAktif)) {
                slotBelumDitagih.add(new SlotBulan(bulanKe, slot));
            }
        }
        tabelBulan.setItems(slotBelumDitagih);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @FXML
    void onRowClicked(MouseEvent event) {
        if (event.getClickCount() < 1) return;
        SlotBulan dipilih = tabelBulan.getSelectionModel().getSelectedItem();
        if (dipilih == null) return;
        jatuhTempoTerpilih = dipilih.jatuhTempo;
        tutupDialog();
    }

    @FXML
    void onBatal(ActionEvent event) {
        jatuhTempoTerpilih = null;
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) tabelBulan.getScene().getWindow();
        stage.close();
    }

    /** Null jika dialog dibatalkan / ditutup tanpa memilih bulan. */
    public LocalDate getJatuhTempoTerpilih() {
        return jatuhTempoTerpilih;
    }
}