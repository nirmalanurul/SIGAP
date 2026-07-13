package com.sigap.APP;

import com.sigap.ADT.Penyewaan;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CRUD_Penyewaan {

    // Transaksi penyewaan TIDAK memiliki method update().
    // Setelah insert, data inti transaksi bersifat final (lihat trg_PreventUpdatePenyewaan
    // di database). Satu-satunya perubahan lanjutan yang sah adalah batalkan(idPenyewaan),
    // yang hanya berhasil jika status saat ini masih 'Menunggu'.

    public static void insert(Penyewaan p) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertPenyewaan(?,?,?,?,?,?)}")) {
            cs.setString(1, p.getIdPenyewaan());
            cs.setString(2, p.getIdKaryawan());
            cs.setString(3, p.getIdPenyewa());
            cs.setString(4, p.getIdKios());
            cs.setDate(5, Date.valueOf(p.getTglMulai()));
            cs.setDate(6, Date.valueOf(p.getTglSelesai()));
            cs.executeUpdate();
        }
    }

    public static void batalkan(String idPenyewaan) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spBatalkanPenyewaan(?)}")) {
            cs.setString(1, idPenyewaan);
            cs.executeUpdate();
        }
    }

    public static void refreshStatus() throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spRefreshStatusPenyewaan}")) {
            cs.executeUpdate();
        }
    }

    public static List<Penyewaan> getAll() throws SQLException {
        List<Penyewaan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetAllPenyewaan}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public static Penyewaan getById(String idPenyewaan) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetPenyewaanById(?)}")) {
            cs.setString(1, idPenyewaan);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public static List<Penyewaan> getByIdKios(String idKios) throws SQLException {
        List<Penyewaan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetPenyewaanByIdKios(?)}")) {
            cs.setString(1, idKios);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public static List<Penyewaan> getByIdPenyewa(String idPenyewa) throws SQLException {
        List<Penyewaan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetPenyewaanByIdPenyewa(?)}")) {
            cs.setString(1, idPenyewa);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public static List<Penyewaan> search(String keyword) throws SQLException {
        List<Penyewaan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spSearchPenyewaan(?)}")) {
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
                     "SELECT dbo.fnGenerateIdPenyewaan() AS Id_Penyewaan")) {
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Id_Penyewaan") : "PY001";
            }
        }
    }

    private static Penyewaan mapRow(ResultSet rs) throws SQLException {
        LocalDate tglMulai     = toLocalDate(rs.getDate("Tgl_Mulai"));
        LocalDate tglSelesai   = toLocalDate(rs.getDate("Tgl_Selesai"));
        LocalDate tglPenyewaan = toLocalDate(rs.getDate("Tgl_Penyewaan"));

        return new Penyewaan(
                rs.getString("Id_Penyewaan"),
                rs.getString("Id_Karyawan"),
                rs.getString("Id_Penyewa"),
                rs.getString("Id_Kios"),
                tglMulai,
                tglSelesai,
                tglPenyewaan,
                rs.getString("Sts_Penyewaan")
        );
    }

    private static LocalDate toLocalDate(Date sqlDate) {
        return sqlDate == null ? null : sqlDate.toLocalDate();
    }
}