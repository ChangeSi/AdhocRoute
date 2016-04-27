package com.xd.adhocroute;

import android.app.Application;
import android.content.Intent;

import com.xd.adhocroute.utils.PrometheusServices;

public class AdhocRouteApp extends Application {
	public static String TAG = "AdhocRoute";
	public PrometheusServices service = null;

	public void serviceStarted(PrometheusServices s) {
		service = s;
		service.startOLSR();
	}

	public void serviceDestroy() {
		service.stopOLSR();
		service = null;
	}

	public void startService() {
		startService(new Intent(this, PrometheusServices.class));
	}

	public void stopService() {
		if (service != null){
			service.stopOLSR();
			service.stopSelf();
		}
	}
}
