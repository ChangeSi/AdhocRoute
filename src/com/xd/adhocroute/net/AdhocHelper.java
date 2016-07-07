package com.xd.adhocroute.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.xd.adhocroute.AdhocRouteApp;
import com.xd.adhocroute.R;

/**
 * 为了解耦合建网和路由的关系，同时需要在建网的时候获取建网成功与否的状态
 * @author qhyuan1992
 *
 */
public class AdhocHelper {
	protected static final int WIFI_START_SUCCEED = 0;
	protected static final int WIFI_CLOSE_SUCCEED = 1;
	protected static final int IP_NOT_SET = 2;
	private static final int ADHOC_NET_ID = 3;
	private static final int ADHOC_NET_FAILED = 4;
	private WifiAdmin wifiAdmin;
	public Handler handler;
	public AdhocRouteApp application;

	@SuppressLint("HandlerLeak")
	public AdhocHelper(final Context context) {
		wifiAdmin = new WifiAdmin(context);
		this.application = (AdhocRouteApp) context.getApplicationContext();
		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == WIFI_START_SUCCEED) {
					application.showToastMsg(R.string.toast_open_wifi_succeed);
				} else if (msg.what == WIFI_CLOSE_SUCCEED) {
					//application.showToastMsg(R.string.toast_close_wifi_succeed);
				} else if (msg.what == IP_NOT_SET) {
					application.showToastMsg(R.string.toast_device_ip_not_set);
				} else if (msg.what == ADHOC_NET_ID) {
					application.showToastMsg(context.getString(R.string.toast_net_id) + (Integer) msg.obj);
				} else if (msg.what == ADHOC_NET_FAILED) {
					application.showToastMsg(R.string.toast_adhoc_build_adhoc_failed);
				}
			};
		};
	}

	public boolean openWifiAndConnect() {
		wifiAdmin.openWifi();
		try {
			while (!wifiAdmin.isWifiEnabled()) {
				Thread.sleep(200);
			}
			wifiAdmin.openWifi();
			while (!(wifiAdmin.checkState() == WifiManager.WIFI_STATE_ENABLED)) {
				Thread.sleep(200);
			}
			handler.sendEmptyMessage(WIFI_START_SUCCEED);
			if (connectWithRightParams()) {
				return true;
			} else {
				return false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
		// app.executorService.execute(new Runnable() {
		// @Override
		// public void run() {}
		// });
	}

	public void exitNet() {
		if (wifiAdmin.getConnectionInfo() != null) {
			int connectId = wifiAdmin.getNetWorkId();
			wifiAdmin.disConnectionWifi(connectId);
		}
		closeWifi();
	}

	private boolean connectWithRightParams() {
		String ssid = application.preferenceUtils.getString("ssid", "AdhocRoute");
		String ip = application.preferenceUtils.getString("adhoc_ip", "");
		String gateway = application.preferenceUtils.getString("gateway", "192.168.2.33");
		String mask = application.preferenceUtils.getString("adhoc_mask", "255.255.255.0");
		String channel = application.preferenceUtils.getString("lan_channel", "2412");
		if (ip.isEmpty()) {
			handler.sendEmptyMessage(IP_NOT_SET);
			return false;
		}
		int netID = -1;
		if ((netID = wifiAdmin.connect(ssid, ip, gateway, toNumMask(mask), Integer.parseInt(channel))) != -1) {
			handler.obtainMessage(ADHOC_NET_ID, netID).sendToTarget();
			return true;
		} else {
			// 建网失败
			handler.obtainMessage(ADHOC_NET_FAILED, netID).sendToTarget();
			return false;
		}
	}

	private void closeWifi() {
		wifiAdmin.closeWifi();
		try {
			while (wifiAdmin.isWifiEnabled()) {
				Thread.sleep(200);
			}
			wifiAdmin.closeWifi();
			while (wifiAdmin.checkState() == WifiManager.WIFI_STATE_ENABLED) {
				Thread.sleep(200);
			}
			handler.sendEmptyMessage(WIFI_CLOSE_SUCCEED);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// app.executorService.execute(new Runnable() {
		// @Override
		// public void run() {}
		// });
	}

	public int toNumMask(String maskStr) {
		String[] ipSegment = maskStr.split("\\.");
		int num = 0;
		for (int n = 0; n < ipSegment.length; n++) {
			char[] array = Integer.toBinaryString(
					Integer.parseInt(ipSegment[n])).toCharArray();
			for (int i = 0; i < array.length; i++) {
				if (array[i] == '1') {
					num++;
				}
			}
		}
		return num;
	}

}