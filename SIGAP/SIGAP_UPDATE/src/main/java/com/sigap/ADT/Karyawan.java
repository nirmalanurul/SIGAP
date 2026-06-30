package com.sigap.ADT;

public class Karyawan {

    private String idKaryawan;
    private String namaKaryawan;
    private String jabatanKaryawan;
    private String noTelp;
    private String email;
    private String username;
    private String password;
    private String stsKaryawan;

    public Karyawan() {
    }

    public Karyawan(String idKaryawan, String namaKaryawan, String jabatanKaryawan, String noTelp, String email, String username, String password) {
        this.idKaryawan = idKaryawan;
        this.namaKaryawan = namaKaryawan;
        this.jabatanKaryawan = jabatanKaryawan;
        this.noTelp = noTelp;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public Karyawan(String idKaryawan, String namaKaryawan, String jabatanKaryawan, String noTelp, String email, String username, String password, String stsKaryawan) {
        this.idKaryawan = idKaryawan;
        this.namaKaryawan = namaKaryawan;
        this.jabatanKaryawan = jabatanKaryawan;
        this.noTelp = noTelp;
        this.email = email;
        this.username = username;
        this.password = password;
        this.stsKaryawan = stsKaryawan;
    }

    public String getIdKaryawan() {
        return idKaryawan;
    }

    public void setIdKaryawan(String idKaryawan) {
        this.idKaryawan = idKaryawan;
    }

    public String getNamaKaryawan() {
        return namaKaryawan;
    }

    public void setNamaKaryawan(String namaKaryawan) {
        this.namaKaryawan = namaKaryawan;
    }

    public String getJabatanKaryawan() {
        return jabatanKaryawan;
    }

    public void setJabatanKaryawan(String jabatanKaryawan) {
        this.jabatanKaryawan = jabatanKaryawan;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStsKaryawan() {
        return stsKaryawan;
    }

    public void setStsKaryawan(String stsKaryawan) {
        this.stsKaryawan = stsKaryawan;
    }

    @Override
    public String toString() {
        return "Karyawan{" +
                "id=" + idKaryawan +
                ", nama=" + namaKaryawan +
                ", jabatan=" + jabatanKaryawan +
                "}";
    }
}