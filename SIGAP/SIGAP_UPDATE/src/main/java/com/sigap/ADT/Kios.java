package com.sigap.ADT;

public class Kios {

    private String idKios;
    private double hargaKios;
    private double panjangKios;
    private double lebarKios;
    private double luasKios;
    private String deskripsi;
    private String stsKetersediaan;
    private String stsKios;


    public Kios() {
    }


    public Kios(String idKios, double hargaKios, double panjangKios, double lebarKios, String stsKetersediaan, String stsKios, String deskripsi) {
        this.idKios = idKios;
        this.hargaKios = hargaKios;
        this.panjangKios = panjangKios;
        this.lebarKios = lebarKios;
        this.luasKios = panjangKios * lebarKios;
        this.stsKetersediaan = stsKetersediaan;
        this.stsKios = stsKios;
        this.deskripsi = deskripsi;
    }


    public Kios(String idKios, double hargaKios, double panjangKios, double lebarKios, double luasKios, String deskripsi, String stsKetersediaan, String stsKios) {
        this.idKios = idKios;
        this.hargaKios = hargaKios;
        this.panjangKios = panjangKios;
        this.lebarKios = lebarKios;
        this.luasKios = luasKios;
        this.deskripsi = deskripsi;
        this.stsKetersediaan = stsKetersediaan;
        this.stsKios = stsKios;
    }


    public String getIdKios() {
        return idKios;
    }

    public double getHargaKios() {
        return hargaKios;
    }

    public double getPanjangKios() {
        return panjangKios;
    }

    public double getLebarKios() {
        return lebarKios;
    }

    public double getLuasKios() {
        return luasKios;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getStsKetersediaan() {
        return stsKetersediaan;
    }

    public String getStsKios() {
        return stsKios;
    }


    public void setIdKios(String idKios) {
        this.idKios = idKios;
    }

    public void setHargaKios(double hargaKios) {
        this.hargaKios = hargaKios;
    }

    public void setPanjangKios(double panjangKios) {
        this.panjangKios = panjangKios;
    }

    public void setLebarKios(double lebarKios) {
        this.lebarKios = lebarKios;
    }

    public void setLuasKios(double luasKios) {
        this.luasKios = luasKios;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setStsKetersediaan(String stsKetersediaan) {
        this.stsKetersediaan = stsKetersediaan;
    }

    public void setStsKios(String stsKios) {
        this.stsKios = stsKios;
    }


    @Override
    public String toString() {
        return "Kios{" +
                "id=" + idKios +
                ", luas=" + luasKios +
                ", harga=" + hargaKios  +
                ", ketersediaan=" + stsKetersediaan +
                ", sts=" + stsKios  +
                "}";
    }
}