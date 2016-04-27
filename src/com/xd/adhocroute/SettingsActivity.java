package com.xd.adhocroute;

import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private SharedPreferences sp;
	private EditTextPreference ssid;
	private ListPreference channel;
	private EditTextPreference ip;
	private EditTextPreference mask;
	private EditTextPreference wan;
	private String [] channelEntry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void init() {
		ssid = (EditTextPreference) findPreference("ssid");
		channel = (ListPreference) findPreference("lan_channel");
		ip = (EditTextPreference) findPreference("adhoc_ip");
		mask = (EditTextPreference) findPreference("adhoc_mask");
		wan = (EditTextPreference) findPreference("wan");
		channel.setEntryValues(getAllChannels());
		channel.setValue(sp.getString("lan_channel", channelEntry[0]));
		channel.setEntries(getAllChannelNames());
	}

	private String [] getAllChannels() {
		List<WifiChannel> cs = ((WifiManager) getSystemService(WIFI_SERVICE)).getSupportedChannels();//new api
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
	
	private String [] getAllChannelNames() {
		String [] channelName = new String[channelEntry.length];
		for (int i = 0; i < channelEntry.length; i++) {
			channelName[i] = i+1 + " - " + channelEntry[i] + " MHz";
		}
		return channelName;
	}
	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
	}

	
}
