package com.sigap.APP;

import com.sigap.ADT.Karyawan;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_Karyawan {

    public static void insert(Karyawan k) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertKaryawan(?,?,?,?,?,?,?)}")) {
            cs.setString(1, k.getIdKaryawan());
            cs.setString(2, k.getNamaKaryawan());
            cs.setString(3, k.getJabatanKaryawan());
            cs.setString(4, k.getNoTelp());
            cs.setString(5, k.getEmail());
            cs.setString(6, k.getUsername());
            cs.setString(7, k.getPassword());
            cs.executeUpdate();
        }
    }

    public static List<Karyawan> getAll() throws SQLException {
        DBConnect db = new DBConnect();
        List<Karyawan> list = new ArrayList<>();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwKaryawanAll");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static List<Karyawan> getActive() throws SQLException {
        DBConnect db = new DBConnect();
        List<Karyawan> list = new ArrayList<>();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwKaryawan");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static Karyawan getById(String idKaryawan) throws SQLException {
        DBConnect db = new DBConnect();
        PreparedStatement ps = db.conn.prepareStatement(
                "SELECT * FROM vwKaryawanAll WHERE Id_Karyawan = ?");
        ps.setString(1, idKaryawan);
        ResultSet rs = ps.executeQuery();
        Karyawan k = null;
        if (rs.next()) k = mapRow(rs);
        rs.close(); ps.close(); db.conn.close();
        return k;
    }

    public static void updateData(Karyawan k) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spUpdateKaryawan(?,?,?,?,?,?)}");
        cs.setString(1, k.getIdKaryawan());
        cs.setString(2, k.getNamaKaryawan());
        cs.setString(3, k.getJabatanKaryawan());
        cs.setString(4, k.getNoTelp());
        cs.setString(5, k.getEmail());
        cs.setString(6, k.getUsername());
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static void updatePassword(String idKaryawan, String newPassword) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spUpdatePasswordKaryawan(?,?)}");
        cs.setString(1, idKaryawan);
        cs.setString(2, newPassword);
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static void delete(String idKaryawan) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spDeleteKaryawan(?)}");
        cs.setString(1, idKaryawan);
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static List<Karyawan> search(String keyword) throws SQLException {
        DBConnect db = new DBConnect();
        List<Karyawan> list = new ArrayList<>();
        String sql = """
                SELECT * FROM vwKaryawanAll
                WHERE Id_Karyawan      LIKE ?
                   OR Nama_Karyawan    LIKE ?
                   OR Jabatan_Karyawan LIKE ?
                   OR Username         LIKE ?
                   OR Email            LIKE ?
                """;
        PreparedStatement ps = db.conn.prepareStatement(sql);
        String kw = "%" + keyword.trim() + "%";
        for (int i = 1; i <= 5; i++) ps.setString(i, kw);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static String generateNextId() throws SQLException {
        DBConnect db = new DBConnect();
        PreparedStatement ps = db.conn.prepareStatement(
                "SELECT dbo.fnGenerateIdKaryawan() AS Id_Karyawan"
        );
        ResultSet rs = ps.executeQuery();
        String id = "KR001";
        if (rs.next()) id = rs.getString("Id_Karyawan");
        rs.close(); ps.close(); db.conn.close();
        return id;
    }

    private static Karyawan mapRow(ResultSet rs) throws SQLException {
        return new Karyawan(
                rs.getString("Id_Karyawan"),
                rs.getString("Nama_Karyawan"),
                rs.getString("Jabatan_Karyawan"),
                rs.getString("No_Telp"),
                rs.getString("Email"),
                rs.getString("Username"),
                "",
                rs.getString("Sts_Karyawan")
        );
    }
}