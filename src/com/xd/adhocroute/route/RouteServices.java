package com.xd.adhocroute.route;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.xd.adhocroute.AdhocRouteApp;

public class RouteServices extends Service {
	public static final String CMD_OLSR = "app_bin/olsrd";
	public static final String DNS = "su setprop net.dns1 8.8.8.8";
	
	private String olsrdPath;
	private String olsrdConfPath;
	private String OLSR_START = "";
	
	private AdhocRouteApp app = null;
	
	// APP state
	public final static int STATE_STOPPED = 0;
	public final static int STATE_NET_FAILED = 1;
	public final static int STATE_NET_SUCCEED = 2;
	public final static int STATE_ROUTE_FAILED = 3;
	public final static int STATE_ROUTE_RUNNING = 4;
	
    final static int MSG_OUTPUT     = 1;
    final static int MSG_ERROR      = 2;
    final static int MSG_PID      = 3;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = (AdhocRouteApp)getApplication();
		olsrdPath = new File(getDir("bin", Context.MODE_PRIVATE), "olsrd").getAbsolutePath();
		olsrdConfPath = new File(getFilesDir(), "olsrd.conf").getAbsolutePath();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String inface = sp.getString("interface", "wlan0");
		OLSR_START = olsrdPath + " -f " +  olsrdConfPath + " -i " + inface;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		app.serviceStarted(this);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		app.serviceDestroy();
		super.onDestroy();
	}
	
	public void startOLSR() {
		app.startProcess(OLSR_START);
	}
	
	public void stopOLSR() {
		app.stopProcess(CMD_OLSR);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}