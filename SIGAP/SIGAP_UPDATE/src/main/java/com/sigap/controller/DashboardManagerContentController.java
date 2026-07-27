package com.sigap.controller;

import com.sigap.ADT.Kios;
import com.sigap.ADT.Penyewaan;
import com.sigap.ADT.TagihanPembayaranSewa;
import com.sigap.APP.CRUD_Kios;
import com.sigap.APP.CRUD_Penyewaan;
import com.sigap.APP.CRUD_TagihanPembayaranSewa;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.net.URL;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;


public class DashboardManagerContentController implements Initializable {

    @FXML private Label lblPeriode;
    @FXML private Label lblTotalPendapatan;
    @FXML private Label lblPenyewaanAktif;
    @FXML private Label lblTagihanBelumLunas;
    @FXML private Label lblKiosTersewa;

    @FXML private LineChart<String, Number> chartPendapatanBulanan;
    @FXML private CategoryAxis axisBulan;
    @FXML private NumberAxis axisPendapatan;

    @FXML private PieChart chartStatusTagihan;

    @FXML private LineChart<String, Number> chartPenyewaanBulanan;
    @FXML private CategoryAxis axisBulanPenyewaan;
    @FXML private NumberAxis axisJumlahPenyewaan;

    @FXML private ComboBox<String> cbBulan;
    @FXML private ComboBox<Integer> cbTahun;

    private static final NumberFormat FMT_RUPIAH = NumberFormat.getNumberInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_BULAN =
            DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID"));

    private static final int JUMLAH_BULAN_TREN = 6;


    private static final List<String> NAMA_BULAN = List.of(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    );

    private static final int JUMLAH_TAHUN_FILTER = 6;


    private static final String STATUS_LUNAS = "Lunas";
    private static final String STATUS_BELUM_LUNAS = "Belum Lunas";
    private static final String STATUS_DIBATALKAN_TAGIHAN = "Dibatalkan";
    private static final String STATUS_PENYEWAAN_BERLANGSUNG = "Berlangsung";
    private static final String STATUS_KIOS_DISEWAKAN = "Disewakan";


    private List<TagihanPembayaranSewa> semuaTagihan = List.of();
    private List<Penyewaan> semuaPenyewaan = List.of();
    private List<Kios> semuaKios = List.of();

    @FXML
    private void onCariPeriode(ActionEvent event) {
        String namaBulan = cbBulan.getSelectionModel().getSelectedItem();
        Integer tahun = cbTahun.getSelectionModel().getSelectedItem();

        if (namaBulan == null || tahun == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Periode Belum Lengkap");
            warning.setHeaderText(null);
            warning.setContentText("Pilih bulan dan tahun terlebih dahulu sebelum mencari.");
            javafx.stage.Window ownerWindow = cbBulan.getScene().getWindow();
            warning.initOwner(ownerWindow);
            warning.initModality(javafx.stage.Modality.WINDOW_MODAL);
            warning.showAndWait();
            return;
        }

        int nomorBulan = NAMA_BULAN.indexOf(namaBulan) + 1;
        YearMonth periodeTerpilih = YearMonth.of(tahun, nomorBulan);

        tampilkanPeriode(periodeTerpilih);
    }

    @FXML
    private void onResetPeriode(ActionEvent event) {
        cbBulan.getSelectionModel().clearSelection();
        cbTahun.getSelectionModel().clearSelection();

        tampilkanPeriode(YearMonth.now());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        muatDataMentah();
        isiComboBoxFilter();

        tampilkanPeriode(YearMonth.now());
    }

    private void tampilkanPeriode(YearMonth periode) {
        lblPeriode.setText("Ringkasan & Laporan Bulanan — "
                + periode.atDay(1).format(FMT_BULAN));

        loadKpi(periode);
        loadChartPendapatanBulanan(periode);
        loadChartStatusTagihan(periode);
        loadChartPenyewaanBulanan(periode);
    }

    private void isiComboBoxFilter() {
        cbBulan.setItems(FXCollections.observableArrayList(NAMA_BULAN));

        int tahunSekarang = YearMonth.now().getYear();
        List<Integer> daftarTahun = java.util.stream.IntStream
                .rangeClosed(tahunSekarang - (JUMLAH_TAHUN_FILTER - 1), tahunSekarang)
                .boxed()
                .sorted(java.util.Collections.reverseOrder())
                .toList();
        cbTahun.setItems(FXCollections.observableArrayList(daftarTahun));
    }

