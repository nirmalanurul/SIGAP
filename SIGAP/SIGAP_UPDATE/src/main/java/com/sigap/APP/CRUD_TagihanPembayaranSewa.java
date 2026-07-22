package com.sigap.APP;

import com.sigap.ADT.TagihanPembayaranSewa;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CRUD_TagihanPembayaranSewa {

    // Transaksi tagihan TIDAK memiliki method update() biasa.
    // Setelah insert, data inti (Id_Penyewaan, Id_Karyawan, Tgl_Bayar, Tgl_Jatuh_Tempo,
    // Total_Biaya_Sewa, Total_Biaya_Tambahan, Total_Tagihan) bersifat final
    // (lihat trg_PreventUpdateTagihan di database). Perubahan lanjutan yang sah
    // hanya lewat bayar(idTagihan, nominal, metode) atau batalkan(idTagihan).

    public static void insert(TagihanPembayaranSewa t) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertTagihanPembayaran(?,?,?,?,?,?)}")) {
            cs.setString(1, t.getIdTagihanPembayaran());
            cs.setString(2, t.getIdPenyewaan());
            cs.setString(3, t.getIdKaryawan());
            cs.setDate(4, Date.valueOf(t.getTglJatuhTempo()));
            cs.setString(5, t.getMetodeBayar());
            cs.setDouble(6, t.getTotalDibayar());
            cs.executeUpdate();
        }
    }

    public static void bayar(String idTagihanPembayaran, double nominalBayar, String metodeBayar) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spBayarTagihan(?,?,?)}")) {
            cs.setString(1, idTagihanPembayaran);
            cs.setDouble(2, nominalBayar);
            cs.setString(3, metodeBayar);
            cs.executeUpdate();
        }
    }

    public static void batalkan(String idTagihanPembayaran) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spBatalkanTagihan(?)}")) {
            cs.setString(1, idTagihanPembayaran);
            cs.executeUpdate();
        }
    }

    public static void refreshStatus() throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spRefreshStatusTagihan}")) {
            cs.executeUpdate();
        }
    }

    public static List<TagihanPembayaranSewa> getAll() throws SQLException {
        List<TagihanPembayaranSewa> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetAllTagihan}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public static TagihanPembayaranSewa getById(String idTagihanPembayaran) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetTagihanById(?)}")) {
            cs.setString(1, idTagihanPembayaran);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public static List<TagihanPembayaranSewa> getByIdPenyewaan(String idPenyewaan) throws SQLException {
        List<TagihanPembayaranSewa> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetTagihanByIdPenyewaan(?)}")) {
            cs.setString(1, idPenyewaan);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public static List<TagihanPembayaranSewa> search(String keyword) throws SQLException {
        List<TagihanPembayaranSewa> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spSearchTagihan(?)}")) {
            cs.setString(1, keyword);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public static String generateNextId() throws SQLException {
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT dbo.fnGenerateIdTagihanPembayaran() AS Id_Tagihan_Pembayaran")) {
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Id_Tagihan_Pembayaran") : "TG001";
            }
        }
    }

    private static TagihanPembayaranSewa mapRow(ResultSet rs) throws SQLException {
        LocalDate tglBayar = toLocalDate(rs.getDate("Tgl_Bayar"));
        LocalDate tglJatuhTempo = toLocalDate(rs.getDate("Tgl_Jatuh_Tempo"));

        return new TagihanPembayaranSewa(
                rs.getString("Id_Tagihan_Pembayaran"),
                rs.getString("Id_Penyewaan"),
                rs.getString("Id_Karyawan"),
                tglBayar,
                tglJatuhTempo,
                rs.getDouble("Total_Biaya_Sewa"),
                rs.getDouble("Total_Biaya_Tambahan"),
                rs.getDouble("Total_Tagihan"),
                rs.getDouble("Total_Dibayar"),
                rs.getString("Metode_Bayar"),
                rs.getString("Sts_Tagihan_Pembayaran")
        );
    }

    private static LocalDate toLocalDate(Date sqlDate) {
        return sqlDate == null ? null : sqlDate.toLocalDate();
    }
}