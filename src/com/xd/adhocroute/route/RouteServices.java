package com.xd.adhocroute.route;

import java.io.BufferedReader;
import java.io.File;
import java.io.OutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.xd.adhocroute.AdhocRouteApp;
import com.xd.adhocroute.log.Lg;
import com.xd.adhocroute.utils.ShellUtils;

public class RouteServices extends Service {
	
	private String olsrdPath;
	private String olsrdConfPath;
	
	private String OLSR_START = "";
	private static final String PS = "ps";
	private static final String OLSR_KILL = "kill -9 ";
	public static final String CMD_OLSR = "app_bin/olsrd";
	public static final String DNS = "su setprop net.dns1 8.8.8.8";
	private AdhocRouteApp app = null;
	// app states
	public final static int STATE_STOPPED = 0;
	public final static int STATE_STARTING = 1;
	public final static int STATE_RUNNING = 2;
	
    final static int MSG_OUTPUT     = 1;
    final static int MSG_ERROR      = 2;
    final static int MSG_PID      = 3;
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_OUTPUT:
				
				break;
			case MSG_ERROR:
				
				break;
			case MSG_PID:
				int pid = (Integer)msg.obj;
				try {
					Process process = Runtime.getRuntime().exec("su");
					OutputStream os = process.getOutputStream();
					os.write((OLSR_KILL + pid).getBytes());
					os.close();
					process.waitFor();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = (AdhocRouteApp)getApplication();
		olsrdPath = new File(getDir("bin", Context.MODE_PRIVATE), "olsrd").getAbsolutePath();
		olsrdConfPath = new File(getFilesDir(), "olsrd.conf").getAbsolutePath();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String inface = sp.getString("interface", "");
		OLSR_START = olsrdPath + " -f " + olsrdConfPath + " -d 0" + " -i " + inface;
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
	private void startProcess() {
		ShellUtils.safeStartOlsrd(OLSR_START);
	}
	
	private void stopProcess() {
		try {
			Process process = Runtime.getRuntime().exec("su");
			OutputStream os = process.getOutputStream();
			os.write(PS.getBytes());
			os.close();
			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
			String line = br.readLine();;
            while(line != null){
                if (line.contains(CMD_OLSR)) {
					String pidStr = line.split("\\s+")[1];
					int pid = Integer.valueOf(pidStr);
					handler.obtainMessage(MSG_PID, pid).sendToTarget();
				}
                line = br.readLine();
            }
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startOLSR() {
		Log.i(AdhocRouteApp.TAG, "startOLSRD");
		startProcess();
		Lg.d("adhocroute-test 执行完了startProcess");
	}
	
	public void stopOLSR() {
		Log.i(AdhocRouteApp.TAG, "stopOLSRD");
		stopProcess();
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}