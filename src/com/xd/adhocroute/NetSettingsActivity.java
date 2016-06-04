package com.xd.adhocroute;

import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class NetSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private ListPreference channel;
	private String[] channelEntry;
	private AdhocRouteApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.netpreferences);
		app = (AdhocRouteApp)getApplication();
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen()
				.getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	private void init() {
		channel = (ListPreference) findPreference("lan_channel");
		channel.setEntryValues(getAllChannels());
		channel.setValue(app.preferenceUtils.getString("lan_channel", "2412"));
		channel.setEntries(getAllChannelNames());
	}

	private String[] getAllChannels() {
		List<WifiChannel> cs = ((WifiManager) getSystemService(WIFI_SERVICE)).getSupportedChannels();// new api
		if (cs.size() > 0) {
			channelEntry = new String[cs.size()];
			for (int i = 0; i < cs.size(); i++) {
				channelEntry[i] = Integer.toString(cs.get(i).frequency);
			}
		} else {
			channelEntry = new String[1];
			channelEntry[0] = Integer.toString(2412);
		}
		return channelEntry;
	}

	private String[] getAllChannelNames() {
		String[] channelName = new String[channelEntry.length];
		for (int i = 0; i < channelEntry.length; i++) {
			channelName[i] = i + 1 + " - " + channelEntry[i] + " MHz";
		}
		return channelName;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

	}

}