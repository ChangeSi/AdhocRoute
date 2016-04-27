package com.xd.adhocroute.utils;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.xd.adhocroute.AdhocRouteApp;

public class PrometheusServices extends Service {
	private static final String OLSR_START = "su /data/local/bin/olsrd -f /data/local/etc/olsrd.conf  -d 1";
	private static final String PS = "su ps";
	private static final String OLSR_KILL = "su kill -9 ";
	public static final String CMD_OLSR = "/data/local/bin/olsrd";
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

	@Override
	public void onCreate() {
		super.onCreate();
		app = (AdhocRouteApp)getApplication();
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		app.serviceStarted(this);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		app.serviceDestroy();
	}
	private boolean startProcess(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec(OLSR_START);
			threads[0] = new Thread(new OutputMonitor(MSG_OUTPUT, process.getInputStream()));
            threads[1] = new Thread(new OutputMonitor(MSG_ERROR, process.getErrorStream()));
            threads[0].start();
            threads[1].start();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

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
		startProcess(OLSR_START);
	}
	
	public void stopOLSR() {
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
                String line;
                do {
                    line = br.readLine();
                    if (line.contains(CMD_OLSR)) {
						String pidStr = line.split("\\s+")[1];
						int pid = Integer.valueOf(pidStr);
						handler.obtainMessage(MSG_PID, pid).sendToTarget();
					}
                 // 处理进程Process的消息
                 // handler.obtainMessage(msg, line).sendToTarget(); // NOTE: the last null is also sent!
                } while(line != null);
            } catch (Exception e) {
                // 处理进程Process的消息
            }
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}