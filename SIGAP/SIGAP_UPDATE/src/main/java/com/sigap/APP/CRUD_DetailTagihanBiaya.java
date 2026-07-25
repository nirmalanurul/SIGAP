package com.sigap.APP;

import com.sigap.ADT.DetailTagihanBiaya;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_DetailTagihanBiaya {

    // Sama seperti Tagihan_Pembayaran_Sewa, baris Detail_Tagihan_Biaya TIDAK
    // punya update biasa: Jumlah_Hari & Sub_total final begitu diinsert.
    // Sub_total sengaja TIDAK dikirim dari klien -- dihitung server-side di
    // spInsertDetailTagihanBiaya (Biaya_Tambahan.Nominal x Jumlah_Hari), lalu
    // SP yang sama juga bertanggung jawab menambah Total_Biaya_Tambahan &
    // Total_Tagihan di baris induk Tagihan_Pembayaran_Sewa terkait (di luar
    // trg_PreventUpdateTagihan, karena perubahan itu bagian dari transaksi
    // insert biaya, bukan update bebas dari klien).
    //
    // CATATAN: Nama-nama stored procedure di bawah ini masih usulan --
    // PDM cuma mendefinisikan tabelnya, jadi SP-nya perlu dibuat kalau belum
    // ada. Sesuaikan nama & urutan parameter kalau SP kamu sudah pakai nama
    // lain.

    public static void insert(DetailTagihanBiaya d) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertDetailTagihanBiaya(?,?,?)}")) {
            cs.setString(1, d.getIdTagihanPembayaran());
            cs.setString(2, d.getIdBiayaTambahan());
            cs.setInt(3, d.getJumlahHari());
            cs.executeUpdate();
        }
    }

    /**
     * Hanya untuk skenario pembersihan/rollback manual (mis. sebagian baris
     * biaya gagal tersimpan saat proses simpan tagihan baru). Bukan bagian
     * dari alur "edit" biasa karena Detail_Tagihan_Biaya bersifat final.
     */
    public static void delete(String idTagihanPembayaran, String idBiayaTambahan) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spHapusDetailTagihanBiaya(?,?)}")) {
            cs.setString(1, idTagihanPembayaran);
            cs.setString(2, idBiayaTambahan);
            cs.executeUpdate();
        }
    }

    public static List<DetailTagihanBiaya> getAll() throws SQLException {
        List<DetailTagihanBiaya> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetAllDetailTagihanBiaya}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Semua baris biaya tambahan milik satu tagihan (dipakai TagihanController saat menampilkan detail). */
    public static List<DetailTagihanBiaya> getByIdTagihanPembayaran(String idTagihanPembayaran) throws SQLException {
        List<DetailTagihanBiaya> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetDetailTagihanBiayaByIdTagihan(?)}")) {
            cs.setString(1, idTagihanPembayaran);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Semua tagihan yang pernah memakai satu jenis Biaya_Tambahan tertentu (berguna untuk laporan). */
    public static List<DetailTagihanBiaya> getByIdBiayaTambahan(String idBiayaTambahan) throws SQLException {
        List<DetailTagihanBiaya> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetDetailTagihanBiayaByIdBiaya(?)}")) {
            cs.setString(1, idBiayaTambahan);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private static DetailTagihanBiaya mapRow(ResultSet rs) throws SQLException {
        return new DetailTagihanBiaya(
                rs.getString("Id_Tagihan_Pembayaran"),
                rs.getString("Id_Biaya_Tambahan"),
                rs.getInt("Jumlah_Hari"),
                rs.getInt("Sub_total")
        );
    }
}