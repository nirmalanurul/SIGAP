package com.sigap.util;

import com.sigap.ADT.Karyawan;

public class Session {

    private static Karyawan loggedInUser;

    private Session() {
    }

    public static void setLoggedInUser(Karyawan k) {
        loggedInUser = k;
    }

    public static Karyawan getLoggedInUser() {
        return loggedInUser;
    }

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static void clear() {
        loggedInUser = null;
    }
}