package com.sigap.scheduler;

import com.sigap.APP.CRUD_Kios;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler background yang secara berkala mengecek kios berstatus
 * Maintenance dan mengembalikannya menjadi Aktif setelah 1 hari.
 * Panggil MaintenanceScheduler.start() sekali saat aplikasi startup.
 */
public class MaintenanceScheduler {

    // Jarak antar pengecekan. Kios yang masa Maintenance-nya sudah
    // lewat akan menunggu maksimal sekian lama sebelum otomatis
    // kembali Aktif. Ubah TimeUnit.HOURS -> TimeUnit.MINUTES kalau
    // butuh lebih presisi.
    private static final long INTERVAL = 1;
    private static final TimeUnit INTERVAL_UNIT = TimeUnit.HOURS;

    private static final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "maintenance-kios-scheduler");
                t.setDaemon(true);
                return t;
            });

    public static void start() {
        // langsung cek sekali saat start, lalu berulang sesuai INTERVAL
        executor.scheduleAtFixedRate(MaintenanceScheduler::checkAndRevert, 0, INTERVAL, INTERVAL_UNIT);
    }

    private static void checkAndRevert() {
        try {
            CRUD_Kios.selesaikanMaintenanceOverdue();
        } catch (SQLException e) {
            // TODO: ganti dengan logger project (mis. SLF4J) kalau sudah ada
            e.printStackTrace();
        }
    }

    public static void stop() {
        executor.shutdown();
    }
}