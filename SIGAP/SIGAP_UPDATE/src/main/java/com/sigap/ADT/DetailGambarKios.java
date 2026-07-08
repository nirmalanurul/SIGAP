package com.sigap.ADT;

public class DetailGambarKios {
    private String idDetailGambar;
    private String idKios;
    private String namaFileGambar;

    public DetailGambarKios(String idDetailGambar, String idKios, String namaFileGambar) {
        this.idDetailGambar = idDetailGambar;
        this.idKios = idKios;
        this.namaFileGambar = namaFileGambar;
    }

    public String getIdDetailGambar() {
        return idDetailGambar;
    }

    public String getIdKios() {
        return idKios;
    }

    public String getNamaFileGambar() {
        return namaFileGambar;
    }

    public void setIdDetailGambar(String idDetailGambar) {
        this.idDetailGambar = idDetailGambar;
    }

    public void setIdKios(String idKios) {
        this.idKios = idKios;
    }

    public void setNamaFileGambar(String namaFileGambar) {
        this.namaFileGambar = namaFileGambar;
    }
}