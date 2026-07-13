package com.sigap.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label     lblTanggal;
    @FXML private Label     lblJam;
    @FXML private Button    btnMenuPenyewa;
    @FXML private Button    btnMenuKios;
    @FXML private Button    btnMenuDenda;
    @FXML private Button    btnMenuKaryawan;
    @FXML private Button    btnMenuPenyewaan;

    private static final String STYLE_ACTIVE =
            "-fx-background-color: #2356C8; -fx-text-fill: WHITE; " +
                    "-fx-font-size: 13px; -fx-font-weight: 700; " +
                    "-fx-alignment: CENTER_LEFT; -fx-padding: 0 16; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;";

    private static final String STYLE_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #A0C0F0; " +
                    "-fx-font-size: 13px; -fx-font-weight: 700; " +
                    "-fx-alignment: CENTER_LEFT; -fx-padding: 0 16; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupClock();
        // Default tampilkan Penyewaan saat pertama buka (kasir langsung siap transaksi)
        onMenuPenyewaan();
    }

    @FXML
    public void onMenuPenyewaan() {
        loadView("/com/sigap/view/Penyewaan/PenyewaanView.fxml");
        btnMenuPenyewaan.setStyle(STYLE_ACTIVE);
        btnMenuPenyewa.setStyle(STYLE_INACTIVE);
        btnMenuKios.setStyle(STYLE_INACTIVE);
        btnMenuDenda.setStyle(STYLE_INACTIVE);
        btnMenuKaryawan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuPenyewa() {
        loadView("/com/sigap/view/PenyewaView.fxml");
        btnMenuPenyewaan.setStyle(STYLE_INACTIVE);
        btnMenuKios.setStyle(STYLE_INACTIVE);
        btnMenuPenyewa.setStyle(STYLE_ACTIVE);
        btnMenuDenda.setStyle(STYLE_INACTIVE);
        btnMenuKaryawan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuKios() {
        loadView("/com/sigap/view/KiosView.fxml");
        btnMenuPenyewaan.setStyle(STYLE_INACTIVE);
        btnMenuKios.setStyle(STYLE_ACTIVE);
        btnMenuPenyewa.setStyle(STYLE_INACTIVE);
        btnMenuDenda.setStyle(STYLE_INACTIVE);
        btnMenuKaryawan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuDenda(){
        loadView("/com/sigap/view/BiayaTambahanView.fxml");
        btnMenuPenyewaan.setStyle(STYLE_INACTIVE);
        btnMenuKios.setStyle(STYLE_INACTIVE);
        btnMenuPenyewa.setStyle(STYLE_INACTIVE);
        btnMenuDenda.setStyle(STYLE_ACTIVE);
        btnMenuKaryawan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuKaryawan() {
        loadView("/com/sigap/view/KaryawanView.fxml");
        btnMenuPenyewaan.setStyle(STYLE_INACTIVE);
        btnMenuPenyewa.setStyle(STYLE_INACTIVE);
        btnMenuKios.setStyle(STYLE_INACTIVE);
        btnMenuDenda.setStyle(STYLE_INACTIVE);
        btnMenuKaryawan.setStyle(STYLE_ACTIVE);
    }

    private void loadView(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("[MainController] FXML tidak ditemukan di classpath: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

            System.out.println("[MainController] Berhasil memuat: " + fxmlPath);

        } catch (Exception e) {
            System.err.println("[MainController] Gagal memuat view: " + fxmlPath);
            System.err.println("[MainController] Exception: " + e.getClass().getName() + " - " + e.getMessage());

            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("[MainController] Penyebab akar (cause): " + cause.getClass().getName() + " - " + cause.getMessage());
            }

            e.printStackTrace();
        }
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