package com.sigap.ADT;

import java.util.Objects;

/**
 * Merepresentasikan satu baris tabel Detail_Tagihan_Biaya (PDM).
 * Composite PK: (Id_Tagihan_Pembayaran, Id_Biaya_Tambahan).
 *
 *   PK,FK1  Id_Tagihan_Pembayaran   VARCHAR(8)  NOT NULL
 *   PK,FK2  Id_Biaya_Tambahan       VARCHAR(8)  NOT NULL
 *           Jumlah_Hari             INT         NOT NULL
 *           Sub_total               INT         NOT NULL
 *
 * Tidak ada Id sendiri (surrogate key) karena baris ini murni tabel
 * junction/detail antara Tagihan_Pembayaran_Sewa dan Biaya_Tambahan --
 * identitasnya adalah gabungan kedua FK di atas.
 */
public class DetailTagihanBiaya {

    private String idTagihanPembayaran;
    private String idBiayaTambahan;
    private int jumlahHari;
    private int subTotal;

    public DetailTagihanBiaya() {
    }

    public DetailTagihanBiaya(String idTagihanPembayaran, String idBiayaTambahan, int jumlahHari, int subTotal) {
        this.idTagihanPembayaran = idTagihanPembayaran;
        this.idBiayaTambahan = idBiayaTambahan;
        this.jumlahHari = jumlahHari;
        this.subTotal = subTotal;
    }

    public String getIdTagihanPembayaran() {
        return idTagihanPembayaran;
    }

    public void setIdTagihanPembayaran(String idTagihanPembayaran) {
        this.idTagihanPembayaran = idTagihanPembayaran;
    }

    public String getIdBiayaTambahan() {
        return idBiayaTambahan;
    }

    public void setIdBiayaTambahan(String idBiayaTambahan) {
        this.idBiayaTambahan = idBiayaTambahan;
    }

    public int getJumlahHari() {
        return jumlahHari;
    }

    public void setJumlahHari(int jumlahHari) {
        this.jumlahHari = jumlahHari;
    }

    public int getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(int subTotal) {
        this.subTotal = subTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetailTagihanBiaya)) return false;
        DetailTagihanBiaya that = (DetailTagihanBiaya) o;
        return Objects.equals(idTagihanPembayaran, that.idTagihanPembayaran)
                && Objects.equals(idBiayaTambahan, that.idBiayaTambahan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTagihanPembayaran, idBiayaTambahan);
    }

    @Override
    public String toString() {
        return "DetailTagihanBiaya{" +
                "idTagihanPembayaran='" + idTagihanPembayaran + '\'' +
                ", idBiayaTambahan='" + idBiayaTambahan + '\'' +
                ", jumlahHari=" + jumlahHari +
                ", subTotal=" + subTotal +
                '}';
    }
}