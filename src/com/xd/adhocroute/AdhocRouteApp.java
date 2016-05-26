package com.xd.adhocroute;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;
import android.content.Intent;

import com.xd.adhocroute.route.RouteServices;

public class AdhocRouteApp extends Application {
	public static String TAG = "AdhocRoute";
	public RouteServices service = null;
	public ExecutorService executorService;

	public ExecutorService getGlobalThreadPool() {
		return executorService;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		executorService = Executors.newFixedThreadPool(2);
	}
	
//	private void initLogger() {
//		Lg.setLogger(WriteLogger.getInstance(getApplicationContext()));
//		Lg.setDebug(true);
//	}
	
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
