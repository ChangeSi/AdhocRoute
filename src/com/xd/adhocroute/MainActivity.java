package com.xd.adhocroute;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.xd.adhocroute.data.Route;
import com.xd.adhocroute.log.Lg;
import com.xd.adhocroute.route.AdhocRun;
import com.xd.adhocroute.route.RouteAdapter;
import com.xd.adhocroute.route.RouteRefresh;
import com.xd.adhocroute.route.RouteRefresh.Callback;
import com.xd.adhocroute.utils.IPUtils;
import com.xd.adhocroute.utils.NativeHelper;
import com.xd.adhocroute.R;
public class MainActivity extends Activity implements OnClickListener {
	final static int MSG_OUTPUT = 1;
	final static int MSG_ERROR = 2;
	final static int MSG_PID = 3;

	private boolean routeRunning;
	private ImageButton olsrd_switch;
	private TextView tv_deviceip;
	private AdhocRun adhocRun;
	private List<Route> routeTables = new ArrayList<Route>();
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
		routeRefresh = new RouteRefresh();
		initUI();
		setSwitchState();
		adapter = new RouteAdapter(routeTables, this);
		lvRoute.setAdapter(adapter);
		timer = new Timer();
	}

	private void initUI() {
		lvRoute = (ListView) findViewById(R.id.lv_route);
		olsrd_switch = (ImageButton) findViewById(R.id.ib_olsrd);
		tv_deviceip = (TextView) findViewById(R.id.tv_deviceip);
		tvTips = (TextView) findViewById(R.id.tv_tips);
		olsrd_switch.setOnClickListener(this);
		tv_deviceip.setText(generateTips(routeRunning));
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
		@Override
		public void run() {
			routeRefresh.refreshRoute(new Callback() {
				@Override
				public void onSuccess(List<Route> routeTables) {
					if (routeTables.size() == 0) {
						tvTips.setText("路由正在初始化");
					} else {
						tvTips.setText("");
					}
					adapter.update(routeTables);
				}

				@Override
				public void onException(int exception) {
					if (exception == RouteRefresh.REFRESH_HOST_UBREACHABLE) {
						// 路由未开启
						adapter.update(new ArrayList<Route>());
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

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ib_olsrd) {
			if (!routeRunning) { 
				// 开启路由 
				// 1.创建Ad-Hoc网络 
				adhocRun.constructAdhoc(); 
				// 2.修改节点IP显示
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						tv_deviceip.setText(generateTips(routeRunning));
					}
				}, 1000);

				// 3.将asset里面的文件拷贝到对应位置
				// 3.1 在app_bin里面创建文件 
				NativeHelper.setup(this);
				// 3.2 将asset里面的文件解压到app_bin目录下
				NativeHelper.unzipAssets(this);
				// 3.3 更新配置文件，asset里面的配置文件和设置的参数共同决定，更新到package/files/olsrd.conf
				NativeHelper.updateConfig(this);
				
				routeRunning = true; 
				
				// 3.使用命令执行路由
				app.startService();
				// 4.修改按钮状态
				olsrd_switch.setImageResource(R.drawable.power_on_icon);
				
				timer.schedule(new RefreshTimeTask(), 1000, 3000);
				Lg.d("adhocroute-test 执行完了startservice");
			} else {
				// 关闭路由 
				app.stopService(); 
				routeRunning = false;
				olsrd_switch.setImageResource(R.drawable.power_off_icon);
				tv_deviceip.setText(generateTips(routeRunning));
			}
		}
	}

	@Override
	protected void onDestroy() {
		timer.cancel();
		super.onDestroy();
	}

	private String generateTips(boolean routeRunning) {
		String result = "";
		if (routeRunning) {
			String localIP = IPUtils.getAdhocIpString();
			if (localIP != null) {
				result = "节点" + localIP + "的路由表";
			} else {
				result = "程序异常";
			}
		} else {
			result = "未开启Ad-hoc路由";
		}
		return result;
	}

	public class InfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

		}
	}
}


/*
	@ Deprecated
	private boolean startProcess() {
		// Process process = Runtime.getRuntime().exec(new
		// String[]{"/system/xbin/su","-c",
		// "/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0"});
		// Process process =
		// Runtime.getRuntime().exec("/system/xbin/su /data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0");
		// RouteUtils.execCmd("/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0");

		// try {
		// RouteUtils.exec("/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 1 -i wlan0");
		// } catch (IOException e) {
		// e.printStackTrace();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		// ShellUtils.execCommand(new String[]{"su 1"}, true);
		// ShellUtils.execCommand(new String[]{"",
		// "/data/data/com.xd.adhocroute/app_bin/olsrd -f /data/data/com.xd.adhocroute/files/olsrd.conf -d 0 -i wlan0"},
		// true);

		// TODO:
		// 存在问题，Android手机上可以执行
		// 红米2A上有问题：执行不了root权限的命令
		try {
			Process process = Runtime.getRuntime().exec(
					"su mkdir /data/data/test");
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

 */
