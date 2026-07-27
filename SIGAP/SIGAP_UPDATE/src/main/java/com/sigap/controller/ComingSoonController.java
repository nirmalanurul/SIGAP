package com.sigap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ComingSoonController {

    @FXML private Label lblJudul;
    @FXML private Label lblKeterangan;

    public void setJudul(String namaModul) {
        lblJudul.setText(namaModul + " — Belum Tersedia");
        lblKeterangan.setText("Modul \"" + namaModul + "\" sedang dalam pengembangan dan akan segera hadir.");
    }
}