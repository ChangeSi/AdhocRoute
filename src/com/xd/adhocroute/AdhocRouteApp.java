package com.xd.adhocroute;

import android.app.Application;
import android.content.Intent;

import com.xd.adhocroute.utils.RouteServices;

public class AdhocRouteApp extends Application {
	public static String TAG = "AdhocRoute";
	public RouteServices service = null;

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
