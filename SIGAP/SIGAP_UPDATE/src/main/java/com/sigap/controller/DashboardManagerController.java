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
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Dashboard untuk role Manajer.
 * Belum ada modul yang menjadi hak akses Manajer, sehingga dashboard ini
 * hanya menampilkan placeholder kosong. Sidebar sengaja tidak memiliki
 * tombol menu apa pun (lihat DashboardManagerView.fxml).
 */
public class DashboardManagerController implements Initializable {

    private static final String LOGIN_FXML = "/com/sigap/view/Login.fxml";
    private static final String COMING_SOON_FXML = "/com/sigap/view/ComingSoonView.fxml";

    @FXML private StackPane contentArea;
    @FXML private Label     lblTanggal;
    @FXML private Label     lblJam;
    @FXML private Label     lblUserName;
    @FXML private Label     lblUserRole;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupClock();
        setupUserInfo();
        showEmptyDashboard();
    }

    private void setupUserInfo() {
        Karyawan user = Session.getLoggedInUser();
        if (user != null) {
            lblUserName.setText(user.getNamaKaryawan());
            lblUserRole.setText(user.getJabatanKaryawan());
        }
    }

    private void showEmptyDashboard() {
        try {
            URL url = getClass().getResource(COMING_SOON_FXML);
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            ComingSoonController controller = loader.getController();
            controller.setJudul("Dashboard Manajer");

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("[DashboardManagerController] Gagal memuat placeholder: " + e.getMessage());
            e.printStackTrace();
        }
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
            System.err.println("[DashboardManagerController] Gagal logout: " + e.getMessage());
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