package com.xd.adhocroute;

import java.lang.ref.WeakReference;
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
import com.xd.adhocroute.utils.ConfigHelper;

public class MainActivity extends Activity implements OnClickListener {

	public static final String ACTION_DIALOG_SHOW_BROADCASTRECEIVER = "action.dialog.startroute.show";
	public static final String ACTION_DIALOG_HIDE_BROADCASTRECEIVER = "action.dialog.startroute.hide";
	public static final int INTERFACE_NOT_EXIST = 0x01;
	public static boolean routeRunning;
	private ImageButton olsrd_switch;
//	private AdhocRun adhocRun;
	private List<Route> routeTables = new ArrayList<Route>();
	private AdhocRouteApp app;
	private ListView lvRoute;
	private Timer timer;
	private TextView tvinfo;
	private RouteAdapter adapter;
	private ProgressDialog tipDialog;
	
	private Handler handler = new UIHandler(this);
	
	private static class UIHandler extends Handler{
		WeakReference<MainActivity> outerReference;
		
		public UIHandler(MainActivity outer) {
			outerReference = new WeakReference<MainActivity>(outer);;
		}
		public void handleMessage(android.os.Message msg) {
			MainActivity mainActivity = outerReference.get();
			if (mainActivity == null) return;
			switch (msg.what) {
			case INTERFACE_NOT_EXIST:
				Toast.makeText(mainActivity, "指定的网卡不存在", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (AdhocRouteApp) getApplication();
		setContentView(R.layout.activity_main);
//		adhocRun = new AdhocRun(this);
//		adhocRun.startScanThread();
		registerDialogBroadcastReceiver();
		initUI();
		setSwitchState();
		adapter = new RouteAdapter(routeTables, this);
		lvRoute.setAdapter(adapter);
		timer = new Timer();
		timer.schedule(new RefreshTimeTask(), 0, 2000);
	}

	private void initUI() {
		lvRoute = (ListView) findViewById(R.id.lv_route);
		olsrd_switch = (ImageButton) findViewById(R.id.ib_olsrd);
		tvinfo = (TextView) findViewById(R.id.tv_info);
		olsrd_switch.setOnClickListener(this);
	}
	
	private void setSwitchState() {
		if (app.service == null) {
			// service为null说明首次启动手机
			app.stopProcess(RouteServices.CMD_OLSR);
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
			app.routeRefresh.refreshRoute(new Callback() {
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
				showDialog();
				app.getGlobalThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						if (!ConfigHelper.configAPP(MainActivity.this)) {
							handler.sendEmptyMessage(INTERFACE_NOT_EXIST);
							// 关闭进度条
							tipDialog.dismiss();
							routeRunning = false;
						} else {
							app.startService();
						}
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

	private void showDialog() {
		tipDialog = new ProgressDialog(this);
		tipDialog.setTitle("启动路由");
		tipDialog.setMessage("路由正在启动中");
		tipDialog.setCanceledOnTouchOutside(false);
		tipDialog.setCancelable(false);
		tipDialog.show();
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

}