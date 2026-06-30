package com.sigap.ADT;

public class Denda {

    private String idDenda;
    private String jenisPelanggaran;
    private double nominalDenda;
    private String keterangan;
    private String stsDenda;

    public Denda() {
    }

    public Denda(String idDenda, String jenisPelanggaran, double nominalDenda, String keterangan) {
        this.idDenda = idDenda;
        this.jenisPelanggaran = jenisPelanggaran;
        this.nominalDenda = nominalDenda;
        this.keterangan = keterangan;
    }

    public Denda(String idDenda, String jenisPelanggaran, double nominalDenda, String keterangan, String stsDenda) {
        this.idDenda = idDenda;
        this.jenisPelanggaran = jenisPelanggaran;
        this.nominalDenda = nominalDenda;
        this.keterangan = keterangan;
        this.stsDenda = stsDenda;
    }

    public String getIdDenda() {
        return idDenda;
    }

    public void setIdDenda(String idDenda) {
        this.idDenda = idDenda;
    }

    public String getJenisPelanggaran() {
        return jenisPelanggaran;
    }

    public void setJenisPelanggaran(String jenisPelanggaran) {
        this.jenisPelanggaran = jenisPelanggaran;
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
        return "Denda{" +
                "id=" + idDenda +
                ", jenis=" + jenisPelanggaran +
                ", nominal=" + nominalDenda +
                ", sts=" + stsDenda +
                "}";
    }
}