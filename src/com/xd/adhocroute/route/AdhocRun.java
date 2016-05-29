package com.xd.adhocroute.route;

import java.net.InetAddress;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xd.adhocroute.AdhocRouteApp;

/**
 * 用来创建Adhoc网络
 * @author qhyuan1992
 *
 */
@Deprecated
public class AdhocRun {
	private Thread mScanTread;
	private Context context;
	private WifiManager wifiManager;
	private WifiConfigurationNew wifiConfig;
//	private boolean showScan = true;
	private int networkID;

	public AdhocRun(Context context) {
		this.context = context;
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiConfig = new WifiConfigurationNew();
	}

	public void startScanThread() {
		mScanTread = new Thread(new Runnable() {
			@Override
			public void run() {
				startScan();
				synchronized (this) {
					while (true) {
						Log.i(AdhocRouteApp.TAG, "start rescan.");
						try {
							wait(2000);
//							if(showScan == false)
//                                continue;
							wifiManager.startScan();
							wait(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		mScanTread.start();
	}

	private void startScan() {
		if (wifiManager != null) {
			if (wifiManager.isWifiEnabled() == false) {
				wifiManager.setWifiEnabled(true);
			}
			wifiManager.startScan();
		}
	}
	
	public void exitAdhoc(){
		disConnectionWifi(networkID);
		closeWifi();
	}
	
	private void disConnectionWifi(int netId) {
		wifiManager.disableNetwork(netId);
		wifiManager.disconnect();
	}
	
	private void closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }
	
	public boolean constructAdhoc() {
		startScan();
//		showScan = false;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String ssid = sp.getString("ssid", "");
		String channel = sp.getString("lan_channel", "2412");
		String ip = sp.getString("adhoc_ip", "");
		String mask = sp.getString("adhoc_mask", "");

		// 进行检查参数是否设置以及合理性
		if (checkParams(ssid, channel, ip, mask)) {
			try {
				wifiConfig.SSID = "\"" + ssid + "\"";
				wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
				wifiConfig.setIpAssignment("STATIC");
				wifiConfig.setIpAddress(InetAddress.getByName(ip),24);
				wifiConfig.setDNS(InetAddress.getByName("8.8.8.8"));
				wifiConfig.isIBSS = true;// new api
				wifiConfig.frequency = Integer.parseInt(channel);// new
				
				WifiConfiguration tempConfig = isExsits(wifiConfig.SSID);
	    		if (tempConfig != null) {
	    			wifiManager.removeNetwork(tempConfig.networkId);
	    		}
				networkID = wifiManager.addNetwork(wifiConfig);
				if (networkID < 0) {
					Log.i(AdhocRouteApp.TAG, "Failed to add Ad-hoc network");
					Toast.makeText(context, "Failed to add Ad-hoc network" + networkID, 0).show();
				} else {
					wifiManager.enableNetwork(networkID, true);
					wifiManager.saveConfiguration();
				}
			} catch (Exception e) {
				Log.i(AdhocRouteApp.TAG, "Wifi configuration failed!");
				Toast.makeText(context, "Failed to add Ad-hoc network", 0).show();
				e.printStackTrace();
				return false;
			}
			Toast.makeText(context,"Ad-hoc start successfully id " + wifiManager.getConnectionInfo().getNetworkId(), 0).show();
			return true;
		}
		return false;
	}
	
    private WifiConfiguration isExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (("\"" + SSID + "\"").equals(existingConfig.SSID)) {
				return existingConfig;
			}
		}
		return null;
	}
    
	private boolean checkParams(String ssid, String channel, String ip,
			String mask) {
		String show = "";
		if (ssid.equals(""))
			show = "ssid";
		else if (channel.equals(""))
			show = "channel";
		else if (ip.equals(""))
			show = "ip";
		else if (mask.equals(""))
			show = "mask";
		if (!show.equals("")) {
			Toast.makeText(context, show + " is not set", 0).show();
			return false;
		}
		return true;
		// 继续检查参数是否规范
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
