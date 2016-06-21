package com.xd.adhocroute.net;

import java.net.InetAddress;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

public class WifiAdmin {
	private static final String TAG = "AdhocRoute -> WifiAdmin";
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	private WifiLock wifiLock;

	public WifiAdmin(Context context) {
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

    public WifiInfo getConnectionInfo() {
        wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }
    
	public void openWifi() {
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
	}

	public void closeWifi() {
		if (wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(false);
		}
	}

	public boolean isWifiEnabled () {
		return wifiManager.isWifiEnabled();
	}

    public int checkState() {
        return wifiManager.getWifiState();
    }

    public void createWifiLock(String tag) {
        wifiLock = wifiManager.createWifiLock(tag);
    }

    public void acquireWifiLock() {
        wifiLock.acquire();
    }


    public void releaseWifiLock() {
        // 判断是否锁定
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }
    
    // very important
    public void disConnectionWifi(int netId) {
        wifiManager.disableNetwork(netId);
        wifiManager.disconnect();
    }
    

    /**
     * 创建/连接到某个网络并设置IP等信息
     * @param ssid
     * @param ipAddr
     * @param mask
     * @param frequency
     * @return 返回值表示创建的网络的ID，-1表示网络创建失败
     */
    public int connect(String ssid, String ipAddr, int mask, int frequency) {
    	WifiConfigurationNew wifiConfig = new WifiConfigurationNew();
		try {
		wifiConfig.SSID = "\"" + ssid + "\"";
		wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
		/* Use reflection to configure static IP addresses */
		wifiConfig.setIpAssignment("STATIC");
		wifiConfig.setIpAddress(InetAddress.getByName(ipAddr), mask);
		wifiConfig.setDNS(InetAddress.getByName("8.8.8.8"));
		wifiConfig.isIBSS = true;//new api
        // set frequency.
		wifiConfig.frequency = frequency;//new api
        WifiConfiguration tempConfig = exsitSSIDConfig(ssid);
		if (tempConfig != null) {
			wifiManager.removeNetwork(tempConfig.networkId);
		}
        int id = wifiManager.addNetwork(wifiConfig);
        if (id < 0) {
            Log.i(TAG, "Failed to add Ad-hoc network");
        } else {
            wifiManager.enableNetwork(id, true);
            wifiManager.saveConfiguration();
        }
        return id;
		} catch(Exception e) {
			return -1;
		}
	}
    
    public String getMacAddress() {
        return getConnectionInfo() == null ? "null" : getConnectionInfo().getMacAddress();
    }

    public String getBSSID() {
        return getConnectionInfo() == null ? "null" : getConnectionInfo().getBSSID();
    }

    public int getIpAddress() {
        return getConnectionInfo() == null ? 0 : getConnectionInfo().getIpAddress();
    }

    // 得到连接的ID important
    public int getNetWorkId() {
        return getConnectionInfo() == null ? 0 : getConnectionInfo().getNetworkId();
    }

    private WifiConfiguration exsitSSIDConfig(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (("\"" + SSID + "\"").equals(existingConfig.SSID)) {
				return existingConfig;
			}
		}
		return null;
	}
}
