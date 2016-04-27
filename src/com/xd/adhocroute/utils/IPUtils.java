package com.xd.adhocroute.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

public class IPUtils {
	private static final String TAG = "Util";
	private static Pattern IpPattern;
	private static String WIFI_DEV_NAME = null;
	private static String WIFI_IP_ADDR = null;
	
	public static String getAdhocIpString() { 
		if (WIFI_IP_ADDR != null && WIFI_DEV_NAME != null) {
			return WIFI_IP_ADDR;
		}
        try { 

            for (Enumeration<NetworkInterface> en = NetworkInterface 
                    .getNetworkInterfaces(); en.hasMoreElements();) { 
                NetworkInterface intf = en.nextElement(); 
                String name = intf.getDisplayName();
                if (name.equals("wlan0") ) {
                	for (Enumeration<InetAddress> enumIpAddr = intf 
                			.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
                		InetAddress inetAddress = enumIpAddr.nextElement(); 
                		if(!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress().toString())) {
                			WIFI_IP_ADDR = inetAddress.getHostAddress().toString();
                			WIFI_DEV_NAME = "wlan0";
                			return WIFI_IP_ADDR; 
                		}
                	}
				}
                
                if(name.equals("eth0")){
                	for (Enumeration<InetAddress> enumIpAddr = intf 
                			.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
                		InetAddress inetAddress = enumIpAddr.nextElement(); 
                		if(!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress().toString())) {
                			WIFI_IP_ADDR = inetAddress.getHostAddress().toString();
                			WIFI_DEV_NAME = "eth0";
                			return WIFI_IP_ADDR; 
                		}
                	} 
                }
            } 
        } catch (SocketException ex) { 
            Log.e("Print", ex.toString()); 
        } 
        return null; 
    } 
	
	public static boolean matchIP(String address){
			if(IpPattern == null){
				String ipRegex =	"(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})" +
									"\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})" +
									"\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})" +
									"\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
				IpPattern = Pattern.compile(ipRegex);
			}
			return IpPattern.matcher(address).matches();
	}

	public static byte getAdhocLastIpByte() {
		return TransUtils.int2Byte(getAdhocLastIpInt());
	}
	
	public static int getAdhocLastIpInt(){
		String lastIp = getAdhocLastIpString();
		if (lastIp == null) {
			return 0;
		}
		return Integer.parseInt(lastIp);
	}
	
	public static String getAdhocLastIpString() {
		String ipStr = getAdhocIpString();
		if (ipStr == null) {
			return null;
		}
		return ipStr.substring(ipStr.lastIndexOf(".")+1);
	}
	/**
	 * 获取网络地址前缀，比如“192.168.2.10 ”的地址前缀为“192.168.2.”
	 * @return 网络地址前缀
	 */
	public static String getAdHocIpPrefix(){
		String ipStr = getAdhocIpString();
		if (ipStr == null) {
			return null;
		}
		return ipStr.substring(0, ipStr.lastIndexOf(".")+1);
	}
	
	public static String getBroadcastIpString() {
		return getAdHocIpPrefix()+"255";
	}
	public static String getWifiDevName(){
		if (WIFI_DEV_NAME != null) {
			return WIFI_DEV_NAME;
		}
		getAdhocIpString();
		return WIFI_DEV_NAME;
	}
}
