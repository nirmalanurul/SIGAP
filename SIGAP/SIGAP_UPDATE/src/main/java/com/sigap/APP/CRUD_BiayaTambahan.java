package com.sigap.APP;

import com.sigap.ADT.BiayaTambahan;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_BiayaTambahan {

    public static void insert(BiayaTambahan d) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertBiayaTambahan(?,?,?,?)}")) {
            cs.setString(1, d.getIdBiayaTambahan());
            cs.setString(2, d.getJenisBiayaTambahan());
            cs.setDouble(3, d.getNominalDenda());
            cs.setString(4, d.getKeterangan());
            cs.executeUpdate();
        }
    }

    public static List<BiayaTambahan> getAll() throws SQLException {
        List<BiayaTambahan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM vwBiayaTambahanAll");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public static List<BiayaTambahan> getActive() throws SQLException {
        List<BiayaTambahan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM vwBiayaTambahan");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public static BiayaTambahan getById(String idBiayaTambahan) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM vwBiayaTambahanAll WHERE Id_Biaya_Tambahan = ?")) {
            ps.setString(1, idBiayaTambahan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public static void update(BiayaTambahan d) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spUpdateBiayaTambahan(?,?,?,?)}")) {
            cs.setString(1, d.getIdBiayaTambahan());
            cs.setString(2, d.getJenisBiayaTambahan());
            cs.setDouble(3, d.getNominalDenda());
            cs.setString(4, d.getKeterangan());
            cs.executeUpdate();
        }
    }

    public static void delete(String idBiayaTambahan) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spDeleteBiayaTambahan(?)}")) {
            cs.setString(1, idBiayaTambahan);
            cs.executeUpdate();
        }
    }

    public static List<BiayaTambahan> search(String keyword) throws SQLException {
        List<BiayaTambahan> list = new ArrayList<>();
        String sql = """
                SELECT * FROM vwBiayaTambahanAll
                WHERE Id_Biaya_Tambahan    LIKE ?
                   OR Jenis_Biaya_Tambahan LIKE ?
                   OR Keterangan           LIKE ?
                   OR Sts_Biaya_Tambahan   LIKE ?
                   OR CAST(Nominal AS VARCHAR) LIKE ?
                """;
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword.trim() + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public static String generateNextId() throws SQLException {
        String id = "BY001";
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT dbo.fnGenerateIdBiayaTambahan() AS Id_Biaya_Tambahan");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) id = rs.getString("Id_Biaya_Tambahan");
        }
        return id;
    }

    private static BiayaTambahan mapRow(ResultSet rs) throws SQLException {
        return new BiayaTambahan(
                rs.getString("Id_Biaya_Tambahan"),
                rs.getString("Jenis_Biaya_Tambahan"),
                rs.getDouble("Nominal"),
                rs.getString("Keterangan"),
                rs.getString("Sts_Biaya_Tambahan")
        );
    }
}