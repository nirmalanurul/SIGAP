package com.sigap.APP;

import com.sigap.ADT.DetailTagihanBiaya;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_DetailTagihanBiaya {


    public static void insert(DetailTagihanBiaya d) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertDetailTagihanBiaya(?,?,?)}")) {
            cs.setString(1, d.getIdTagihanPembayaran());
            cs.setString(2, d.getIdBiayaTambahan());
            cs.setInt(3, d.getJumlahHari());
            cs.executeUpdate();
        }
    }

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