package com.xd.adhocroute;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xd.adhocroute.data.RouteItem;
import com.xd.adhocroute.utils.AdhocRun;
import com.xd.adhocroute.utils.IPUtils;
import com.xd.adhocroute.utils.RouteRefresh;
import com.xd.adhocroute.utils.ShellUtils;
import com.xd.adhocroute.utils.RouteRefresh.Callback;
import com.xd.adhocroute.utils.RouteUtils;

public class MainActivity extends Activity implements OnClickListener {
	private Thread[] threads = new Thread[2];
	final static int MSG_OUTPUT = 1;
	final static int MSG_ERROR = 2;
	final static int MSG_PID = 3;

	private boolean routeRunning;
	private ImageButton olsrd_switch;
	private TextView tv_deviceip;
	private AdhocRun adhocRun;
	private List<RouteItem> routeTables = new ArrayList<RouteItem>();
	private Handler handler;
	private AdhocRouteApp app;
	private ListView lvRoute;
	private Timer timer;
	private RouteRefresh routeRefresh;
	private TextView tvTips;
	private RouteAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (AdhocRouteApp) getApplication();
		setContentView(R.layout.activity_main);
		handler = new InfoHandler();
		adhocRun = new AdhocRun(this);
		adhocRun.startScanThread();
		lvRoute = (ListView) findViewById(R.id.lv_route);
		olsrd_switch = (ImageButton) findViewById(R.id.ib_olsrd);
		tv_deviceip = (TextView) findViewById(R.id.tv_deviceip);
		tvTips = (TextView) findViewById(R.id.tv_tips);
		olsrd_switch.setOnClickListener(this);
		String localIP = IPUtils.getAdhocIpString();
		if (localIP != null) {
			tv_deviceip.setText(Html.fromHtml(generateDeviceName(localIP)));
		}
		setSwitchState();
		routeRefresh = new RouteRefresh();
		adapter = new RouteAdapter(routeTables, this);
		lvRoute.setAdapter(adapter);
		timer = new Timer();
		timer.schedule(new RefreshTimeTask("http://127.0.0.1:8888/routes"),
				1000, 3000);
	}

	private void setSwitchState() {
		if (app.service == null) {
			routeRunning = false;
			olsrd_switch.setImageResource(R.drawable.power_off_icon);
		} else {
			routeRunning = true;
			olsrd_switch.setImageResource(R.drawable.power_on_icon);
		}
	}

	private class RefreshTimeTask extends TimerTask {

		private String url;

		public RefreshTimeTask(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			routeRefresh.refreshRoute(url, new Callback() {
				@Override
				public void onSuccess(List<RouteItem> routeTables) {
					if (routeTables.size() == 0) {
						tvTips.setText("路由正在初始化");
					} else {
						tvTips.setText("");
					}
					adapter.update(routeTables);
				}

				@Override
				public void onFailed() {
					Toast.makeText(MainActivity.this, "刷新出错", 0).show();
				}

				@Override
				public void onException(int exception) {
					if (exception == RouteRefresh.REFRESH_HOST_UBREACHABLE) {
						// 路由未开启
						adapter.update(new ArrayList<RouteItem>());
						tvTips.setText("路由未开启");
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean startProcess() {
		
		// Process process = Runtime.getRuntime().exec(new
		// String[]{"/system/xbin/su","-c",
		// "/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0"});
		// Process process =
		// Runtime.getRuntime().exec("/system/xbin/su /data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0");
		//RouteUtils.execCmd("/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0");

//		try {
//			RouteUtils.exec("/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 1 -i wlan0");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		
//		ShellUtils.execCommand(new String[]{"su 1"}, true);
//		ShellUtils.execCommand(new String[]{"", "/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0"}, true);
		
		// TODO:
		// 存在问题，Android手机上可以执行
		// 红米2A上有问题：执行不了root权限的命令
		try {
			Process process = Runtime.getRuntime().exec("su mkdir /data/data/test");
			 threads[0] = new Thread(new OutputMonitor(MSG_OUTPUT,
			 process.getInputStream()));
			 threads[1] = new Thread(new OutputMonitor(MSG_ERROR,
			 process.getErrorStream()));
			 threads[0].start();
			 threads[1].start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private String exec(String arg0, String arg1, String arg2) {
		try {
			// android.os.Exec is not included in android.jar so we need to use
			// reflection.
			Class execClass = Class.forName("android.os.Exec");
			Method createSubprocess = execClass.getMethod("createSubprocess",
					String.class, String.class, String.class, int[].class);
			Method waitFor = execClass.getMethod("waitFor", int.class);

			// Executes the command.
			// NOTE: createSubprocess() is asynchronous.
			int[] pid = new int[1];
			FileDescriptor fd = (FileDescriptor) createSubprocess.invoke(null,
					arg0, arg1, arg2, pid);

			// Reads stdout.
			// NOTE: You can write to stdin of the command using new
			// FileOutputStream(fd).
			FileInputStream in = new FileInputStream(fd);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String output = "";
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					output += line + "\n";
				}
			} catch (IOException e) {
				// It seems IOException is thrown when it reaches EOF.
			}

			// Waits for the command to finish.
			waitFor.invoke(null, pid[0]);

			return output;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		} catch (SecurityException e) {
			throw new RuntimeException(e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private class OutputMonitor implements Runnable {
		private final java.io.BufferedReader br;
		private final int msg;

		public OutputMonitor(int t, java.io.InputStream is) {
			br = new java.io.BufferedReader(new java.io.InputStreamReader(is),
					8192);
			msg = t;
		}

		public void run() {
			try {
				String line = br.readLine();
				while (line != null) {
					Log.e("##########", line);
					// if (line.contains(olsrdPath)) {
					// String pidStr = line.split("\\s+")[1];
					// int pid = Integer.valueOf(pidStr);
					// handler.obtainMessage(MSG_PID, pid).sendToTarget();
					// }
					line = br.readLine();
					// 处理进程Process的消息
					// handler.obtainMessage(msg, line).sendToTarget(); // NOTE:
					// the last null is also sent!
				}
			} catch (Exception e) {
				// 处理进程Process的消息
				Log.i(AdhocRouteApp.TAG,
						"shell start olsr failed: " + e.toString());
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ib_olsrd) {
			// 测试命令

			startProcess();

			/*
			 * if (!routeRunning) { // 开启路由 // 1.根据程序运行状态改变按钮状态
			 * 
			 * // 2.创建Ad-Hoc网络 adhocRun.constructAdhoc(); // 3.修改节点IP显示
			 * handler.postDelayed(new Runnable() {
			 * 
			 * @Override public void run() { String localIP =
			 * IPUtils.getAdhocIpString();
			 * tv_deviceip.setText(Html.fromHtml(generateDeviceName(localIP)));
			 * } }, 1000);
			 * 
			 * // 在app_bin里面创建文件 NativeHelper.setup(this); //
			 * 将asset里面的文件解压到app_bin目录下 NativeHelper.unzipAssets(this); //
			 * 更新配置文件，asset里面的配置文件和设置的参数共同决定，更新到package/files/olsrd.conf
			 * NativeHelper.updateConfig(this); routeRunning = true; // 使用命令执行
			 * app.startService();
			 * 
			 * // 修改按钮状态
			 * olsrd_switch.setImageResource(R.drawable.power_on_icon); } else {
			 * // 关闭路由 app.stopService(); routeRunning = false;
			 * olsrd_switch.setImageResource(R.drawable.power_off_icon); }
			 */
		}

	}

	@Override
	protected void onDestroy() {
		timer.cancel();
		super.onDestroy();
	}

	private String generateDeviceName(String dest) {
		return "<html>节点<em>" + dest + "</em>的路由表</html>";
	}

	public class InfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

		}
	}
}
