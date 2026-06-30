package com.sigap.database;

import java.sql.*;

public class DBConnect {

    public Connection conn;
    public Statement stat;
    public ResultSet result;
    public PreparedStatement pstat;

    public DBConnect() {
        try {
            String url = ("jdbc:sqlserver://LAPTOP-4F1A3VGF\\SQLWAHYU:1433;"+
                    "databaseName=db_sigap;"+
                    "user=sa;"+
                    "password=PoltekAstra;"+
                    "trustServerCertificate=true");

            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
            System.out.println("Koneksi berhasil!");
        } catch (Exception e) {
            System.out.println("Error saat connect database " + e);
        }
    }

    public static void main(String[] args) {
        DBConnect connection = new DBConnect();
    }
}