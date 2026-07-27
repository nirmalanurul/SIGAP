package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.database.DBConnect;
import com.sigap.util.Session;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Dashboard untuk role Manajer.
 * Modul yang menjadi hak akses Manajer saat ini: Laporan Transaksi Penyewaan
 * dan Laporan Tagihan Pembayaran Sewa (lihat DashboardManagerView.fxml).
 */
public class DashboardManagerController implements Initializable {

    private static final String LOGIN_FXML = "/com/sigap/view/Login.fxml";
    private static final String DASHBOARD_CONTENT_FXML = "/com/sigap/view/DashboardManagerContentView.fxml";

    // Path classpath ke file .jasper (SUDAH di-compile lewat Jaspersoft Studio),
    // bukan .jrxml — supaya tidak perlu compile-runtime yang rawan gagal di komputer lain
    private static final String REPORT_PENYEWAAN_JASPER = "/report/LaporanTransaksiPenyewaan.jasper";
    private static final String REPORT_TAGIHAN_JASPER   = "/report/LaporanTagihanPembayaranSewa.jasper";

    @FXML private StackPane contentArea;
    @FXML private Label     lblTanggal;
    @FXML private Label     lblJam;
    @FXML private Label     lblUserName;
    @FXML private Label     lblUserRole;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupClock();
        setupUserInfo();
        onDashboard(null);
    }

    private void setupUserInfo() {
        Karyawan user = Session.getLoggedInUser();
        if (user != null) {
            lblUserName.setText(user.getNamaKaryawan());
            lblUserRole.setText(user.getJabatanKaryawan());
        }
    }

    // ================= DASHBOARD =================

    /** Menu "Dashboard" — tampilkan ringkasan KPI + diagram laporan bulanan. */
    @FXML
    private void onDashboard(ActionEvent event) {
        try {
            URL url = getClass().getResource(DASHBOARD_CONTENT_FXML);
            if (url == null) {
                System.err.println("[DashboardManagerController] "
                        + DASHBOARD_CONTENT_FXML + " tidak ditemukan, fallback ke placeholder.");
                return;
            }
            Parent view = FXMLLoader.load(url);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("[DashboardManagerController] Gagal memuat dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Placeholder fallback kalau DashboardManagerContentView belum/gagal dimuat. */
    @FXML
    public void onLogout(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(LOGIN_FXML);
            if (fxmlUrl == null) {
                tampilkanError("File login tidak ditemukan di classpath: " + LOGIN_FXML
                        + "\nPeriksa nama file FXML login kamu yang sebenarnya (mungkin bukan \"Login.fxml\") "
                        + "lalu sesuaikan konstanta LOGIN_FXML di DashboardManagerController.");
                return;
            }
            Parent loginRoot = FXMLLoader.load(fxmlUrl);
            Session.clear();
            Scene scene = contentArea.getScene();
            scene.setRoot(loginRoot);
        } catch (Exception e) {
            System.err.println("[DashboardManagerController] Gagal logout: " + e.getMessage());
            e.printStackTrace();
            tampilkanError("Gagal kembali ke halaman login: "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // ================= LAPORAN =================

    @FXML
    private void onLaporanPenyewaan(ActionEvent event) {
        cetakLaporan(REPORT_PENYEWAAN_JASPER, "LaporanTransaksiPenyewaan");
    }

    @FXML
    private void onLaporanTagihan(ActionEvent event) {
        cetakLaporan(REPORT_TAGIHAN_JASPER, "LaporanTagihanPembayaranSewa");
    }

    /**
     * Load laporan dari file .jasper (sudah di-compile sebelumnya via Jaspersoft Studio),
     * fill dari database, export ke PDF, lalu buka otomatis lewat aplikasi PDF default OS.
     *
     * @param resourcePath path .jasper di classpath, mis. "/report/xxx.jasper"
     * @param namaFile     nama file PDF output (tanpa ekstensi)
     */
    private void cetakLaporan(String resourcePath, String namaFile) {
        try (Connection conn = new DBConnect().conn;
             InputStream jasperStream = getClass().getResourceAsStream(resourcePath)) {

            if (jasperStream == null) {
                tampilkanError("File .jasper tidak ditemukan di classpath: " + resourcePath
                        + "\nPastikan file ada di src/main/resources" + resourcePath
                        + " (compile dulu dari jrxml lewat Jaspersoft Studio) dan sudah di-rebuild.");
                return;
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

            Map<String, Object> params = new HashMap<>();
            // tambahkan parameter di sini kalau report memakainya, contoh:
            // params.put("idKaryawan", Session.getLoggedInUser().getIdKaryawan());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

            File outputFile = new File(System.getProperty("java.io.tmpdir"), namaFile + ".pdf");
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());

            boolean berhasilDibuka = false;
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                try {
                    Desktop.getDesktop().open(outputFile);
                    berhasilDibuka = true;
                } catch (Exception exOpen) {
                    System.err.println("[DashboardManagerController] Desktop.open gagal: " + exOpen.getMessage());
                }
            }

            if (!berhasilDibuka) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Laporan Berhasil Dibuat");
                info.setHeaderText(null);
                info.setContentText("PDF berhasil dibuat, tapi tidak bisa dibuka otomatis di komputer ini.\n"
                        + "Buka manual dari lokasi berikut:\n" + outputFile.getAbsolutePath());
                if (contentArea.getScene() != null) {
                    info.initOwner(contentArea.getScene().getWindow());
                    info.initModality(javafx.stage.Modality.WINDOW_MODAL);
                }
                info.showAndWait();
            }

        } catch (Exception e) {
            System.err.println("[DashboardManagerController] Gagal mencetak laporan: " + e.getMessage());
            e.printStackTrace();
            tampilkanError("Gagal membuka laporan: " + e.getMessage());
        }
    }

    private void tampilkanError(String pesan) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Laporan Gagal Dibuka");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        // Tempelkan ke window aplikasi supaya dialog tidak muncul sebagai window
        // lepas/independen, melainkan modal & selalu di atas app.
        if (contentArea.getScene() != null) {
            alert.initOwner(contentArea.getScene().getWindow());
            alert.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
        alert.showAndWait();
    }

    private void setupClock() {
        DateTimeFormatter fmtTgl = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        DateTimeFormatter fmtJam = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            lblTanggal.setText(now.format(fmtTgl));
            lblJam.setText(now.format(fmtJam));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
}