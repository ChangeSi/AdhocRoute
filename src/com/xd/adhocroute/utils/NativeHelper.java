package com.xd.adhocroute.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.xd.adhocroute.AdhocRouteApp;

public class NativeHelper {
	public static final String TAG = "NativeHelper";

	public static File publicFiles;
	public static File profileDir;
	public static File app_bin;
	public static File app_log;
	public static File shared_prefs;

	private static String OLSRD_HTTP;
	private static String OLSRD;
	private static String OLSRD_DYN_GW;
	private static String OLSRD_CONF;


	public static void setup(Context context) {
		app_bin = context.getDir("bin", Context.MODE_PRIVATE).getAbsoluteFile();
		OLSRD = new File(app_bin, "olsrd").getAbsolutePath();
		OLSRD_HTTP = new File(app_bin, "olsrd_dyn_gw_plain.so.0.4").getAbsolutePath();
		OLSRD_DYN_GW = new File(app_bin, "olsrd_httpinfo.so.0.1").getAbsolutePath();
		OLSRD_CONF = new File(app_bin, "olsrd.conf").getAbsolutePath();
	}

	public static boolean unzipAssets(Context context) {
		boolean result = true;
		try {
			AssetManager am = context.getAssets();
			final String[] assetList = am.list("");
			Map<String,File> unzipTo = new HashMap<String,File>();
			Map<String,Boolean> overwriteOnUnzip = new HashMap<String,Boolean>();
			for (String asset : assetList) {
				if (asset.equals("images") || asset.equals("sounds") || asset.equals("webkit") || asset.equals("databases") || asset.equals("kioskmode"))
					continue;
				int BUFFER = 2048;
				final File file;
				String extension = asset.substring((asset.lastIndexOf('.') != -1) ? asset.lastIndexOf('.') : 0);
				File directory = unzipTo.get(extension);
				boolean overwrite = (overwriteOnUnzip.get(extension) == null) ? true : overwriteOnUnzip.get(extension);; 
				Log.d("NativeHelper", "extension: " + extension);
				Log.d("NativeHelper", "directory: " +
						((directory != null) ? directory.getAbsolutePath() : "N/A") +
						((overwrite) ? "(overwrite)" : "(keep)"));
				if (directory != null) {
					file = new File(directory, asset);
					if (file.exists() && !overwrite) {
						continue;
					}
				} else {
					file = new File(NativeHelper.app_bin, asset);
				}
				InputStream tmp;
				try {
					tmp = am.open(asset);
				} catch (FileNotFoundException e) {
					// if asset is a directory, we'll get this exception
					e.printStackTrace();
					continue;
				}
				final InputStream assetIS = tmp;
				if (file.exists()) {
					file.delete();
					Log.i(AdhocRouteApp.TAG, "DebiHelper.unzipDebiFiles() deleting "
							+ file.getAbsolutePath());
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

				int count;
				byte[] data = new byte[BUFFER];
				while ((count = assetIS.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				assetIS.close();
			}
		} catch (IOException e) {
			result = false;
			Log.e(AdhocRouteApp.TAG, "Can't unzip", e);
		}
		chmod("0750", new File(OLSRD));
		return result;
	}

	public static void chmod(String modestr, File path) {
		Log.i(TAG, "chmod " + modestr + " " + path.getAbsolutePath());
		try {
			Class<?> fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions = fileUtils.getMethod("setPermissions", String.class,
					int.class, int.class, int.class);
			int mode = Integer.parseInt(modestr, 8);
			int a = (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode,
					-1, -1);
			if (a != 0) {
				Log.i(TAG, "ERROR: android.os.FileUtils.setPermissions() returned " + a
						+ " for '" + path + "'");
			}
		} catch (ClassNotFoundException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (IllegalAccessException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (InvocationTargetException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (NoSuchMethodException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		}
	}
	
	// 每次启动的时候去检查设置信息，修改配置文件 
	public static void updateConfig(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String wan_info = sp.getString("wan", "");
		boolean open_gateway_info = sp.getBoolean("open_gateway", false);
		
		String baseConfig = NativeHelper.app_bin.getAbsolutePath() + "/olsrd.conf";
		String dyngatewayPath = NativeHelper.app_bin.getAbsolutePath() + "/olsrd_dyn_gw_plain.so.0.4";
		
		try {
			// config里面已经有了基础的配置信息
			Config config = new Config(baseConfig);
			if(!wan_info.equals("")){
				config.addConfigInfo(new Config.NormalConfigInfo(wan_info));
			}
			if (open_gateway_info) {
				config.addConfigInfo(new Config.PluginConfigInfo(dyngatewayPath));
			}
			// 
			FileOutputStream olsrdConf = context.openFileOutput("olsrd.conf", 0);
			config.write(olsrdConf);
			olsrdConf.close();
			
		} catch (FileNotFoundException e) {
			Log.i(AdhocRouteApp.TAG, "baseConfig file not found");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 
		
		
		
	}
}
