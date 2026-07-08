package com.sigap.controller;

import com.sigap.ADT.Kios;
import com.sigap.ADT.DetailGambarKios;
import com.sigap.APP.CRUD_Kios;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class KiosCardController {

    @FXML
    private ImageView imgThumb;

    @FXML
    private Label lblHarga;

    @FXML
    private Label lblId;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblUkuran;

    @FXML
    private VBox rootCard;

    private static final NumberFormat FMT_RUPIAH =
            NumberFormat.getNumberInstance(new Locale("id", "ID"));

    public void setData(Kios k, Consumer<Kios> onCardClicked) {
        lblId.setText(k.getIdKios());
        lblHarga.setText("Rp " + FMT_RUPIAH.format(k.getHargaKios()));
        lblUkuran.setText(k.getPanjangKios() + "m x " + k.getLebarKios() + "m  ·  " + k.getLuasKios() + " m²");
        lblStatus.setText(k.getStsKios());

        boolean aktif = "Aktif".equalsIgnoreCase(k.getStsKios());
        lblStatus.setStyle(aktif
                ? "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;-fx-font-weight:700;-fx-font-size:10px;-fx-padding:2 8;-fx-background-radius:8;"
                : "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;-fx-font-weight:700;-fx-font-size:10px;-fx-padding:2 8;-fx-background-radius:8;");

        try {
            List<DetailGambarKios> foto = CRUD_Kios.getFoto(k.getIdKios());
            if (!foto.isEmpty()) {
                File f = new File(foto.get(0).getNamaFileGambar());
                if (f.exists()) {
                    imgThumb.setImage(new Image(f.toURI().toString(), 230, 130, false, true));
                }
            }
        } catch (Exception ignored) {
        }

        rootCard.setOnMouseClicked(e -> onCardClicked.accept(k));
    }
}