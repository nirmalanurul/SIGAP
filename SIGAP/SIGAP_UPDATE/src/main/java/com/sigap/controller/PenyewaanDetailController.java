package com.sigap.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller untuk PenyewaanDetailDialog.fxml — popup detail satu transaksi penyewaan,
 * dibuka dari klik kolom ID di tabel Daftar Penyewaan (PenyewaanController).
 */
public class PenyewaanDetailController {

    @FXML
    private Label lblId;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblKios;
    @FXML
    private Label lblPenyewa;
    @FXML
    private Label lblKaryawan;
    @FXML
    private Label lblTglMulai;
    @FXML
    private Label lblTglSelesai;
    @FXML
    private Label lblTglTransaksi;

    /**
     * Mengisi dialog dengan data transaksi penyewaan yang sudah di-label-join
     * (ID kios/penyewa/karyawan sudah diubah jadi "ID - Nama" oleh pemanggil)
     * dan tanggal yang sudah diformat oleh pemanggil (format nama bulan).
     */
    public void setData(String idPenyewaan, String statusPenyewaan,
                         String labelKios, String labelPenyewa, String labelKaryawan,
                         String tglMulaiText, String tglSelesaiText, String tglTransaksiText) {
        lblId.setText(idPenyewaan);
        lblStatus.setText(statusPenyewaan);
        lblStatus.setStyle(styleBadgeStatus(statusPenyewaan));
        lblKios.setText(labelKios);
        lblPenyewa.setText(labelPenyewa);
        lblKaryawan.setText(labelKaryawan);
        lblTglMulai.setText(tglMulaiText);
        lblTglSelesai.setText(tglSelesaiText);
        lblTglTransaksi.setText(tglTransaksiText);
    }

    private String styleBadgeStatus(String status) {
        String base = "-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 3 10;";
        return switch (status == null ? "" : status) {
            case "Berlangsung" -> "-fx-text-fill: #1E8A3C; -fx-background-color: #E0F5E8;" + base;
            case "Selesai"     -> "-fx-text-fill: #555555; -fx-background-color: #EAEAEA;" + base;
            case "Dibatalkan"  -> "-fx-text-fill: #C0392B; -fx-background-color: #FFE8E8;" + base;
            default            -> "-fx-text-fill: #B8860B; -fx-background-color: #FFF3D6;" + base; // Menunggu
        };
    }

    @FXML
    void onTutup(ActionEvent event) {
        Stage stage = (Stage) lblId.getScene().getWindow();
        stage.close();
    }
}