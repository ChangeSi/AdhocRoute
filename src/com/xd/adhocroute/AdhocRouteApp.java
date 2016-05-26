package com.xd.adhocroute;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;
import android.content.Intent;

import com.xd.adhocroute.nativehelper.CoreTask;
import com.xd.adhocroute.route.RouteServices;

public class AdhocRouteApp extends Application {
	public static String TAG = "AdhocRoute";
	public RouteServices service = null;
	public ExecutorService executorService;
	public CoreTask coretask;
	
	public ExecutorService getGlobalThreadPool() {
		return executorService;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		coretask = new CoreTask();
		executorService = Executors.newFixedThreadPool(2);
	}

	public void startProcess(final String proc) {
		getGlobalThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				MainActivity.routeRunning = true;
				CoreTask.startProcess(proc);
				MainActivity.routeRunning = false;
			}
		});

		getGlobalThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(MainActivity.ACTION_DIALOG_HIDE_BROADCASTRECEIVER);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (MainActivity.routeRunning && coretask.isProcessRunning(RouteServices.CMD_OLSR)) {
					intent.putExtra("isStarted", true);
				} else {
					intent.putExtra("isStarted", false);
				}
				AdhocRouteApp.this.sendBroadcast(intent);
			}
		});
	}
	
	public void stopProcess(final String proc) {
		if (coretask.isProcessRunning(proc)) {
			getGlobalThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					if (coretask.killProcess(proc)) {
						MainActivity.routeRunning = false;
					}
				}
			});
		}
	}

	public void serviceStarted(RouteServices s) {
		service = s;
		service.startOLSR();
	}

	public void serviceDestroy() {
		service.stopOLSR();
		service = null;
	}

	public void startService() {
		startService(new Intent(this, RouteServices.class));
	}

	public void stopService() {
		if (service != null){
			service.stopOLSR();
			service.stopSelf();
		}
	}
	
}
