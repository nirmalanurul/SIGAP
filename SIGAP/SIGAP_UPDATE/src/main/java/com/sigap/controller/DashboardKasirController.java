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
 * Dashboard untuk role Kasir.
 * Hak akses: Penyewa, Penyewaan, Tagihan Pembayaran Sewa.
 *
 * Catatan: modul Penyewaan dan Tagihan Pembayaran Sewa belum memiliki
 * Controller/FXML/CRUD sendiri, sehingga untuk sementara ditampilkan
 * sebagai placeholder "Coming Soon" (lihat ComingSoonController).
 */
public class DashboardKasirController implements Initializable {

    private static final String LOGIN_FXML = "/com/sigap/view/Login.fxml";
    private static final String COMING_SOON_FXML = "/com/sigap/view/ComingSoonView.fxml";

    @FXML private StackPane contentArea;
    @FXML private Label     lblTanggal;
    @FXML private Label     lblJam;
    @FXML private Label     lblUserName;
    @FXML private Label     lblUserRole;
    @FXML private Button    btnMenuPenyewa;
    @FXML private Button    btnMenuPenyewaan;
    @FXML private Button    btnMenuTagihan;

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
        // Default tampilkan Penyewa saat pertama buka
        onMenuPenyewa();
    }

    private void setupUserInfo() {
        Karyawan user = Session.getLoggedInUser();
        if (user != null) {
            lblUserName.setText(user.getNamaKaryawan());
            lblUserRole.setText(user.getJabatanKaryawan());
        }
    }

    @FXML
    public void onMenuPenyewa() {
        loadView("/com/sigap/view/PenyewaView.fxml", null);
        btnMenuPenyewa.setStyle(STYLE_ACTIVE);
        btnMenuPenyewaan.setStyle(STYLE_INACTIVE);
        btnMenuTagihan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuPenyewaan() {
        loadView(COMING_SOON_FXML, "Penyewaan");
        btnMenuPenyewa.setStyle(STYLE_INACTIVE);
        btnMenuPenyewaan.setStyle(STYLE_ACTIVE);
        btnMenuTagihan.setStyle(STYLE_INACTIVE);
    }

    @FXML
    public void onMenuTagihan() {
        loadView(COMING_SOON_FXML, "Tagihan Pembayaran Sewa");
        btnMenuPenyewa.setStyle(STYLE_INACTIVE);
        btnMenuPenyewaan.setStyle(STYLE_INACTIVE);
        btnMenuTagihan.setStyle(STYLE_ACTIVE);
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
            System.err.println("[DashboardKasirController] Gagal logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Memuat FXML ke contentArea. Jika judulPlaceholder tidak null dan
     * controller hasil load adalah ComingSoonController, judul akan
     * disetel otomatis.
     */
    private void loadView(String fxmlPath, String judulPlaceholder) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("[DashboardKasirController] FXML tidak ditemukan: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            if (judulPlaceholder != null) {
                Object controller = loader.getController();
                if (controller instanceof ComingSoonController) {
                    ((ComingSoonController) controller).setJudul(judulPlaceholder);
                }
            }

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("[DashboardKasirController] Gagal memuat view: " + e.getMessage());
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