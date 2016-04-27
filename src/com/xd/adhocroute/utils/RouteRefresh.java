package com.xd.adhocroute.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Handler;
import android.os.Message;

import com.xd.adhocroute.data.RouteItem;

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
				routeRefresh.refreshListener.onSuccess((List<RouteItem>) msg.obj);
				break;
			case REFRESH_FAILED:
				routeRefresh.refreshListener.onFailed();
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
	
	
//	private static Handler handler = new Handler() {
//		
//		
//		public void handleMessage(android.os.Message msg) {
//			
//			switch (msg.what) {
//			case REFRESH_SUCCESS:
//				refreshListener.onSuccess((List<RouteItem>) msg.obj);
//				break;
//			case REFRESH_FAILED:
//				refreshListener.onFailed();
//				break;
//			case REFRESH_HOST_UBREACHABLE:
//				refreshListener.onException(REFRESH_HOST_UBREACHABLE);
//				break;
//			default:
//				break;
//			}
//		}
//	};

	public void refreshRoute(String url, Callback refreshListener) {
		this.refreshListener = refreshListener;
		exec.execute(new RefreshRunnable(url));
	}

	private class RefreshRunnable implements Runnable {

		private String url;

		public RefreshRunnable(String url) {
			this.url = url;
		}

		// @Override
		// public void run() {
		// try {
		// URL url1 = new URL(url);
		// HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
		// conn.setRequestMethod("GET");
		// // conn.setRequestProperty("connection", "close");
		// conn.setDoInput(true);
		// conn.setConnectTimeout(1000);
		// conn.connect();
		// int code = conn.getResponseCode();
		// Message msg = Message.obtain();
		// handler.sendMessage(msg);
		// if (code == 200) {
		// InputStream instStream = conn.getInputStream();
		// String routeContent= stream2String(instStream);
		// System.out.println(routeContent);
		// // String routeContent = EntityUtils.toString(response.getEntity());
		// List<RouteItem> routeTables = new ArrayList<RouteItem>();
		// JSONObject jsonObject = new JSONObject(routeContent);
		// JSONArray routeArray = (JSONArray) jsonObject.get("routes");
		// for (int i = 0; i < routeArray.length(); i++) {
		// JSONObject routeItem = (JSONObject) routeArray.get(i);
		// RouteItem item = new RouteItem();
		// item.setDestination(routeItem.getString("destination"));
		// item.setGenmask(routeItem.getInt("genmask"));
		// item.setGateway(routeItem.getString("gateway"));
		// item.setMetric(routeItem.getInt("metric"));
		// item.setRtpMetricCost(routeItem.getInt("rtpMetricCost"));
		// item.setNetworkInterface(routeItem.getString("networkInterface"));
		// routeTables.add(item);
		// }
		// msg.what = REFRESH_SUCCESS;
		// msg.obj = routeTables;
		// handler.sendMessage(msg);
		// } else {
		// msg.what = REFRESH_FAILED;
		// msg.obj = null;
		// handler.sendMessage(msg);
		// }
		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// System.out.println("---------------------------");
		//
		// e.printStackTrace();
		// }
		// }

		public void run() {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(URI.create(url));
			try {
				HttpResponse response = client.execute(get);
				int code = response.getStatusLine().getStatusCode();
				Message msg = Message.obtain();
				System.out.println("------->" + code);
				if (code == 200) {
					String routeContent = EntityUtils.toString(response.getEntity());
					List<RouteItem> routeTables = getRouteItem(routeContent);
					// json解析
					// System.out.println("routeContent-------------" +
					// routeContent);
					// JSONObject jsonObject = new JSONObject(routeContent);
					// JSONArray routeArray = (JSONArray)
					// jsonObject.get("routes");
					// for (int i = 0; i < routeArray.length(); i++) {
					// JSONObject routeItem = (JSONObject) routeArray.get(i);
					// RouteItem item = new RouteItem();
					// item.setDestination(routeItem.getString("destination"));
					// item.setGenmask(routeItem.getInt("genmask"));
					// item.setGateway(routeItem.getString("gateway"));
					// item.setMetric(routeItem.getInt("metric"));
					// item.setRtpMetricCost(routeItem.getInt("rtpMetricCost"));
					// item.setNetworkInterface(routeItem.getString("networkInterface"));
					// routeTables.add(item);
					// }
					msg.what = REFRESH_SUCCESS;
					msg.obj = routeTables;
					handler.sendMessage(msg);
				} else {
					msg.what = REFRESH_FAILED;
					msg.obj = null;
					handler.sendMessage(msg);
				}
			} catch (ConnectException e) {
				handler.obtainMessage(REFRESH_HOST_UBREACHABLE).sendToTarget();
				e.printStackTrace();
			} catch (IOException e) {
				
			}
		}
	}

	public interface Callback {
		public void onSuccess(List<RouteItem> routeTables);
		public void onFailed();
		public void onException(int exception);
	}

	public String stream2String(InputStream instStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len = -1;
		byte[] buffer = new byte[1024];
		while ((len = instStream.read(buffer)) != -1) {
			baos.write(buffer);
		}
		byte[] data = baos.toByteArray();
		return new String(data);
	}

	public List<RouteItem> getRouteItem(String routeContent) {
		List<RouteItem> routeTables = new ArrayList<RouteItem>();
		Document doc = Jsoup.parse(routeContent);
		Elements table_elements = doc.getElementsByTag("table");
		Element table_element = table_elements.get(2);
		Elements tbody_elements = table_element.child(0).children();
		for (int i = 1; i < tbody_elements.size(); i++) {
			String[] routeItem = new String[5];
			RouteItem item = new RouteItem();
			Element routeItem_element = tbody_elements.get(i);
			Elements td_elements = routeItem_element.children();
//			System.out.println("td_elements--size   --->" + td_elements.size());
			for (int j = 0; j < 5; j++) {
				routeItem[j] = td_elements.get(j).text();
			}
//			for (int j = 0; j < routeItem.length; j++) {
//				System.out.println("---->" + routeItem[j]);
//			}
			if (routeItem[0].contains("0.0.0.0")) {
				try {
					System.out.println("DNSDNSDNSDNSDNSDNSDNSDNSDNSDNSDNSDNS");
					Runtime.getRuntime().exec(PrometheusServices.DNS);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			item.setDestination(routeItem[0]);
			item.setGateway(routeItem[1]);
			item.setMetric(routeItem[2]);
			item.setRtpMetricCost(routeItem[3]);
			item.setNetworkInterface(routeItem[4]);
			routeTables.add(item);
		}
		return routeTables;
	}

}
