package com.xd.adhocroute.net;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.xd.adhocroute.AdhocRouteApp;

public class AdhocHelper {
	protected static final int WIFI_START_SUCCEED = 0;
	protected static final int WIFI_CLOSE_SUCCEED = 1;
	protected static final int IP_NOT_SET = 2;
	private static final int ADHOC_NET_ID = 3;
	private static final int ADHOC_NET_FAILED = 4;
	private WifiAdmin wifiAdmin;
	public Handler handler;
	public AdhocRouteApp app;

	public AdhocHelper(final Context context) {
		wifiAdmin = new WifiAdmin(context);
		this.app = (AdhocRouteApp) context.getApplicationContext();
		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == WIFI_START_SUCCEED) {
					app.showToastMsg("开启WIFI成功");
				} else if (msg.what == WIFI_CLOSE_SUCCEED) {
					app.showToastMsg("断开网络成功");
				} else if (msg.what == IP_NOT_SET) {
					app.showToastMsg("设备IP未设置");
				} else if (msg.what == ADHOC_NET_ID) {
					app.showToastMsg("Adhoc网络创建成功，网络ID：" + (Integer) msg.obj);
				} else if (msg.what == ADHOC_NET_FAILED) {
					app.showToastMsg("Adhoc网络创建失败");
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
				//
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
		String ssid = app.preferenceUtils.getString("ssid", "AdhocRoute");
		String ip = app.preferenceUtils.getString("adhoc_ip", "");
		String mask = app.preferenceUtils.getString("adhoc_mask",
				"255.255.255.0");
		String channel = app.preferenceUtils.getString("lan_channel", "2412");
		if (ip.isEmpty()) {
			handler.sendEmptyMessage(IP_NOT_SET);
			return false;
		}
		int netID = -1;
		if ((netID = wifiAdmin.connect(ssid, ip, toNumMask(mask),
				Integer.parseInt(channel))) != -1) {
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