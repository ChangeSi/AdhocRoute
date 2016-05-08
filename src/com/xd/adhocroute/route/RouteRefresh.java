package com.xd.adhocroute.route;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import com.xd.adhocroute.data.OlsrDataDump;
import com.xd.adhocroute.data.Route;

public class RouteRefresh {
	protected static final int REFRESH_SUCCESS = 0X01;
	protected static final int REFRESH_FAILED = 0X02;
	public static final int REFRESH_HOST_UBREACHABLE = 0x03;
	private Callback refreshListener;
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	
	private static class MyHandler extends Handler{
		WeakReference<RouteRefresh> outerReference;
		
		public MyHandler(RouteRefresh outer) {
			outerReference = new WeakReference<RouteRefresh>(outer);;
		}
		public void handleMessage(android.os.Message msg) {
			RouteRefresh routeRefresh = outerReference.get();
			if (routeRefresh == null) return;
			switch (msg.what) {
			case REFRESH_SUCCESS:
				routeRefresh.refreshListener.onSuccess((List<Route>) msg.obj);
				break;
			case REFRESH_HOST_UBREACHABLE:
				routeRefresh.refreshListener.onException(REFRESH_HOST_UBREACHABLE);
				break;
			default:
				break;
			}
		}		
		
	}
	
	private Handler handler = new MyHandler(this);

	public void refreshRoute(Callback refreshListener) {
		this.refreshListener = refreshListener;
		exec.execute(new RefreshRunnable());
	}

	private class RefreshRunnable implements Runnable {

		public void run() {
			JsonInfo jsonInfo = new JsonInfo();
			OlsrDataDump olsrDataDump = jsonInfo.all();
			if (olsrDataDump.getRaw().equals("")) {
				// 路由未开启
				handler.sendEmptyMessage(REFRESH_HOST_UBREACHABLE);
			}
			else {
				// 开启了路由可以看到信息
				handler.obtainMessage(REFRESH_SUCCESS, olsrDataDump.routes).sendToTarget();
			}
		}
	}

	public interface Callback {
		public void onSuccess(List<Route> routeTables);
		public void onException(int exception);
	}

}
