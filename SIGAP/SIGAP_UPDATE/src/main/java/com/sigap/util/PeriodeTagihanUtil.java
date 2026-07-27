package com.sigap.util;

import com.sigap.ADT.TagihanPembayaranSewa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class PeriodeTagihanUtil {

    private PeriodeTagihanUtil() {
    }

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
            LocalDate slot = bulanBerjalan.withDayOfMonth(hariValid);

            if (slot.isAfter(tglSelesai)) {
                slot = tglSelesai;
            }
            hasil.add(slot);
            bulanBerjalan = bulanBerjalan.plusMonths(1);
        }
        return hasil;
    }

    public static boolean sudahDitagih(LocalDate slot, List<TagihanPembayaranSewa> tagihanAktif) {
        if (slot == null || tagihanAktif == null) return false;
        return tagihanAktif.stream().anyMatch(t ->
                t.getTglJatuhTempo() != null
                        && t.getTglJatuhTempo().getMonthValue() == slot.getMonthValue()
                        && t.getTglJatuhTempo().getYear() == slot.getYear());
    }
}