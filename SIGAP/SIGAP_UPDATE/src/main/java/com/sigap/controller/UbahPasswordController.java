package com.sigap.controller;

import com.sigap.APP.CRUD_Karyawan;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class UbahPasswordController {

    @FXML private Label lblIdKaryawan;
    @FXML private Label lblNamaKaryawan;
    @FXML private PasswordField txtPasswordBaru;
    @FXML private PasswordField txtKonfirmasiPassword;
    @FXML private Button btnSimpan;
    @FXML private Button btnBatal;

    private String idKaryawan;
    private boolean berhasil = false;
//lakik
    public void setData(String idKaryawan, String namaKaryawan) {
        this.idKaryawan = idKaryawan;
        lblIdKaryawan.setText(idKaryawan);
        lblNamaKaryawan.setText(namaKaryawan);
    }

    public boolean isBerhasil() {
        return berhasil;
    }

    @FXML
    void onSimpan(ActionEvent event) {
        String pass1 = txtPasswordBaru.getText().trim();
        String pass2 = txtKonfirmasiPassword.getText().trim();

        StringBuilder sb = new StringBuilder();
        if (pass1.isEmpty()) {
            sb.append("• Password Baru wajib diisi.\n");
        } else if (pass1.length() < 8) {
            sb.append("• Password minimal 8 karakter.\n");
        } else if (pass1.length() > 30) {
            sb.append("• Password maksimal 30 karakter.\n");
        }
        if (!pass1.equals(pass2)) {
            sb.append("• Konfirmasi Password tidak sama dengan Password Baru.\n");
        }

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return;
        }

        try {
            CRUD_Karyawan.updatePassword(idKaryawan, pass1);
            berhasil = true;
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Password karyawan berhasil diubah.");
            closeDialog();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah Password", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onBatal(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnBatal.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (btnBatal != null && btnBatal.getScene() != null)
                alert.initOwner(btnBatal.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }
}