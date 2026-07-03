package com.sigap.Util;

import com.sigap.ADT.Karyawan;

public class Session {

    private static Karyawan loggedInUser;

    private Session() {
        //  instansiasi, class ini hanya dipakai secara statis//
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