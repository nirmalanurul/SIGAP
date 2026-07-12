package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.Util.Session;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Dashboard untuk role Admin.
 * Hak akses: Karyawan, Kios, Biaya Tambahan.
 */
public class DashboardAdminController implements Initializable {

    private static final String LOGIN_FXML = "/com/sigap/view/Login.fxml";

    @FXML private StackPane contentArea;
    @FXML private Label     lblTanggal;
    @FXML private Label     lblJam;
    @FXML private Label     lblUserName;
    @FXML private Label     lblUserRole;
    @FXML private Button    btnMenuKaryawan;
    @FXML private Button    btnMenuKios;
    @FXML private Button    btnMenuBiayaTambahan;

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
        setupUserInfo();
        onMenuKaryawan();
    }

    private void setupUserInfo() {
        Karyawan user = Session.getLoggedInUser();
        if (user != null) {
            lblUserName.setText(user.getNamaKaryawan());
            lblUserRole.setText(user.getJabatanKaryawan());
        }
    }

    @FXML
    public void onMenuKaryawan() {
        loadView("/com/sigap/view/KaryawanView.fxml");
        btnMenuKaryawan.setStyle(STYLE_ACTIVE);
        btnMenuKios.setStyle(STYLE_INACTIVE);
        btnMenuBiayaTambahan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuKios() {
        loadView("/com/sigap/view/KiosView.fxml");
        btnMenuKaryawan.setStyle(STYLE_INACTIVE);
        btnMenuKios.setStyle(STYLE_ACTIVE);
        btnMenuBiayaTambahan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuBiayaTambahan() {
        loadView("/com/sigap/view/BiayaTambahanView.fxml");
        btnMenuKaryawan.setStyle(STYLE_INACTIVE);
        btnMenuKios.setStyle(STYLE_INACTIVE);
        btnMenuBiayaTambahan.setStyle(STYLE_ACTIVE);
    }

    @FXML
    public void onLogout(ActionEvent event) {
        try {
            Session.clear();
            URL fxmlUrl = getClass().getResource(LOGIN_FXML);
            Parent loginRoot = FXMLLoader.load(fxmlUrl);
            Scene scene = contentArea.getScene();
            scene.setRoot(loginRoot);
        } catch (Exception e) {
            System.err.println("[DashboardAdminController] Gagal logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("[DashboardAdminController] FXML tidak ditemukan: " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(url);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("[DashboardAdminController] Gagal memuat view: " + e.getMessage());
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