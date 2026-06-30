package com.sigap.APP;

import com.sigap.ADT.Denda;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_Denda {

    public static void insert(Denda d) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertDenda(?,?,?,?)}")) {
            cs.setString(1, d.getIdDenda());
            cs.setString(2, d.getJenisPelanggaran());
            cs.setDouble(3, d.getNominalDenda());
            cs.setString(4, d.getKeterangan());
            cs.executeUpdate();
        }
    }

    public static List<Denda> getAll() throws SQLException {
        DBConnect db = new DBConnect();
        List<Denda> list = new ArrayList<>();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwDendaAll");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static List<Denda> getActive() throws SQLException {
        DBConnect db = new DBConnect();
        List<Denda> list = new ArrayList<>();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwDenda");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        rs.close(); ps.close(); db.conn.close();
        return list;
    }

    public static Denda getById(String idDenda) throws SQLException {
        DBConnect db = new DBConnect();
        PreparedStatement ps = db.conn.prepareStatement("SELECT * FROM vwDendaAll WHERE Id_Denda = ?");
        ps.setString(1, idDenda);
        ResultSet rs = ps.executeQuery();
        Denda d = null;
        if (rs.next()) d = mapRow(rs);
        rs.close(); ps.close(); db.conn.close();
        return d;
    }

    public static void update(Denda d) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spUpdateDenda(?,?,?,?)}");
        cs.setString(1, d.getIdDenda());
        cs.setString(2, d.getJenisPelanggaran());
        cs.setDouble(3, d.getNominalDenda());
        cs.setString(4, d.getKeterangan());
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static void delete(String idDenda) throws SQLException {
        DBConnect db = new DBConnect();
        CallableStatement cs = db.conn.prepareCall("{CALL spDeleteDenda(?)}");
        cs.setString(1, idDenda);
        cs.executeUpdate();
        cs.close();
        db.conn.close();
    }

    public static List<Denda> search(String keyword) throws SQLException {
        DBConnect db = new DBConnect();
        List<Denda> list = new ArrayList<>();
        String sql = """
                SELECT * FROM vwDendaAll
                WHERE Id_Denda          LIKE ?
                   OR Jenis_Pelanggaran LIKE ?
                   OR Keterangan        LIKE ?
                   OR Sts_Denda         LIKE ?
                   OR CAST(Nominal_Denda AS VARCHAR) LIKE ?
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
                "SELECT dbo.fnGenerateIdDenda() AS Id_Denda"
        );
        ResultSet rs = ps.executeQuery();
        String id = "DN001";
        if (rs.next()) id = rs.getString("Id_Denda");
        rs.close(); ps.close(); db.conn.close();
        return id;
    }

    private static Denda mapRow(ResultSet rs) throws SQLException {
        return new Denda(
                rs.getString("Id_Denda"),
                rs.getString("Jenis_Pelanggaran"),
                rs.getDouble("Nominal_Denda"),
                rs.getString("Keterangan"),
                rs.getString("Sts_Denda")
        );
    }
}