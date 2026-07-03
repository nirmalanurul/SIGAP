package com.sigap.controller;
/// ///
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private Label loadingLabel;
    @FXML private VBox mainContent;
    @FXML private ProgressBar progressBar;
    @FXML private AnchorPane rootPane;

    private static final double LOADING_DURATION_MS  = 4000;
    private static final double FADE_OUT_DURATION_MS = 700;
    private static final String LOGIN_FXML = "/com/sigap/view/Login.fxml";

    private Timeline progressTimeline;
    private boolean  isTransitioning = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playFadeIn();
        startProgressAnimation();
        Platform.runLater(this::registerInputListeners);
    }

    private void playFadeIn() {
        mainContent.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(900), mainContent);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void startProgressAnimation() {
        progressBar.setProgress(0.0);
        progressTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0.0)),
                new KeyFrame(Duration.millis(LOADING_DURATION_MS),
                        new KeyValue(progressBar.progressProperty(), 1.0, Interpolator.EASE_OUT))
        );
        progressTimeline.setOnFinished(e -> onLoadingFinished());
        progressTimeline.play();
    }

    private void onLoadingFinished() {
        loadingLabel.setText("Memuat aplikasi...");
        loadingLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A3A8F; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        PauseTransition pause = new PauseTransition(Duration.millis(350));
        pause.setOnFinished(e -> doTransition());
        pause.play();
    }

    private void registerInputListeners() {
        Scene scene = rootPane.getScene();
        if (scene == null) return;
        scene.addEventFilter(KeyEvent.KEY_PRESSED,     e -> handleUserInput());
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> handleUserInput());
    }

    private void handleUserInput() {
        if (isTransitioning) return;
        if (progressTimeline != null) progressTimeline.stop();
        progressBar.setProgress(1.0);
        doTransition();
    }

    private void doTransition() {
        if (isTransitioning) return;
        isTransitioning = true;
        FadeTransition fo = new FadeTransition(Duration.millis(FADE_OUT_DURATION_MS), rootPane);
        fo.setFromValue(1.0);
        fo.setToValue(0.0);
        fo.setOnFinished(e -> loadLoginView());
        fo.play();
    }

    private void loadLoginView() {
        try {
            URL fxmlUrl = getClass().getResource(LOGIN_FXML);
            if (fxmlUrl == null) {
                System.err.println("[SplashController] LoginView.fxml tidak ditemukan: " + LOGIN_FXML);
                Platform.exit();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent loginRoot = loader.load();

            Scene currentScene = rootPane.getScene();

            if (currentScene != null) {
                currentScene.setRoot(loginRoot);
            } else {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                Scene loginScene = new Scene(loginRoot);
                stage.setScene(loginScene);
                stage.setFullScreen(true);
            }

        } catch (Exception ex) {
            System.err.println("[SplashController] Gagal memuat LoginView: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}