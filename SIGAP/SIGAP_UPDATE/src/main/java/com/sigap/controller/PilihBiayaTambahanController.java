package com.sigap.controller;

import com.sigap.ADT.BiayaTambahan;
import com.sigap.ADT.DetailTagihanBiaya;
import com.sigap.APP.CRUD_BiayaTambahan;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Dialog picker untuk menambahkan satu atau lebih Biaya_Tambahan ke sebuah
 * Tagihan_Pembayaran_Sewa. Mengikuti pola dialog PilihPenyewaan /
 * PilihBulanTagihan: dipanggil dari TagihanController lewat showAndWait(),
 * hasil akhirnya diambil lewat getDaftarBiayaTerpilih() setelah dialog ditutup.
 *
 * Alur di dalam dialog ini sendiri ada 2 tabel:
 *  - tabelTersedia : daftar Biaya_Tambahan aktif dari master data (kiri)
 *  - tabelDipilih  : daftar biaya yang sudah ditambahkan ke tagihan ini,
 *                    lengkap dengan Jumlah_Hari & Sub_total (kanan)
 *
 * Sub_total dihitung sementara di sisi klien (Nominal x Jumlah_Hari) hanya
 * untuk ditampilkan; nilai final tetap sumber kebenarannya dari server saat
 * proses insert (SESUAIKAN dengan stored procedure yang dipakai
 * CRUD_DetailTagihanBiaya.insert()).
 */
public class PilihBiayaTambahanController implements Initializable {

    @FXML private TableView<BiayaTambahan> tabelTersedia;
    @FXML private TableColumn<BiayaTambahan, String> colIdTersedia;
    @FXML private TableColumn<BiayaTambahan, String> colJenisTersedia;
    @FXML private TableColumn<BiayaTambahan, String> colNominalTersedia;
    @FXML private TableColumn<BiayaTambahan, String> colKeteranganTersedia;
    @FXML private TableColumn<BiayaTambahan, String> colStatusTersedia;

    @FXML private TextField txtJumlahHari;
    @FXML private Button btnTambahKeDaftar;

    @FXML private TableView<BarisBiayaTerpilih> tabelDipilih;
    @FXML private TableColumn<BarisBiayaTerpilih, String> colIdDipilih;
    @FXML private TableColumn<BarisBiayaTerpilih, String> colJenisDipilih;
    @FXML private TableColumn<BarisBiayaTerpilih, String> colJumlahHariDipilih;
    @FXML private TableColumn<BarisBiayaTerpilih, String> colSubtotalDipilih;
    @FXML private TableColumn<BarisBiayaTerpilih, Void> colHapus;

    @FXML private Label lblTotalBiayaTambahan;
    @FXML private Button btnSelesai;
    @FXML private Button btnBatal;

    private static final NumberFormat FMT_RUPIAH = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    private final ObservableList<BiayaTambahan> masterTersedia = FXCollections.observableArrayList();
    private final ObservableList<BarisBiayaTerpilih> daftarDipilih = FXCollections.observableArrayList();

    private BiayaTambahan biayaTerpilihSementara = null;
    private boolean selesaiDiklik = false;

    // Untuk hitung otomatis Jumlah_Hari khusus jenis "Keterlambatan Bayar Sewa".
    // Diisi lewat setInfoJatuhTempo() oleh TagihanController sebelum showAndWait().
    private LocalDate tglJatuhTempo;
    private LocalDate tglBayar;

    // Kata kunci penanda jenis biaya "denda keterlambatan" -- SATU-SATUNYA jenis
    // yang Jumlah_Hari-nya OTOMATIS terhitung dari (Tgl_Bayar - Tgl_Jatuh_Tempo).
    // Jenis biaya lain (mis. Kerusakan Fasilitas) itu FLAT/one-time: field
    // Jumlah_Hari dikunci total (disabled), tidak bisa diisi manual maupun
    // otomatis, dan Sub_total = Nominal saja tanpa dikali apa pun.
    private static final String KATA_KUNCI_KETERLAMBATAN = "keterlambatan";

    /** True kalau dialog ditutup lewat "Selesai" (bukan dibatalkan / ditutup paksa). */
    public boolean isSelesaiDiklik() {
        return selesaiDiklik;
    }

    /** Baris tampilan di tabelDipilih: bungkus BiayaTambahan + Jumlah_Hari + Sub_total. */
    public static class BarisBiayaTerpilih {
        final BiayaTambahan biaya;
        int jumlahHari;
        int subTotal;

