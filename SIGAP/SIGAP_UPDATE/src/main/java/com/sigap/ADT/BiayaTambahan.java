package com.sigap.ADT;

public class BiayaTambahan {

    private String idBiayaTambahan;
    private String jenisBiayaTambahan;
    private double nominalDenda;
    private String keterangan;
    private String stsDenda;

    public BiayaTambahan() {
    }

    public BiayaTambahan(String idBiayaTambahan, String jenisBiayaTambahan, double nominalDenda, String keterangan) {
        this.idBiayaTambahan = idBiayaTambahan;
        this.jenisBiayaTambahan = jenisBiayaTambahan;
        this.nominalDenda = nominalDenda;
        this.keterangan = keterangan;
    }

    public BiayaTambahan(String idBiayaTambahan, String jenisBiayaTambahan, double nominalDenda, String keterangan, String stsDenda) {
        this.idBiayaTambahan = idBiayaTambahan;
        this.jenisBiayaTambahan = jenisBiayaTambahan;
        this.nominalDenda = nominalDenda;
        this.keterangan = keterangan;
        this.stsDenda = stsDenda;
    }

    public String getIdBiayaTambahan() {
        return idBiayaTambahan;
    }

    public void setIdBiayaTambahan(String idBiayaTambahan) {
        this.idBiayaTambahan = idBiayaTambahan;
    }

    public String getJenisBiayaTambahan() {
        return jenisBiayaTambahan;
    }

    public void setJenisBiayaTambahan(String jenisBiayaTambahan) {
        this.jenisBiayaTambahan = jenisBiayaTambahan;
    }

    public double getNominalDenda() {
        return nominalDenda;
    }

    public void setNominalDenda(double nominalDenda) {
        this.nominalDenda = nominalDenda;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getStsDenda() {
        return stsDenda;
    }

    public void setStsDenda(String stsDenda) {
        this.stsDenda = stsDenda;
    }

    @Override
    public String toString() {
        return "BiayaTambahan{" +
                "idBiayaTambahan='" + idBiayaTambahan + '\'' +
                ", jenisBiayaTambahan='" + jenisBiayaTambahan + '\'' +
                ", nominalDenda=" + nominalDenda +
                ", keterangan='" + keterangan + '\'' +
                ", stsDenda='" + stsDenda + '\'' +
                '}';
    }
}