package com.senz.sdk.utils;

import android.util.Log;

public class L {
    public static final String TAG = "Senz SDK";

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void i(String msg, Throwable tr) {
        Log.i(TAG, msg, tr);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void d(String msg, Throwable tr) {
        Log.d(TAG, msg, tr);
    }

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void v(String msg, Throwable tr) {
        Log.v(TAG, msg, tr);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void w(String msg, Throwable tr) {
        Log.w(TAG, msg, tr);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        Log.e(TAG, msg, tr);
    }

    public static void wtf(String msg) {
        Log.wtf(TAG, msg);
    }

    public static void wtf(String msg, Throwable tr) {
        Log.wtf(TAG, msg, tr);
    }
}
