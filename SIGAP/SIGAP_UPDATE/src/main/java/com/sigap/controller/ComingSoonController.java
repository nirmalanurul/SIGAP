package com.sigap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller generik untuk halaman placeholder "Coming Soon".
 * Dipakai sementara untuk modul yang menu-nya sudah tampil di sidebar
 * tapi implementasinya (Controller/FXML/CRUD) belum dibuat, misalnya
 * Penyewaan dan Tagihan Pembayaran Sewa pada dashboard Kasir.
 */
public class ComingSoonController {

    @FXML private Label lblJudul;
    @FXML private Label lblKeterangan;

    public void setJudul(String namaModul) {
        lblJudul.setText(namaModul + " — Belum Tersedia");
        lblKeterangan.setText("Modul \"" + namaModul + "\" sedang dalam pengembangan dan akan segera hadir.");
    }
}