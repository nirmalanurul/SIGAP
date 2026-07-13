package com.sigap.ADT;

import java.time.LocalDate;

public class Penyewaan {

    private String idPenyewaan;
    private String idKaryawan;
    private String idPenyewa;
    private String idKios;
    private LocalDate tglMulai;
    private LocalDate tglSelesai;
    private LocalDate tglPenyewaan;
    private String stsPenyewaan;

    public Penyewaan() {
    }

    public Penyewaan(String idPenyewaan, String idKaryawan, String idPenyewa, String idKios,
                     LocalDate tglMulai, LocalDate tglSelesai, LocalDate tglPenyewaan, String stsPenyewaan) {
        this.idPenyewaan = idPenyewaan;
        this.idKaryawan = idKaryawan;
        this.idPenyewa = idPenyewa;
        this.idKios = idKios;
        this.tglMulai = tglMulai;
        this.tglSelesai = tglSelesai;
        this.tglPenyewaan = tglPenyewaan;
        this.stsPenyewaan = stsPenyewaan;
    }

    public String getIdPenyewaan() {
        return idPenyewaan;
    }

    public String getIdKaryawan() {
        return idKaryawan;
    }

    public String getIdPenyewa() {
        return idPenyewa;
    }

    public String getIdKios() {
        return idKios;
    }

    public LocalDate getTglMulai() {
        return tglMulai;
    }

    public LocalDate getTglSelesai() {
        return tglSelesai;
    }

    public LocalDate getTglPenyewaan() {
        return tglPenyewaan;
    }

    public String getStsPenyewaan() {
        return stsPenyewaan;
    }

    public void setIdPenyewaan(String idPenyewaan) {
        this.idPenyewaan = idPenyewaan;
    }

    public void setIdKaryawan(String idKaryawan) {
        this.idKaryawan = idKaryawan;
    }

    public void setIdPenyewa(String idPenyewa) {
        this.idPenyewa = idPenyewa;
    }

    public void setIdKios(String idKios) {
        this.idKios = idKios;
    }

    public void setTglMulai(LocalDate tglMulai) {
        this.tglMulai = tglMulai;
    }

    public void setTglSelesai(LocalDate tglSelesai) {
        this.tglSelesai = tglSelesai;
    }

    public void setTglPenyewaan(LocalDate tglPenyewaan) {
        this.tglPenyewaan = tglPenyewaan;
    }

    public void setStsPenyewaan(String stsPenyewaan) {
        this.stsPenyewaan = stsPenyewaan;
    }

    @Override
    public String toString() {
        return "Penyewaan{" +
                "id=" + idPenyewaan +
                ", idKios=" + idKios +
                ", idPenyewa=" + idPenyewa +
                ", sts=" + stsPenyewaan +
                "}";
    }
}