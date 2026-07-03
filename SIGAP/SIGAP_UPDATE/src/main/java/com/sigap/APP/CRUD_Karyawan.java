package com.sigap.APP;
/// //
import com.sigap.ADT.Karyawan;
import com.sigap.database.DBConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CRUD_Karyawan {

    public static void insert(Karyawan k) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spInsertKaryawan(?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, k.getIdKaryawan());
            cs.setString(2, k.getNamaKaryawan());
            cs.setString(3, k.getJabatanKaryawan());
            cs.setString(4, k.getNoTelp());
            cs.setString(5, k.getEmail());
            cs.setString(6, k.getUsername());
            cs.setString(7, k.getPassword());
            cs.setString(8, k.getFotoKtp());
            cs.executeUpdate();
        }
    }

    public static List<Karyawan> getAll() throws SQLException {
        List<Karyawan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM vwKaryawanAll");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public static List<Karyawan> getActive() throws SQLException {
        List<Karyawan> list = new ArrayList<>();
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM vwKaryawan");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public static Karyawan getById(String idKaryawan) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM vwKaryawanAll WHERE Id_Karyawan = ?")) {
            ps.setString(1, idKaryawan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Khusus untuk proses login. Mengambil data langsung dari tbKaryawan
     * (bukan view) karena butuh kolom Password yang sengaja dikecualikan
     * dari vwKaryawan / vwKaryawanAll untuk keamanan tampilan data.
     */
    public static Karyawan findByUsername(String username) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spGetKaryawanByUsername(?)}")) {
            cs.setString(1, username);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Karyawan k = new Karyawan(
                            rs.getString("Id_Karyawan"),
                            rs.getString("Nama_Karyawan"),
                            rs.getString("Jabatan_Karyawan"),
                            rs.getString("No_Telp"),
                            rs.getString("Email"),
                            rs.getString("Username"),
                            rs.getString("Password"),
                            rs.getString("Sts_Karyawan")
                    );
                    k.setFotoKtp(rs.getString("Foto_KTP"));
                    return k;
                }
                return null;
            }
        }
    }

    public static void updateData(Karyawan k) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spUpdateKaryawan(?,?,?,?,?,?,?)}")) {
            cs.setString(1, k.getIdKaryawan());
            cs.setString(2, k.getNamaKaryawan());
            cs.setString(3, k.getJabatanKaryawan());
            cs.setString(4, k.getNoTelp());
            cs.setString(5, k.getEmail());
            cs.setString(6, k.getUsername());
            cs.setString(7, k.getFotoKtp());
            cs.executeUpdate();
        }
    }

    public static void updatePassword(String idKaryawan, String newPassword) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spUpdatePasswordKaryawan(?,?)}")) {
            cs.setString(1, idKaryawan);
            cs.setString(2, newPassword);
            cs.executeUpdate();
        }
    }

    public static void delete(String idKaryawan) throws SQLException {
        try (Connection conn = new DBConnect().conn;
             CallableStatement cs = conn.prepareCall("{CALL spDeleteKaryawan(?)}")) {
            cs.setString(1, idKaryawan);
            cs.executeUpdate();
        }
    }

    public static List<Karyawan> search(String keyword) throws SQLException {
        List<Karyawan> list = new ArrayList<>();
        String sql = """
                SELECT * FROM vwKaryawanAll
                WHERE Id_Karyawan      LIKE ?
                   OR Nama_Karyawan    LIKE ?
                   OR Jabatan_Karyawan LIKE ?
                   OR Username         LIKE ?
                   OR Email            LIKE ?
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
        try (Connection conn = new DBConnect().conn;
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT dbo.fnGenerateIdKaryawan() AS Id_Karyawan");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("Id_Karyawan");
            return "KR001";
        }
    }

    private static Karyawan mapRow(ResultSet rs) throws SQLException {
        Karyawan k = new Karyawan(
                rs.getString("Id_Karyawan"),
                rs.getString("Nama_Karyawan"),
                rs.getString("Jabatan_Karyawan"),
                rs.getString("No_Telp"),
                rs.getString("Email"),
                rs.getString("Username"),
                "",   // memang sngaja kosong: view tidak menyertakan Password
                rs.getString("Sts_Karyawan")
        );
        k.setFotoKtp(rs.getString("Foto_KTP"));
        return k;
    }
}