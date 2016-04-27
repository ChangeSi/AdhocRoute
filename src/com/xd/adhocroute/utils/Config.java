package com.xd.adhocroute.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Config {
	private String mBaseConfiguration;
	private Vector<Config.ConfigInfo> vInfos;
	
	public Config(String baseConfiguration) throws IOException, FileNotFoundException {
		this(new FileInputStream(baseConfiguration));
	}
	
	public Config(InputStream base) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader buf = new BufferedReader(new InputStreamReader(base));		
		if (buf != null) {
			String line;
			while ((line = buf.readLine()) != null) {
				builder.append(line);
				builder.append("\n");
			}
			buf.close();
		}
		mBaseConfiguration = new String(builder);
		vInfos = new Vector<Config.ConfigInfo>();
	}
	
	public void addConfigInfo(Config.ConfigInfo configInfo) {
		vInfos.add(configInfo);
	}
	
	// 基本的配置 + SharedPreference里面的配置
	public String toString() {
		StringBuilder builder = new StringBuilder(mBaseConfiguration);
		for (Config.ConfigInfo s : vInfos) {
			builder.append(s.toString());
		}
		return new String(builder);
	}
	
	public void write(OutputStream out) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		if (writer != null) {
			writer.write(toString());
			writer.flush();
			writer.close();
		}
	}
	
	// 路由的配置信息
	public static class ConfigInfo {
		protected Map<String, String> mKvs;
		protected String mDirective;
		protected String mDirectiveParameter;
		public ConfigInfo() {
			mKvs = new HashMap<String,String>();
		}
		public void addKeyValue(String key, String value) {
			mKvs.put(key, value);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (mDirective != null && mDirectiveParameter != null) {
				builder.append(mDirective);
				builder.append(" ");
				builder.append(mDirectiveParameter);
			}
			builder.append("{\n");
			for (Map.Entry<String, String> e : mKvs.entrySet()) {
				builder.append("\"" + e.getKey() + "\"=\"" + e.getValue() + "\"\n");
			}
			builder.append("}\n");
			return new String(builder);
		}
	}
	public static class NormalConfigInfo extends ConfigInfo {
		public NormalConfigInfo(String netInfo) {
			super();
			mDirective = "Hna4";
			mDirectiveParameter = netInfo;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(mDirective);
			builder.append(" ");
			builder.append("\n{\n");
			builder.append(mDirectiveParameter);
			builder.append("\n");
			builder.append("}\n");
			return new String(builder);
		}
	}
	
	public static class PluginConfigInfo extends ConfigInfo{
		public PluginConfigInfo(String pluginPath) {
			super();
			mDirective = "LoadPlugin";
			mDirectiveParameter = "\"" + pluginPath + "\"";
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(mDirective);
			builder.append(" ");
			builder.append(mDirectiveParameter);
			builder.append("\n{\n");
			for (Map.Entry<String, String> e : mKvs.entrySet()) {
				builder.append("\tPlParam \"" + e.getKey() + "\" \"" + e.getValue() + "\"\n");
			}
			builder.append("}\n");
			return new String(builder);
		}
	}
}
