package com.xd.adhocroute;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xd.adhocroute.data.Interface;
import com.xd.adhocroute.data.OlsrDataDump;
import com.xd.adhocroute.data.Route;
import com.xd.adhocroute.route.RouteAdapter;
import com.xd.adhocroute.route.RouteRefresh;
import com.xd.adhocroute.route.RouteRefresh.Callback;
import com.xd.adhocroute.route.RouteServices;
import com.xd.adhocroute.route.RouteServices.KillOlsrRunnable;
import com.xd.adhocroute.utils.IPUtils;
import com.xd.adhocroute.utils.NativeHelper;
public class MainActivity extends Activity implements OnClickListener {

	public static final String ACTION_DIALOG_SHOW_BROADCASTRECEIVER = "action.dialog.show";
	public static final String ACTION_DIALOG_HIDE_BROADCASTRECEIVER = "action.dialog.hide";
	private boolean routeRunning;
	private ImageButton olsrd_switch;
//	private AdhocRun adhocRun;
	private List<Route> routeTables = new ArrayList<Route>();
	private AdhocRouteApp app;
	private ListView lvRoute;
	private Timer timer;
	private RouteRefresh routeRefresh;
	private TextView tvinfo;
	private RouteAdapter adapter;
	private ProgressDialog tipDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (AdhocRouteApp) getApplication();
		setContentView(R.layout.activity_main);
//		adhocRun = new AdhocRun(this);
//		adhocRun.startScanThread();
		routeRefresh = new RouteRefresh();
		registerDialogBroadcastReceiver();
		initUI();
		setSwitchState();
		adapter = new RouteAdapter(routeTables, this);
		lvRoute.setAdapter(adapter);
		timer = new Timer();
		timer.schedule(new RefreshTimeTask(), 0, 1000);
	}

	private void initUI() {
		lvRoute = (ListView) findViewById(R.id.lv_route);
		olsrd_switch = (ImageButton) findViewById(R.id.ib_olsrd);
//		tvTips = (TextView) findViewById(R.id.tv_tips);
		tvinfo = (TextView) findViewById(R.id.tv_info);
		olsrd_switch.setOnClickListener(this);
	}

	private void setSwitchState() {
		
		if (app.service == null) {
			// service为null说明首次启动手机
			((AdhocRouteApp)getApplication()).getGlobalThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					stopProcess();
				}
			});
			
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
				public void onSuccess(OlsrDataDump olsrDataDump) {
					List<Interface> interfaces = (List<Interface>)olsrDataDump.interfaces;
					if (interfaces != null && interfaces.size() != 0) {
						Interface inface = interfaces.get(0);
						tvinfo.setText("IP：" + inface.ipv4Address + "\n"
									 + "网卡：" + inface.name + "\n"
									 + "mac地址：" + inface.macAddress
										);
					}
					adapter.update((List<Route>)olsrDataDump.routes);
				}

				@Override
				public void onException(int exception) {
					if (exception == RouteRefresh.REFRESH_UNSTARTED) {
						// 路由未开启
						adapter.update(new ArrayList<Route>());
						tvinfo.setText("路由未开启");
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
				//adhocRun.constructAdhoc(); 
				// 2.修改节点IP显示

				tipDialog = new ProgressDialog(this);
				tipDialog.setTitle("启动路由");
				tipDialog.setMessage("路由正在启动中");
				tipDialog.show();
				
				((AdhocRouteApp)getApplication()).getGlobalThreadPool().execute(new Runnable() {
					
					@Override
					public void run() {
						// 3.将asset里面的文件拷贝到对应位置
						// 3.1 在app_bin里面创建文件 
						NativeHelper.setup(MainActivity.this);
						// 3.2 将asset里面的文件解压到app_bin目录下
						NativeHelper.unzipAssets(MainActivity.this);
						// 3.3 更新配置文件，asset里面的配置文件和设置的参数共同决定，更新到package/files/olsrd.conf
						NativeHelper.updateConfig(MainActivity.this);
						// 3.使用命令执行路由
						app.startService();
						// 4.修改按钮状态
					}
				});
				
			} else {
				// 关闭路由 
				app.stopService();
				routeRunning = false;
				olsrd_switch.setImageResource(R.drawable.power_off_icon);
			}
		}
	}

	private void registerDialogBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();  
        filter.addAction(ACTION_DIALOG_HIDE_BROADCASTRECEIVER);  
        registerReceiver(dialogShowHideReceiver, filter);  
	}
	
	private void unregisterDialogBroadcastReceiver() {
        unregisterReceiver(dialogShowHideReceiver);
	}
	
	private BroadcastReceiver dialogShowHideReceiver = new BroadcastReceiver(){  
		  
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        	if (action.equals(ACTION_DIALOG_SHOW_BROADCASTRECEIVER)){
        		// Omitted
        	} else if (action.equals(ACTION_DIALOG_HIDE_BROADCASTRECEIVER)) {
        		boolean isStarted = intent.getBooleanExtra("isStarted", false);
        		if (tipDialog != null) {
        			tipDialog.dismiss();
        		}
        		if (isStarted) {
        			Toast.makeText(MainActivity.this, "路由启动成功", Toast.LENGTH_LONG).show();
        			routeRunning = true; 
        			olsrd_switch.setImageResource(R.drawable.power_on_icon);
        		} else {
        			Toast.makeText(MainActivity.this, "路由启动异常，请重新启动", Toast.LENGTH_LONG).show();
        			routeRunning = false;
        			olsrd_switch.setImageResource(R.drawable.power_off_icon);
        		}
        	}
        }
    };

	@Override
	protected void onDestroy() {
		timer.cancel();
		unregisterDialogBroadcastReceiver();
		super.onDestroy();
	}

	private void stopProcess() {
		try {
			Process process = Runtime.getRuntime().exec("su");
			OutputStream os = process.getOutputStream();
			os.write(RouteServices.PS.getBytes());
			os.close();
			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
			String line = br.readLine();
            while(line != null){
                if (line.contains(RouteServices.CMD_OLSR)) {
					String pidStr = line.split("\\s+")[1];
					int pid = Integer.valueOf(pidStr);
					((AdhocRouteApp)getApplication()).getGlobalThreadPool().execute(new KillOlsrRunnable(pid));
				}
                line = br.readLine();
            }
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}