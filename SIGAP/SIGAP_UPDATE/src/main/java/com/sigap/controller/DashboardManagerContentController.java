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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.net.URL;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller untuk DashboardManagerContentView.fxml.
 * Menampilkan ringkasan KPI dan diagram laporan bulanan untuk role Manajer.
 *
 * Karena belum ada stored procedure khusus laporan bulanan, data diambil lewat
 * getAll() yang sudah ada di masing-masing CRUD lalu diagregasi di sisi Java
 * (filter per bulan pakai YearMonth). Kalau datanya sudah besar, sebaiknya
 * dipindah jadi stored procedure agregasi di database supaya lebih cepat.
 */
public class DashboardManagerContentController implements Initializable {

    @FXML private Label lblPeriode;
    @FXML private Label lblTotalPendapatan;
    @FXML private Label lblPenyewaanAktif;
    @FXML private Label lblTagihanBelumLunas;
    @FXML private Label lblKiosTersewa;

    @FXML private BarChart<String, Number> chartPendapatanBulanan;
    @FXML private CategoryAxis axisBulan;
    @FXML private NumberAxis axisPendapatan;

    @FXML private PieChart chartStatusTagihan;

    @FXML private BarChart<String, Number> chartPenyewaanBulanan;
    @FXML private CategoryAxis axisBulanPenyewaan;
    @FXML private NumberAxis axisJumlahPenyewaan;

    private static final NumberFormat FMT_RUPIAH = NumberFormat.getNumberInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter FMT_BULAN =
            DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("id", "ID"));

    private static final int JUMLAH_BULAN_TREN = 6;

    // Status persis yang dipakai di database — SESUAIKAN kalau ejaan/kapitalisasi
    // Sts_Tagihan_Pembayaran / Sts_Penyewaan / Sts_Kios kamu berbeda.
    private static final String STATUS_LUNAS = "Lunas";
    private static final String STATUS_BELUM_LUNAS = "Belum Lunas";
    private static final String STATUS_DIBATALKAN_TAGIHAN = "Dibatalkan";
    private static final String STATUS_PENYEWAAN_BERLANGSUNG = "Berlangsung";
    private static final String STATUS_KIOS_DISEWAKAN = "Disewakan";

    // Data mentah dimuat sekali di initialize(), dipakai ulang oleh semua panel
    // (KPI + 3 chart) supaya tidak query berkali-kali ke database.
    private List<TagihanPembayaranSewa> semuaTagihan = List.of();
    private List<Penyewaan> semuaPenyewaan = List.of();
    private List<Kios> semuaKios = List.of();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        YearMonth bulanIni = YearMonth.now();
        lblPeriode.setText("Ringkasan & Laporan Bulanan — "
                + bulanIni.atDay(1).format(FMT_BULAN));

        muatDataMentah();

        loadKpi(bulanIni);
        loadChartPendapatanBulanan();
        loadChartStatusTagihan(bulanIni);
        loadChartPenyewaanBulanan();
    }

    /** Ambil semua data sekali lewat CRUD getAll(); kalau gagal konek DB, biarkan list kosong. */
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

    private void loadKpi(YearMonth bulanIni) {
        // Total Pendapatan Bulan Ini = jumlah Total_Dibayar dari tagihan yang
        // Tgl_Bayar-nya jatuh di bulan berjalan.
        double totalPendapatan = semuaTagihan.stream()
                .filter(t -> t.getTglBayar() != null && YearMonth.from(t.getTglBayar()).equals(bulanIni))
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

    /** Pendapatan (Total_Dibayar berdasarkan Tgl_Bayar) untuk N bulan terakhir. */
    private void loadChartPendapatanBulanan() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pendapatan");

        for (YearMonth bulan : bulanTerakhir(JUMLAH_BULAN_TREN)) {
            double totalBulanIni = semuaTagihan.stream()
                    .filter(t -> t.getTglBayar() != null && YearMonth.from(t.getTglBayar()).equals(bulan))
                    .mapToDouble(TagihanPembayaranSewa::getTotalDibayar)
                    .sum();
            series.getData().add(new XYChart.Data<>(labelBulan(bulan), totalBulanIni));
        }

        chartPendapatanBulanan.setData(FXCollections.observableArrayList(series));
    }

    /** Sebaran status tagihan yang Tgl_Jatuh_Tempo-nya jatuh di bulan berjalan. */
    private void loadChartStatusTagihan(YearMonth bulanIni) {
        long lunas = 0, belumLunas = 0, dibatalkan = 0;

        for (TagihanPembayaranSewa t : semuaTagihan) {
            if (t.getTglJatuhTempo() == null || !YearMonth.from(t.getTglJatuhTempo()).equals(bulanIni)) continue;

            String status = t.getStsTagihanPembayaran();
            if (STATUS_LUNAS.equalsIgnoreCase(status)) lunas++;
            else if (STATUS_DIBATALKAN_TAGIHAN.equalsIgnoreCase(status)) dibatalkan++;
            else belumLunas++; // termasuk "Belum Lunas" atau status lain yang belum dikenali
        }

        chartStatusTagihan.setData(FXCollections.observableArrayList(
                new PieChart.Data("Lunas", lunas),
                new PieChart.Data("Belum Lunas", belumLunas),
                new PieChart.Data("Dibatalkan", dibatalkan)
        ));
    }

    /** Jumlah Penyewaan baru (berdasarkan Tgl_Penyewaan) untuk N bulan terakhir. */
    private void loadChartPenyewaanBulanan() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Penyewaan Baru");

        for (YearMonth bulan : bulanTerakhir(JUMLAH_BULAN_TREN)) {
            long jumlahBulanIni = semuaPenyewaan.stream()
                    .filter(p -> p.getTglPenyewaan() != null && YearMonth.from(p.getTglPenyewaan()).equals(bulan))
                    .count();
            series.getData().add(new XYChart.Data<>(labelBulan(bulan), jumlahBulanIni));
        }

        chartPenyewaanBulanan.setData(FXCollections.observableArrayList(series));
    }

    /** N bulan terakhir termasuk bulan berjalan, urut dari yang paling lama ke yang terbaru. */
    private List<YearMonth> bulanTerakhir(int jumlahBulan) {
        YearMonth sekarang = YearMonth.now();
        return java.util.stream.IntStream.rangeClosed(0, jumlahBulan - 1)
                .mapToObj(i -> sekarang.minusMonths(jumlahBulan - 1 - i))
                .toList();
    }

    private String labelBulan(YearMonth bulan) {
        return bulan.getMonth().getDisplayName(TextStyle.SHORT, new Locale("id", "ID"));
    }
}