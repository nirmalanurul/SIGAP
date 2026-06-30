package com.sigap.ADT;

import java.time.LocalDate;

public class Penyewa {

    private String idPenyewa;
    private String namaPenyewa;
    private String nik;
    private String noTelp;
    private String alamat;
    private LocalDate tglDaftar;
    private String stsPenyewa;

    public Penyewa() {
    }

    public Penyewa(String idPenyewa, String namaPenyewa, String nik,
                   String noTelp, String alamat, LocalDate tglDaftar,
                   String stsPenyewa) {
        this.idPenyewa = idPenyewa;
        this.namaPenyewa = namaPenyewa;
        this.nik = nik;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.tglDaftar = tglDaftar;
        this.stsPenyewa = stsPenyewa;
    }

    public String getIdPenyewa() {
        return idPenyewa;
    }

    public void setIdPenyewa(String idPenyewa) {
        this.idPenyewa = idPenyewa;
    }

    public String getNamaPenyewa() {
        return namaPenyewa;
    }

    public void setNamaPenyewa(String namaPenyewa) {
        this.namaPenyewa = namaPenyewa;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public LocalDate getTglDaftar() {
        return tglDaftar;
    }

    public void setTglDaftar(LocalDate tglDaftar) {
        this.tglDaftar = tglDaftar;
    }

    public String getStsPenyewa() {
        return stsPenyewa;
    }

    public void setStsPenyewa(String stsPenyewa) {
        this.stsPenyewa = stsPenyewa;
    }

    @Override
    public String toString() {
        return "Penyewa{" +
                "id=" + idPenyewa +
                ", nama=" + namaPenyewa +
                ", nik=" + nik +
                "}";
    }
}