package com.sigap.APP;

import com.sigap.ADT.Kios;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_Kios {

    public static void insert(Kios k) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertKios(?,?,?,?,?)}")) {
            cs.setString(1, k.getIdKios());
            cs.setDouble(2, k.getHargaKios());
            cs.setDouble(3, k.getPanjangKios());
            cs.setDouble(4, k.getLebarKios());
            cs.setString(5, k.getDeskripsi());
            cs.executeUpdate();
        }
    }

    public static List<Kios> getAll() throws SQLException {
        DBConnect db = new DBConnect();
        List<Kios> list = new ArrayList<>();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwKiosAll");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static Kios getById(String idKios) throws SQLException {
        DBConnect db = new DBConnect();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwKiosAll WHERE Id_Kios = ?");
        ps.setString(1, idKios);
        ResultSet rs = ps.executeQuery();
        Kios k = null;
        if (rs.next()) k = mapRow(rs);
        rs.close(); ps.close(); db.conn.close();
        return k;
    }

    public static void update(Kios k) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spUpdateKios(?,?,?,?,?)}");
        cs.setString(1, k.getIdKios());
        cs.setDouble(2, k.getHargaKios());
        cs.setDouble(3, k.getPanjangKios());
        cs.setDouble(4, k.getLebarKios());
        cs.setString(5, k.getDeskripsi());
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static void delete(String idKios) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spDeleteKios(?)}");
        cs.setString(1, idKios);
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static List<Kios> search(String keyword) throws SQLException {
        DBConnect db = new DBConnect();
        List<Kios> list = new ArrayList<>();
        String sql = """
        SELECT * FROM vwKiosAll
        WHERE Id_Kios         LIKE ?
           OR Deskripsi       LIKE ?
           OR Sts_Kios        LIKE ?
           OR CAST(Harga_Kios AS VARCHAR) LIKE ?
        """;
        PreparedStatement ps = db.conn.prepareStatement(sql);
        String kw = "%" + keyword.trim() + "%";
        for (int i = 1; i <= 4; i++)
            ps.setString(i, kw);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            list.add(mapRow(rs));
        rs.close();
        ps.close();
        db.conn.close();
        return list;
    }

    public static String generateNextId() throws SQLException {
        DBConnect db = new DBConnect();
        PreparedStatement ps = db.conn.prepareStatement(
                "SELECT dbo.fnGenerateIdKios() AS Id_Kios"
        );
        ResultSet rs = ps.executeQuery();
        String id = "KS001";
        if (rs.next()) id = rs.getString("Id_Kios");
        rs.close(); ps.close(); db.conn.close();
        return id;
    }

    private static Kios mapRow(ResultSet rs) throws SQLException {
        return new Kios(
                rs.getString("Id_Kios"),
                rs.getDouble("Harga_Kios"),
                rs.getDouble("Panjang_Kios"),
                rs.getDouble("Lebar_Kios"),
                rs.getDouble("Luas_Kios"),
                rs.getString("Deskripsi"),
                rs.getString("Sts_Kios")
        );
    }
}