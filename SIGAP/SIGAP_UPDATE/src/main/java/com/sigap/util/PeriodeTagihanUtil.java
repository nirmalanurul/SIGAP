package com.sigap.util;

import com.sigap.ADT.TagihanPembayaranSewa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitas untuk menghitung slot tagihan bulanan sebuah Penyewaan
 * (dari Tgl_Mulai s/d Tgl_Selesai) dan mencocokkannya dengan tagihan
 * yang sudah pernah dibuat, berdasarkan bulan+tahun Tgl_Jatuh_Tempo.
 * <p>
 * Slot-slot ini bersifat VIRTUAL — tidak disimpan sebagai baris di database
 * sampai kasir benar-benar memilih satu slot lewat dialog Pilih Penyewaan →
 * Pilih Bulan Tagihan, lalu menekan SIMPAN pada form Tagihan Pembayaran Sewa.
 */
public final class PeriodeTagihanUtil {

    private PeriodeTagihanUtil() {
    }

    /**
     * Menghasilkan daftar tanggal jatuh tempo bulanan, satu per bulan,
     * dari bulan Tgl_Mulai sampai bulan Tgl_Selesai (inklusif).
     * Tanggal tiap slot mengikuti hari (tanggal) Tgl_Mulai, dan otomatis
     * disesuaikan (clamp) jika bulan tersebut tidak punya tanggal itu
     * (contoh: mulai tanggal 31 Januari -> slot Februari jadi tanggal 28/29).
     */
    public static List<LocalDate> generateJatuhTempoBulanan(LocalDate tglMulai, LocalDate tglSelesai) {
        List<LocalDate> hasil = new ArrayList<>();
        if (tglMulai == null || tglSelesai == null || tglSelesai.isBefore(tglMulai)) {
            return hasil;
        }

        int hariAcuan = tglMulai.getDayOfMonth();
        LocalDate bulanBerjalan = tglMulai.withDayOfMonth(1);
        LocalDate batasBulan = tglSelesai.withDayOfMonth(1);

        while (!bulanBerjalan.isAfter(batasBulan)) {
            int hariValid = Math.min(hariAcuan, bulanBerjalan.lengthOfMonth());
            hasil.add(bulanBerjalan.withDayOfMonth(hariValid));
            bulanBerjalan = bulanBerjalan.plusMonths(1);
        }
        return hasil;
    }

    /**
     * True jika sudah ada tagihan aktif (bukan 'Dibatalkan') pada daftar yang
     * diberikan, yang bulan+tahun Tgl_Jatuh_Tempo-nya sama dengan slot ini.
     * Daftar tagihanAktif yang dioper wajib sudah difilter di luar (hanya
     * tagihan milik penyewaan yang sama dan berstatus bukan 'Dibatalkan').
     */
    public static boolean sudahDitagih(LocalDate slot, List<TagihanPembayaranSewa> tagihanAktif) {
        if (slot == null || tagihanAktif == null) return false;
        return tagihanAktif.stream().anyMatch(t ->
                t.getTglJatuhTempo() != null
                        && t.getTglJatuhTempo().getMonthValue() == slot.getMonthValue()
                        && t.getTglJatuhTempo().getYear() == slot.getYear());
    }
}