package com.sigap.controller;

import com.sigap.ADT.Karyawan;
import com.sigap.APP.CRUD_Karyawan;
import com.sigap.util.Session;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private AnchorPane loginCard;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private static final String MAIN_FXML = "/com/sigap/view/MainView.fxml";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(this::onLogin);
        hideError();

        // Enter di field password juga trigger login (defaultButton sudah handle ini,
        // tapi jaga-jaga kalau fokus di username lalu tekan Enter)
        usernameField.setOnAction(this::onLogin);
        passwordField.setOnAction(this::onLogin);
    }

    @FXML
    private void onLogin(ActionEvent event) {
        hideError();

        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password wajib diisi.");
            return;
        }

        loginButton.setDisable(true);
        try {
            Karyawan matched = authenticate(username, password);

            if (matched == null) {
                showError("Username atau password salah.");
                return;
            }

            if (!"Aktif".equalsIgnoreCase(
                    matched.getStsKaryawan() != null ? matched.getStsKaryawan().trim() : "")) {
                showError("Akun ini tidak aktif. Hubungi admin.");
                return;
            }

            Session.setLoggedInUser(matched);
            goToMain();

        } catch (Exception e) {
            showError("Gagal terhubung ke database.");
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
        }
    }

    private Karyawan authenticate(String username, String password) throws Exception {
        Karyawan k = CRUD_Karyawan.findByUsername(username);
        if (k == null) return null;
        if (k.getPassword() == null || !k.getPassword().equals(password)) return null;
        return k;
    }

    private void goToMain() {
        try {
            URL fxmlUrl = getClass().getResource(MAIN_FXML);
            if (fxmlUrl == null) {
                showError("MainView.fxml tidak ditemukan.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent mainRoot = loader.load();

            Scene currentScene = rootPane.getScene();
            if (currentScene != null) {
                currentScene.setRoot(mainRoot);
            } else {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setScene(new Scene(mainRoot));
                stage.setFullScreen(true);
            }
        } catch (Exception ex) {
            showError("Gagal membuka halaman utama.");
            ex.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}