package com.sigap.APP;

import com.sigap.ADT.DetailGambarKios;
import com.sigap.ADT.Kios;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_Kios {

    public static void insert(Kios k, List<String> daftarPathFoto) throws SQLException {
        try (Connection conn = new DBConnect().conn) {
            conn.setAutoCommit(false);
            try {
                try (CallableStatement cs = conn.prepareCall("{CALL spInsertKios(?,?,?,?,?)}")) {
                    cs.setString(1, k.getIdKios());
                    cs.setDouble(2, k.getHargaKios());
                    cs.setDouble(3, k.getPanjangKios());
                    cs.setDouble(4, k.getLebarKios());
                    cs.setString(5, k.getDeskripsi());
                    cs.executeUpdate();
                }

                for (String path : daftarPathFoto) {
                    String idGambar = CRUD_DetailGambarKios.generateNextId(conn);
                    DetailGambarKios g = new DetailGambarKios(idGambar, k.getIdKios(), path);
                    CRUD_DetailGambarKios.insert(conn, g);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void update(Kios k, List<String> daftarPathFotoBaru) throws SQLException {
        try (Connection conn = new DBConnect().conn) {
            conn.setAutoCommit(false);
            try {
                try (CallableStatement cs = conn.prepareCall("{CALL spUpdateKios(?,?,?,?,?)}")) {
                    cs.setString(1, k.getIdKios());
                    cs.setDouble(2, k.getHargaKios());
                    cs.setDouble(3, k.getPanjangKios());
                    cs.setDouble(4, k.getLebarKios());
                    cs.setString(5, k.getDeskripsi());
                    cs.executeUpdate();
                }

                if (daftarPathFotoBaru != null) {
                    CRUD_DetailGambarKios.deleteAllByIdKios(conn, k.getIdKios());
                    for (String path : daftarPathFotoBaru) {
                        String idGambar = CRUD_DetailGambarKios.generateNextId(conn);
                        DetailGambarKios g = new DetailGambarKios(idGambar, k.getIdKios(), path);
                        CRUD_DetailGambarKios.insert(conn, g);
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static void delete(String idKios) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spNonaktifkanKios(?)}")) {
            cs.setString(1, idKios);
            cs.executeUpdate();
        }
    }

    public static List<Kios> getAll() throws SQLException {
        List<Kios> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetAllKios}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public static Kios getById(String idKios) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetKiosById(?)}")) {
            cs.setString(1, idKios);
            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public static List<Kios> search(String keyword) throws SQLException {
        List<Kios> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spSearchKios(?)}")) {
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
                     "SELECT dbo.fnGenerateIdKios() AS Id_Kios")) {
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Id_Kios") : "KS001";
            }
        }
    }

    public static List<DetailGambarKios> getFoto(String idKios) throws SQLException {
        return CRUD_DetailGambarKios.getByIdKios(idKios);
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