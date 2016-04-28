package com.xd.adhocroute.utils;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.xd.adhocroute.AdhocRouteApp;

public class PrometheusServices extends Service {
	
	private String OLSR_START = "";
	private static final String PS = "su ps";
	private static final String OLSR_KILL = "su kill -9 ";
	public static final String CMD_OLSR = "olsrd";
	public static final String DNS = "su setprop net.dns1 8.8.8.8";
	private AdhocRouteApp app = null;
	// app states
	public final static int STATE_STOPPED = 0;
	public final static int STATE_STARTING = 1;
	public final static int STATE_RUNNING = 2; // process said OK
	// messages from the process
    final static int MSG_OUTPUT     = 1;
    final static int MSG_ERROR      = 2;
    final static int MSG_PID      = 3;
	// private state
	private Thread[] threads = new Thread[2];
	private Thread killThread = null;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_OUTPUT:
				
				break;
			case MSG_ERROR:
				
				break;
			case MSG_PID:
				int pid = (Integer)msg.obj;
				// 
				try {
					Process process = Runtime.getRuntime().exec(OLSR_KILL + pid);
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
	private String olsrdPath;
	private String olsrdConfPath;

	@Override
	public void onCreate() {
		super.onCreate();
		app = (AdhocRouteApp)getApplication();
		olsrdPath = new File(getDir("bin", Context.MODE_PRIVATE), "olsrd").getAbsolutePath();
		olsrdConfPath = new File(getFilesDir(), "olsrd.conf").getAbsolutePath();
		OLSR_START = "su " + olsrdPath + " -f " + olsrdConfPath + " -d 1" + " -i " + "wlan0";
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		app.serviceStarted(this);
		return super.onStartCommand(intent, flags, startId);
	}
	//         /data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0

	@Override
	public void onDestroy() {
		super.onDestroy();
		app.serviceDestroy();
	}
	private boolean startProcess(String cmd) {
		RouteUtils.execCmd(cmd);
//		try {
//			Process process = Runtime.getRuntime().exec(OLSR_START);
////			RouteUtils.execCmd(OLSR_START);
//			Log.i(AdhocRouteApp.TAG, OLSR_START);
//			threads[0] = new Thread(new OutputMonitor(MSG_OUTPUT, process.getInputStream()));
//            threads[1] = new Thread(new OutputMonitor(MSG_ERROR, process.getErrorStream()));
//            threads[0].start();
//            threads[1].start();
//			return true;
//		} catch (IOException e) {
//			Log.i(AdhocRouteApp.TAG, "=============================");
//			Log.i(AdhocRouteApp.TAG, "start olsrd failed in shell : "+ e.toString());
//			return false;
//		}
		return false;
	}
//   su /data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0


	private void stopProcess() {
		try {
			Process process = Runtime.getRuntime().exec(PS);
			killThread = new Thread(new OutputMonitor(MSG_OUTPUT, process.getInputStream()));
			killThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startOLSR() {
		Log.i(AdhocRouteApp.TAG, "startOLSRD");
		startProcess(OLSR_START);
	}
	
	public void stopOLSR() {
		Log.i(AdhocRouteApp.TAG, "stopOLSRD");
		stopProcess();
	}

	private class OutputMonitor implements Runnable {
        private final java.io.BufferedReader br;
        private final int msg;
        public OutputMonitor(int t, java.io.InputStream is) {
            br = new java.io.BufferedReader(new java.io.InputStreamReader(is), 8192);
            msg = t;
        }
        public void run() {
            try{
                String line = br.readLine();
                while(line != null) {
                    if (line.contains(olsrdPath)) {
						String pidStr = line.split("\\s+")[1];
						int pid = Integer.valueOf(pidStr);
						handler.obtainMessage(MSG_PID, pid).sendToTarget();
					}
                    line = br.readLine();
                 // 处理进程Process的消息
                 // handler.obtainMessage(msg, line).sendToTarget(); // NOTE: the last null is also sent!
                } 
            } catch (Exception e) {
                // 处理进程Process的消息
            	Log.i(AdhocRouteApp.TAG, "shell start olsr failed: " + e.toString());
            }
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}