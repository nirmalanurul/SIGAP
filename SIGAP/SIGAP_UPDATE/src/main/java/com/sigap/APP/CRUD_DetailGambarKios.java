package com.sigap.APP;

import com.sigap.ADT.DetailGambarKios;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_DetailGambarKios {

    public static void insert(Connection conn, DetailGambarKios g) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{CALL spInsertDetailGambarKios(?,?,?)}")) {
            cs.setString(1, g.getIdDetailGambar());
            cs.setString(2, g.getIdKios());
            cs.setString(3, g.getNamaFileGambar());
            cs.executeUpdate();
        }
    }

    public static List<DetailGambarKios> getByIdKios(String idKios) throws SQLException {
        List<DetailGambarKios> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetGambarByIdKios(?)}")) {
            cs.setString(1, idKios);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                list.add(new DetailGambarKios(
                        rs.getString("Id_Detail_Gambar"),
                        rs.getString("Id_Kios"),
                        rs.getString("Nama_File_Gambar")
                ));
            }
            rs.close();
        }
        return list;
    }

    public static void deleteAllByIdKios(Connection conn, String idKios) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{CALL spDeleteAllGambarByIdKios(?)}")) {
            cs.setString(1, idKios);
            cs.executeUpdate();
        }
    }

    public static String generateNextId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT dbo.fnGenerateIdDetailGambarKios() AS Id_Detail_Gambar")) {
            ResultSet rs = ps.executeQuery();
            String id = "GB001";
            if (rs.next()) id = rs.getString("Id_Detail_Gambar");
            rs.close();
            return id;
        }
    }
}