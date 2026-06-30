package com.sigap.APP;

import com.sigap.ADT.Penyewa;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_Penyewa {

    public static void insert(Penyewa p) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertPenyewa(?,?,?,?,?,?)}")) {
            cs.setString(1, p.getIdPenyewa());
            cs.setString(2, p.getNamaPenyewa());
            cs.setString(3, p.getNik());
            cs.setString(4, p.getNoTelp());
            cs.setString(5, p.getAlamat());
            cs.setDate(6, Date.valueOf(p.getTglDaftar()));
            cs.executeUpdate();
        }
    }

    public static List<Penyewa> getAll() throws SQLException {
        DBConnect db = new DBConnect();
        List<Penyewa> list = new ArrayList<>();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwPenyewaAll");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static List<Penyewa> getActive() throws SQLException {
        DBConnect db = new DBConnect();
        List<Penyewa> list = new ArrayList<>();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwPenyewa");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static Penyewa getById(String idPenyewa) throws SQLException {
        DBConnect db = new DBConnect();
        PreparedStatement ps = db.conn.prepareStatement(
                "SELECT * FROM vwPenyewaAll WHERE Id_Penyewa = ?");
        ps.setString(1, idPenyewa);
        ResultSet rs = ps.executeQuery();
        Penyewa p = null;
        if (rs.next()) p = mapRow(rs);
        rs.close(); ps.close(); db.conn.close();
        return p;
    }

    public static void update(Penyewa p) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spUpdatePenyewa(?,?,?,?,?,?)}");
        cs.setString(1, p.getIdPenyewa());
        cs.setString(2, p.getNamaPenyewa());
        cs.setString(3, p.getNik());
        cs.setString(4, p.getNoTelp());
        cs.setString(5, p.getAlamat());
        cs.setDate(6, Date.valueOf(p.getTglDaftar()));
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static void delete(String idPenyewa) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spDeletePenyewa(?)}");
        cs.setString(1, idPenyewa);
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static List<Penyewa> search(String keyword) throws SQLException {
        DBConnect db = new DBConnect();
        List<Penyewa> list = new ArrayList<>();
        String sql = """
                SELECT * FROM vwPenyewaAll
                WHERE Id_Penyewa   LIKE ?
                   OR Nama_Penyewa LIKE ?
                   OR NIK          LIKE ?
                   OR No_Telp      LIKE ?
                   OR Alamat       LIKE ?
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
                "SELECT dbo.fnGenerateIdPenyewa() AS Id_Penyewa"
        );
        ResultSet rs = ps.executeQuery();
        String id = "PW001";
        if (rs.next()) id = rs.getString("Id_Penyewa");
        rs.close(); ps.close(); db.conn.close();
        return id;
    }

    private static Penyewa mapRow(ResultSet rs) throws SQLException {
        return new Penyewa(
                rs.getString("Id_Penyewa"),
                rs.getString("Nama_Penyewa"),
                rs.getString("NIK"),
                rs.getString("No_Telp"),
                rs.getString("Alamat"),
                rs.getDate("Tgl_Daftar").toLocalDate(),
                rs.getString("Sts_Penyewa")
        );
    }
}