package com.xd.adhocroute.log;


import com.xd.adhocroute.AdhocRouteApp;

import android.text.format.DateFormat;
import android.util.Log;

public class Lg {
    // private static final boolean DEBUG = true;
    private static String TAG = AdhocRouteApp.TAG;

    private static WriteLogger logger;
    private static boolean debug;

    public static final void setDebug(boolean debug) {
        Lg.debug = debug;
    }

    public static final void setLogger(WriteLogger writeLogger) {
        logger = writeLogger;
    }

    private static final void post(String msg) {
        String timestamp = DateFormat.format("yyyy-MM-dd kk-mm-ss", System.currentTimeMillis()).toString();
        post(timestamp + " " + msg, null);
    }

    private static final void post(String msg, Throwable e) {
        if (logger != null) {
            logger.post(msg, e);
        }
    }

    public static final boolean isDebug() {
        return debug || Log.isLoggable(TAG, Log.DEBUG);
    }

    public static void v(String msg) {
        if (isDebug()) {
            Log.v(TAG, msg);
            // post(TAG + "\t" + msg);
        }
    }

    public static void d(String msg) {
        if (isDebug()) {
            if (msg == null) {
                msg = "null";
            }
            Log.d(TAG, msg);
            post(TAG + "\t" + msg);
        }
    }

    public static void i(String msg) {
        if (isDebug()) {
            if (msg == null) {
                msg = "null";
            }
            Log.i(TAG, msg);
            post(TAG + "\t" + msg);
        }
    }

    public static void w(Throwable e) {
        w("", e);
    }

    public static void w(String msg, Throwable e) {
        if (msg == null) {
            msg = "null";
        }
        Log.w(TAG, msg, e);
        post(TAG + "\t" + msg, e);
    }

    public static void e(Throwable e) {
        e("", e);
    }

    public static void e(String msg, Throwable e) {
        if (msg == null) {
            msg = "null";
        }
        Log.e(TAG, msg, e);
        post(TAG + "\t" + msg, e);
    }

    public static void print(String msg) {
        if (isDebug()) {
            if (msg == null) {
                msg = "null";
            }
            Log.v(TAG, msg);
            post(TAG + "\t" + msg);
        }
    }

    public static void print(String tag, String obj) {
        if (isDebug()) {
            if (obj == null) {
                obj = "null";
            }
            Log.v(tag, obj);
            post(tag + "\t" + obj);
        }
    }
}