        BarisBiayaTerpilih(BiayaTambahan biaya, int jumlahHari) {
            this.biaya = biaya;
            this.jumlahHari = jumlahHari;
            // Sub_total HANYA dikali Jumlah_Hari untuk jenis "Keterlambatan Bayar
            // Sewa" (biaya per-hari, tarifnya di kolom Nominal / Hari). Untuk
            // jenis lain (mis. Kerusakan Fasilitas) Nominal itu FLAT/one-time,
            // jadi Sub_total = Nominal saja, Jumlah_Hari diabaikan di kalkulasi
            // (tetap disimpan = 1 di DB, cuma buat konsistensi skema).
            boolean perHari = biaya.getJenisBiayaTambahan() != null
                    && biaya.getJenisBiayaTambahan().toLowerCase().contains(KATA_KUNCI_KETERLAMBATAN);
            this.subTotal = perHari
                    ? (int) Math.round(biaya.getNominalDenda() * jumlahHari)
                    : (int) Math.round(biaya.getNominalDenda());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTabelTersedia();
        setupTabelDipilih();
        // Field dikunci dari awal -- baru terisi/terbuka setelah kasir klik
        // salah satu baris di tabelTersedia (lihat onRowTersediaClicked).
        txtJumlahHari.setDisable(true);
        txtJumlahHari.setEditable(false);
        txtJumlahHari.setPromptText("Pilih jenis biaya dulu");
        txtJumlahHari.setStyle("-fx-background-color:#F0F0F0;-fx-text-fill:#AAA;");
        Platform.runLater(this::muatBiayaTersedia);
    }

