package com.sigap.controller;

import com.sigap.ADT.DetailGambarKios;
import com.sigap.ADT.Kios;
import com.sigap.APP.CRUD_Kios;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class KiosDetailController {

    @FXML
    private Label lblDeskripsi;

    @FXML
    private Label lblHarga;

    @FXML
    private Label lblId;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblUkuran;

    @FXML
    private VBox vboxGaleriFoto;

    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    private boolean perluRefresh = false;

    public void setData(Kios k) {
        lblId.setText(k.getIdKios());
        lblHarga.setText("Rp " + FMT_RUPIAH.format(k.getHargaKios()));
        lblUkuran.setText(k.getPanjangKios() + "m x " + k.getLebarKios() + "m  (" + k.getLuasKios() + " m²)");
        lblDeskripsi.setText(k.getDeskripsi() == null ? "-" : k.getDeskripsi());
        lblStatus.setText(k.getStsKios());

        boolean aktif = "Aktif".equalsIgnoreCase(k.getStsKios());
        lblStatus.setStyle(aktif
                ? "-fx-font-size:11px;-fx-font-weight:700;-fx-text-fill:#1E8A3C;-fx-background-color:#E0F5E8;-fx-background-radius:8;-fx-padding:3 10;"
                : "-fx-font-size:11px;-fx-font-weight:700;-fx-text-fill:#C0392B;-fx-background-color:#FFE8E8;-fx-background-radius:8;-fx-padding:3 10;");

        vboxGaleriFoto.getChildren().clear();
        try {
            List<DetailGambarKios> daftarFoto = CRUD_Kios.getFoto(k.getIdKios());
            if (daftarFoto.isEmpty()) {
                Label kosong = new Label("Tidak ada foto untuk kios ini.");
                kosong.setStyle("-fx-font-size:13px;-fx-text-fill:#AAA;-fx-padding:20;");
                vboxGaleriFoto.getChildren().add(kosong);
            } else {
                for (DetailGambarKios g : daftarFoto) {
                    File f = new File(g.getNamaFileGambar());
                    if (!f.exists()) continue;

                    ImageView iv = new ImageView(new Image(f.toURI().toString()));
                    iv.setFitWidth(500);
                    iv.setPreserveRatio(true);
                    iv.setStyle("-fx-background-radius:8;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),6,0,0,2);");
                    vboxGaleriFoto.getChildren().add(iv);
                }
            }
        } catch (Exception e) {
            Label error = new Label("Gagal memuat foto: " + e.getMessage());
            error.setStyle("-fx-font-size:12px;-fx-text-fill:#C0392B;-fx-padding:20;");
            vboxGaleriFoto.getChildren().add(error);
        }
    }

    @FXML
    void onTutup(ActionEvent event) {
        Stage stage = (Stage) lblId.getScene().getWindow();
        stage.close();
    }

    public boolean isPerluRefresh() {
        return perluRefresh;
    }
}