    private void muatDataMentah() {
        try {
            semuaTagihan = CRUD_TagihanPembayaranSewa.getAll();
        } catch (Exception e) {
            System.err.println("[DashboardManagerContentController] Gagal memuat Tagihan: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            semuaPenyewaan = CRUD_Penyewaan.getAll();
        } catch (Exception e) {
            System.err.println("[DashboardManagerContentController] Gagal memuat Penyewaan: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            semuaKios = CRUD_Kios.getAll();
        } catch (Exception e) {
            System.err.println("[DashboardManagerContentController] Gagal memuat Kios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadKpi(YearMonth periode) {
        // Total Pendapatan = jumlah Total_Dibayar dari tagihan yang
        // Tgl_Bayar-nya jatuh di periode yang dipilih.
        double totalPendapatan = semuaTagihan.stream()
                .filter(t -> t.getTglBayar() != null && YearMonth.from(t.getTglBayar()).equals(periode))
                .mapToDouble(TagihanPembayaranSewa::getTotalDibayar)
                .sum();

        long penyewaanAktif = semuaPenyewaan.stream()
                .filter(p -> STATUS_PENYEWAAN_BERLANGSUNG.equalsIgnoreCase(p.getStsPenyewaan()))
                .count();

        long tagihanBelumLunas = semuaTagihan.stream()
                .filter(t -> STATUS_BELUM_LUNAS.equalsIgnoreCase(t.getStsTagihanPembayaran()))
                .count();

        long kiosTersewa = semuaKios.stream()
                .filter(k -> STATUS_KIOS_DISEWAKAN.equalsIgnoreCase(k.getStsKios()))
                .count();

        lblTotalPendapatan.setText("Rp " + FMT_RUPIAH.format((long) totalPendapatan));
        lblPenyewaanAktif.setText(String.valueOf(penyewaanAktif));
        lblTagihanBelumLunas.setText(String.valueOf(tagihanBelumLunas));
        lblKiosTersewa.setText(kiosTersewa + " / " + semuaKios.size());
    }

    /** Pendapatan (Total_Dibayar berdasarkan Tgl_Bayar) untuk N bulan terakhir sampai periode. */
    private void loadChartPendapatanBulanan(YearMonth periode) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pendapatan");

        for (YearMonth bulan : bulanTerakhir(periode, JUMLAH_BULAN_TREN)) {
            double totalBulanIni = semuaTagihan.stream()
                    .filter(t -> t.getTglBayar() != null && YearMonth.from(t.getTglBayar()).equals(bulan))
                    .mapToDouble(TagihanPembayaranSewa::getTotalDibayar)
                    .sum();
            series.getData().add(new XYChart.Data<>(labelBulan(bulan), totalBulanIni));
        }

        chartPendapatanBulanan.setData(FXCollections.observableArrayList(series));
    }

    private void loadChartStatusTagihan(YearMonth periode) {
        long lunas = 0, belumLunas = 0, dibatalkan = 0;

        for (TagihanPembayaranSewa t : semuaTagihan) {
            if (t.getTglJatuhTempo() == null || !YearMonth.from(t.getTglJatuhTempo()).equals(periode)) continue;

            String status = t.getStsTagihanPembayaran();
            if (STATUS_LUNAS.equalsIgnoreCase(status)) lunas++;
            else if (STATUS_DIBATALKAN_TAGIHAN.equalsIgnoreCase(status)) dibatalkan++;
            else belumLunas++;
        }

        chartStatusTagihan.setData(FXCollections.observableArrayList(
                new PieChart.Data("Lunas", lunas),
                new PieChart.Data("Belum Lunas", belumLunas),
                new PieChart.Data("Dibatalkan", dibatalkan)
        ));
    }


    private void loadChartPenyewaanBulanan(YearMonth periode) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Penyewaan Baru");

        for (YearMonth bulan : bulanTerakhir(periode, JUMLAH_BULAN_TREN)) {
            long jumlahBulanIni = semuaPenyewaan.stream()
                    .filter(p -> p.getTglPenyewaan() != null && YearMonth.from(p.getTglPenyewaan()).equals(bulan))
                    .count();
            series.getData().add(new XYChart.Data<>(labelBulan(bulan), jumlahBulanIni));
        }

        chartPenyewaanBulanan.setData(FXCollections.observableArrayList(series));
    }


    private List<YearMonth> bulanTerakhir(YearMonth bulanAkhir, int jumlahBulan) {
        return java.util.stream.IntStream.rangeClosed(0, jumlahBulan - 1)
                .mapToObj(i -> bulanAkhir.minusMonths(jumlahBulan - 1 - i))
                .toList();
    }

    private String labelBulan(YearMonth bulan) {
        return bulan.getMonth().getDisplayName(TextStyle.SHORT, new Locale("id", "ID"));
    }
}