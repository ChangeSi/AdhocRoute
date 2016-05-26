package com.xd.adhocroute.nativehelper;

import android.util.Log;

public class NativeTask {
    
	public static final String MSG_TAG = "AdhocRoute -> NativeTask";

	static {
        try {
            Log.i(MSG_TAG, "Trying to load libwtnativetask.so");
            System.loadLibrary("nativecommand");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e(MSG_TAG, "Could not load libwtnativetask.so");
        }
    }
    public static native String getProp(String name);
    public static native int runCommand(String command);
    public static native int killProcess(int parameter, String processName);
}
