package com.xd.adhocroute;

import android.app.Application;
import android.content.Intent;

import com.xd.adhocroute.log.Lg;
import com.xd.adhocroute.log.WriteLogger;
import com.xd.adhocroute.route.RouteServices;

public class AdhocRouteApp extends Application {
	public static String TAG = "AdhocRoute";
	public RouteServices service = null;

	@Override
	public void onCreate() {
		super.onCreate();
		Lg.setLogger(WriteLogger.getInstance(getApplicationContext()));
		Lg.setDebug(true);
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
