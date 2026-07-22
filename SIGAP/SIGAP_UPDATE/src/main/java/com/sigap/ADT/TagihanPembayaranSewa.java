package com.sigap.ADT;

import java.time.LocalDate;

public class TagihanPembayaranSewa {

    private String idTagihanPembayaran;
    private String idPenyewaan;
    private String idKaryawan;
    private LocalDate tglBayar;
    private LocalDate tglJatuhTempo;
    private double totalBiayaSewa;
    private double totalBiayaTambahan;
    private double totalTagihan;
    private double totalDibayar;
    private String metodeBayar;
    private String stsTagihanPembayaran;

    public TagihanPembayaranSewa() {
    }

    public TagihanPembayaranSewa(String idTagihanPembayaran, String idPenyewaan, String idKaryawan,
                                 LocalDate tglBayar, LocalDate tglJatuhTempo,
                                 double totalBiayaSewa, double totalBiayaTambahan, double totalTagihan,
                                 double totalDibayar, String metodeBayar, String stsTagihanPembayaran) {
        this.idTagihanPembayaran = idTagihanPembayaran;
        this.idPenyewaan = idPenyewaan;
        this.idKaryawan = idKaryawan;
        this.tglBayar = tglBayar;
        this.tglJatuhTempo = tglJatuhTempo;
        this.totalBiayaSewa = totalBiayaSewa;
        this.totalBiayaTambahan = totalBiayaTambahan;
        this.totalTagihan = totalTagihan;
        this.totalDibayar = totalDibayar;
        this.metodeBayar = metodeBayar;
        this.stsTagihanPembayaran = stsTagihanPembayaran;
    }

    public String getIdTagihanPembayaran() {
        return idTagihanPembayaran;
    }

    public String getIdPenyewaan() {
        return idPenyewaan;
    }

    public String getIdKaryawan() {
        return idKaryawan;
    }

    public LocalDate getTglBayar() {
        return tglBayar;
    }

    public LocalDate getTglJatuhTempo() {
        return tglJatuhTempo;
    }

    public double getTotalBiayaSewa() {
        return totalBiayaSewa;
    }

    public double getTotalBiayaTambahan() {
        return totalBiayaTambahan;
    }

    public double getTotalTagihan() {
        return totalTagihan;
    }

    public double getTotalDibayar() {
        return totalDibayar;
    }

    public String getMetodeBayar() {
        return metodeBayar;
    }

    public String getStsTagihanPembayaran() {
        return stsTagihanPembayaran;
    }

    public void setIdTagihanPembayaran(String idTagihanPembayaran) {
        this.idTagihanPembayaran = idTagihanPembayaran;
    }

    public void setIdPenyewaan(String idPenyewaan) {
        this.idPenyewaan = idPenyewaan;
    }

    public void setIdKaryawan(String idKaryawan) {
        this.idKaryawan = idKaryawan;
    }

    public void setTglBayar(LocalDate tglBayar) {
        this.tglBayar = tglBayar;
    }

    public void setTglJatuhTempo(LocalDate tglJatuhTempo) {
        this.tglJatuhTempo = tglJatuhTempo;
    }

    public void setTotalBiayaSewa(double totalBiayaSewa) {
        this.totalBiayaSewa = totalBiayaSewa;
    }

    public void setTotalBiayaTambahan(double totalBiayaTambahan) {
        this.totalBiayaTambahan = totalBiayaTambahan;
    }

    public void setTotalTagihan(double totalTagihan) {
        this.totalTagihan = totalTagihan;
    }

    public void setTotalDibayar(double totalDibayar) {
        this.totalDibayar = totalDibayar;
    }

    public void setMetodeBayar(String metodeBayar) {
        this.metodeBayar = metodeBayar;
    }

    public void setStsTagihanPembayaran(String stsTagihanPembayaran) {
        this.stsTagihanPembayaran = stsTagihanPembayaran;
    }

    @Override
    public String toString() {
        return "TagihanPembayaranSewa{" +
                "id=" + idTagihanPembayaran +
                ", idPenyewaan=" + idPenyewaan +
                ", totalTagihan=" + totalTagihan +
                ", totalDibayar=" + totalDibayar +
                ", sts=" + stsTagihanPembayaran +
                "}";
    }
}