    private void setupTabelTersedia() {
        colIdTersedia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdBiayaTambahan()));
        colJenisTersedia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJenisBiayaTambahan()));
        colNominalTersedia.setCellValueFactory(d -> new SimpleStringProperty(
                "Rp " + FMT_RUPIAH.format((long) d.getValue().getNominalDenda())));
        colKeteranganTersedia.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getKeterangan() == null ? "-" : d.getValue().getKeterangan()));
        colStatusTersedia.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStsDenda() == null ? "-" : d.getValue().getStsDenda()));
    }

    private void setupTabelDipilih() {
        colIdDipilih.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().biaya.getIdBiayaTambahan()));
        colJenisDipilih.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().biaya.getJenisBiayaTambahan()));
        colJumlahHariDipilih.setCellValueFactory(d -> {
            BarisBiayaTerpilih b = d.getValue();
            boolean perHari = b.biaya.getJenisBiayaTambahan() != null
                    && b.biaya.getJenisBiayaTambahan().toLowerCase().contains(KATA_KUNCI_KETERLAMBATAN);
            return new SimpleStringProperty(perHari ? String.valueOf(b.jumlahHari) : "-");
        });
        colSubtotalDipilih.setCellValueFactory(d -> new SimpleStringProperty(
                "Rp " + FMT_RUPIAH.format((long) d.getValue().subTotal)));

        colHapus.setCellFactory(hapusCellFactory());
        tabelDipilih.setItems(daftarDipilih);
    }

    private Callback<TableColumn<BarisBiayaTerpilih, Void>, TableCell<BarisBiayaTerpilih, Void>> hapusCellFactory() {
        return col -> new TableCell<>() {
            private final Button btn = new Button("Hapus");
            {
                btn.setStyle("-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:6;");
                btn.setOnAction(e -> {
                    BarisBiayaTerpilih baris = getTableView().getItems().get(getIndex());
                    daftarDipilih.remove(baris);
                    hitungUlangTotal();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
    }

    private void muatBiayaTersedia() {
        try {
            // SESUAIKAN: CRUD_BiayaTambahan.getAll() -- ganti nama method kalau
            // filter "hanya yang Aktif" nya lewat method lain (mis. getAllAktif()).
            // Filter status pakai stsDenda (nama field di BiayaTambahan.java),
            // merepresentasikan kolom Sts_Biaya_Tambahan pada PDM.
            List<BiayaTambahan> semua = CRUD_BiayaTambahan.getAll();
            masterTersedia.setAll(semua.stream()
                    .filter(b -> "Aktif".equalsIgnoreCase(b.getStsDenda()))
                    .toList());
            tabelTersedia.setItems(masterTersedia);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Memuat",
                    "Gagal memuat daftar biaya tambahan. Periksa koneksi ke database.");
        }
    }

    @FXML
    void onRowTersediaClicked(MouseEvent event) {
        biayaTerpilihSementara = tabelTersedia.getSelectionModel().getSelectedItem();
        terapkanModeJumlahHari();
    }

    /**
     * Jenis biaya "Keterlambatan Bayar Sewa": Jumlah_Hari BUKAN input manual --
     * otomatis dihitung dari selisih Tgl_Jatuh_Tempo ke tanggal bayar saat ini
     * (hari ini kalau belum ada tanggal bayar final), field dikunci supaya
     * kasir tidak bisa mengubahnya.
     * Jenis biaya lain (mis. Kerusakan Fasilitas): Jumlah_Hari TETAP input
     * manual seperti biasa -- field ini bukan representasi hari terlambat,
     * jadi tidak disentuh sama sekali di sini.
     */
    private void terapkanModeJumlahHari() {
        if (biayaTerpilihSementara == null) return;

        if (isJenisKeterlambatan(biayaTerpilihSementara)) {
            // Biaya per-hari: field diaktifkan hanya untuk MENAMPILKAN angka
            // otomatis (Tgl_Bayar - Tgl_Jatuh_Tempo), tetap tidak bisa diketik.
            long hariTerlambat = hitungHariTerlambat();
            txtJumlahHari.setDisable(false);
            txtJumlahHari.setEditable(false);
            txtJumlahHari.setText(String.valueOf(hariTerlambat));
            txtJumlahHari.setPromptText("");
            txtJumlahHari.setStyle("-fx-background-color:#F0F0F0;-fx-text-fill:#888;");
        } else {
            // Biaya flat / one-time (mis. Kerusakan Fasilitas): field dikunci
            // TOTAL (disabled, bukan cuma non-editable) karena Jumlah_Hari
            // tidak relevan di sini -- Sub_total = Nominal saja.
            txtJumlahHari.clear();
            txtJumlahHari.setDisable(true);
            txtJumlahHari.setEditable(false);
            txtJumlahHari.setPromptText("Tidak berlaku (biaya flat)");
            txtJumlahHari.setStyle("-fx-background-color:#F0F0F0;-fx-text-fill:#AAA;");
        }
    }

    private boolean isJenisKeterlambatan(BiayaTambahan b) {
        return b.getJenisBiayaTambahan() != null
                && b.getJenisBiayaTambahan().toLowerCase().contains(KATA_KUNCI_KETERLAMBATAN);
    }

    /** Selisih hari dari Tgl_Jatuh_Tempo ke tanggal bayar (hari ini / tglBayar), minimal 0. */
    private long hitungHariTerlambat() {
        if (tglJatuhTempo == null) return 0;
        LocalDate acuanBayar = (tglBayar != null) ? tglBayar : LocalDate.now();
        return Math.max(0, ChronoUnit.DAYS.between(tglJatuhTempo, acuanBayar));
    }

    /**
     * Dipanggil dari TagihanController sebelum showAndWait(), supaya dialog
     * ini tahu Tgl_Jatuh_Tempo tagihan yang sedang diproses (dari dpTglJatuhTempo)
     * untuk menghitung otomatis Jumlah_Hari khusus jenis "Keterlambatan Bayar Sewa".
     * @param tglBayarSaatIni boleh null -> dianggap hari ini (LocalDate.now()).
     */
    public void setInfoJatuhTempo(LocalDate tglJatuhTempo, LocalDate tglBayarSaatIni) {
        this.tglJatuhTempo = tglJatuhTempo;
        this.tglBayar = tglBayarSaatIni;
        terapkanModeJumlahHari();
    }

    @FXML
    void onTambahKeDaftar(ActionEvent event) {
        if (biayaTerpilihSementara == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih dulu jenis biaya tambahan dari daftar di atas.");
            return;
        }
        boolean keterlambatan = isJenisKeterlambatan(biayaTerpilihSementara);
        int jumlahHari;

        if (keterlambatan) {
            // Jenis per-hari: Jumlah_Hari WAJIB dari angka otomatis di field
            // (hasil hitungHariTerlambat()), bukan input manual.
            String teksHari = txtJumlahHari.getText() == null ? "" : txtJumlahHari.getText().trim();
            try {
                jumlahHari = Integer.parseInt(teksHari);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validasi Input", "Jumlah hari harus berupa angka bulat.");
                return;
            }
            if (jumlahHari <= 0) {
                showAlert(Alert.AlertType.WARNING, "Belum Terlambat",
                        "Tanggal bayar belum melewati jatuh tempo, denda keterlambatan tidak perlu ditambahkan.");
                return;
            }
        } else {
            // Jenis flat / one-time (mis. Kerusakan Fasilitas): field Jumlah_Hari
            // dikunci & tidak dipakai untuk kalkulasi -- selalu diisi 1 di sini
            // supaya konsisten dengan skema Detail_Tagihan_Biaya, Sub_total tetap
            // = Nominal (lihat BarisBiayaTerpilih constructor).
            jumlahHari = 1;
        }

        boolean sudahAda = daftarDipilih.stream()
                .anyMatch(b -> b.biaya.getIdBiayaTambahan().equals(biayaTerpilihSementara.getIdBiayaTambahan()));
        if (sudahAda) {
            showAlert(Alert.AlertType.WARNING, "Sudah Ditambahkan",
                    "Jenis biaya ini sudah ada di daftar. Hapus dulu barisnya kalau mau ubah jumlah hari.");
            return;
        }

        daftarDipilih.add(new BarisBiayaTerpilih(biayaTerpilihSementara, jumlahHari));
        biayaTerpilihSementara = null;
        // Field Jumlah_Hari SELALU dikunci sampai baris tersedia diklik lagi --
        // tidak pernah dibuka jadi editable manual (lihat terapkanModeJumlahHari()).
        txtJumlahHari.clear();
        txtJumlahHari.setDisable(true);
        txtJumlahHari.setEditable(false);
        txtJumlahHari.setPromptText("Pilih jenis biaya dulu");
        txtJumlahHari.setStyle("-fx-background-color:#F0F0F0;-fx-text-fill:#AAA;");
        hitungUlangTotal();
    }

    private void hitungUlangTotal() {
        double total = daftarDipilih.stream().mapToDouble(b -> b.subTotal).sum();
        lblTotalBiayaTambahan.setText("Rp " + FMT_RUPIAH.format((long) total));
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        if (tabelTersedia.getScene() != null) alert.initOwner(tabelTersedia.getScene().getWindow());
        alert.showAndWait();
    }

    @FXML
    void onSelesai(ActionEvent event) {
        selesaiDiklik = true;
        tutupDialog();
    }

    @FXML
    void onBatal(ActionEvent event) {
        selesaiDiklik = false;
        daftarDipilih.clear();
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) tabelTersedia.getScene().getWindow();
        stage.close();
    }

    /** Dipanggil dari luar sebelum showAndWait(), untuk edit ulang daftar yang sudah ada di form. */
    public void setDaftarAwal(List<DetailTagihanBiaya> daftarAwal, List<BiayaTambahan> semuaBiaya) {
        if (daftarAwal == null) return;
        for (DetailTagihanBiaya d : daftarAwal) {
            semuaBiaya.stream()
                    .filter(b -> b.getIdBiayaTambahan().equals(d.getIdBiayaTambahan()))
                    .findFirst()
                    .ifPresent(b -> daftarDipilih.add(new BarisBiayaTerpilih(b, d.getJumlahHari())));
        }
        hitungUlangTotal();
    }

    /**
     * Null / kosong jika dialog dibatalkan. Kalau "Selesai" diklik, kembalikan
     * daftar DetailTagihanBiaya (Id_Tagihan_Pembayaran belum diisi di sini --
     * itu diisi oleh TagihanController setelah Id_Tagihan_Pembayaran final).
     *
     * SESUAIKAN: constructor DetailTagihanBiaya di bawah ini pakai urutan
     * (idTagihanPembayaran, idBiayaTambahan, jumlahHari, subTotal) sesuai pola
     * constructor TagihanPembayaranSewa yang sudah ada -- ganti kalau beda.
     */
    public List<DetailTagihanBiaya> getDaftarBiayaTerpilih() {
        if (!selesaiDiklik) return List.of();
        return daftarDipilih.stream()
                .map(b -> new DetailTagihanBiaya(
                        null, // Id_Tagihan_Pembayaran diisi belakangan
                        b.biaya.getIdBiayaTambahan(),
                        b.jumlahHari,
                        b.subTotal))
                .toList();
    }

    /** Total biaya tambahan versi klien, untuk ditampilkan sementara di form utama. */
    public double getTotalBiayaTambahanSementara() {
        return daftarDipilih.stream().mapToDouble(b -> b.subTotal).sum();
    }
}