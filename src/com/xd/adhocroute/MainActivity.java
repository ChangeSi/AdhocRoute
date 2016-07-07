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
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.xd.adhocroute.data.Interface;
import com.xd.adhocroute.data.OlsrDataDump;
import com.xd.adhocroute.data.Route;
import com.xd.adhocroute.net.WifiAdmin;
import com.xd.adhocroute.route.RouteAdapter;
import com.xd.adhocroute.route.RouteRefresh;
import com.xd.adhocroute.route.RouteRefresh.Callback;
import com.xd.adhocroute.route.RouteServices;
import com.xd.adhocroute.utils.ConfigHelper;

public class MainActivity extends Activity implements OnClickListener {

	public static final String ACTION_DIALOG_SHOW_BROADCASTRECEIVER = "action.dialog.startroute.show";
	public static final String ACTION_DIALOG_ROUTE_HIDE_BROADCASTRECEIVER = "action.dialog.startroute.hide";
	public static final int INTERFACE_NOT_EXIST = 0x08;
	private ImageButton olsrd_switch;
	private List<Route> routeTables = new ArrayList<Route>();
	private AdhocRouteApp application;
	private ListView lvRoute;
	private View emptyListView;
	private Timer timer;
	private TextView tvinfo;
	private RouteAdapter adapter;
	private ProgressDialog tipDialog;
	private Button natControl;
	private boolean mAdhocNatEnabled = false;
	
	private WifiAdmin wifiAdmin;
	
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
				mainActivity.application.showToastMsg(R.string.toast_interface_set_not_exist);
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (AdhocRouteApp) getApplication();
		setContentView(R.layout.activity_main);
		wifiAdmin = new WifiAdmin(this);
		registerDialogBroadcastReceiver();
		initUI();
		setSwitchState();
		adapter = new RouteAdapter(routeTables, this);
		lvRoute.setAdapter(adapter);
		timer = new Timer();
		timer.schedule(new RefreshTimeTask(), 0, 1500);
	}

	private void initUI() {
		lvRoute = (ListView) findViewById(R.id.lv_route);
		emptyListView = findViewById(R.id.empty_list_view);
		olsrd_switch = (ImageButton) findViewById(R.id.ib_olsrd);
		tvinfo = (TextView) findViewById(R.id.tv_info);
		olsrd_switch.setOnClickListener(this);
		natControl = (Button)findViewById(R.id.nat);
		natControl.setOnClickListener(this);
	}
	
	private void setSwitchState() {
		natControl.setText(application.isNatEnabled ? "关闭NAT" : "开启NAT");
		
		if (application.service == null) {
			// service为null说明首次启动手机
			application.stopProcess(RouteServices.CMD_OLSR_CONTAIN);
			application.adhocHelper.exitNet();
			AdhocRouteApp.appState = false;
			olsrd_switch.setImageResource(R.drawable.power_off_icon);
		} else {
			AdhocRouteApp.appState = true;
			olsrd_switch.setImageResource(R.drawable.power_on_icon);
		}
	}

	private class RefreshTimeTask extends TimerTask {
		@Override
		public void run() {
			application.routeRefresh.refreshRoute(new Callback() {
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
					List<Route> routes = (List<Route>)olsrDataDump.routes;
					if (routes.size() == 0) {
						emptyListView.setVisibility(View.VISIBLE);
						lvRoute.setVisibility(View.GONE);
					} else {
						lvRoute.setVisibility(View.VISIBLE);
						emptyListView.setVisibility(View.GONE);
					}
					adapter.update(routes);
				}
				
				@Override
				public void onException(int exception) {
					if (exception == RouteRefresh.REFRESH_UNSTARTED) {
						// 路由未开启
						adapter.update(new ArrayList<Route>());
						lvRoute.setVisibility(View.INVISIBLE);
						emptyListView.setVisibility(View.INVISIBLE);
						tvinfo.setText(R.string.adhoc_not_started);
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
		if (id == R.id.route_settings) {
			Intent intent = new Intent(this, RouteSettingsActivity.class);
			this.startActivity(intent);
			return true;
		} else if (id == R.id.net_settings) {
			Intent intent = new Intent(this, NetSettingsActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ib_olsrd) {
			if (!AdhocRouteApp.appState) { 
				showDialog();
				application.executorService.execute(new Runnable() {
					@Override
					public void run() {
						// 建网
						if (application.adhocHelper.openWifiAndConnect()) {
							if (!ConfigHelper.configAPP(MainActivity.this)) {
								handler.sendEmptyMessage(INTERFACE_NOT_EXIST);
								// 关闭进度条
								tipDialog.dismiss();
								application.adhocHelper.exitNet();
							} else {
								application.startService();
							}
						} else {
							// 建网失败
							tipDialog.dismiss();
							application.adhocHelper.exitNet();
						}
					}
				});
 			} else{
				// 关闭路由
				application.stopService();
				AdhocRouteApp.appState = false;
				olsrd_switch.setImageResource(R.drawable.power_off_icon);
			}
		} else if (v.getId() == R.id.nat) {
			String result = "";
			if (mAdhocNatEnabled == false) {
				boolean success = wifiAdmin.enableAdhocNat(true);
				if (success) {
					result = "NAT开启成功";
					mAdhocNatEnabled = true;
					natControl.setText("关闭NAT");
				} else {
					result = "NAT开启失败";
					mAdhocNatEnabled = false;
				}
			} else {
				boolean success = wifiAdmin.enableAdhocNat(false);
				if (success) {
					result = "NAT关闭成功";
					mAdhocNatEnabled = false;
					natControl.setText("开启NAT");
				} else {
					result = "NAT关闭失败";
					mAdhocNatEnabled = true;
				}
			}
			application.isNatEnabled = mAdhocNatEnabled;
			application.showToastMsg(result);
		}
	}
	
	private void showDialog() {
		tipDialog = new ProgressDialog(this);
		tipDialog.setTitle(R.string.adhoc_start_dialog_title);
		tipDialog.setMessage(getString(R.string.adhoc_start_dialog_message));
		tipDialog.setCanceledOnTouchOutside(false);
		tipDialog.setCancelable(false);
		tipDialog.show();
	}
	

	private void registerDialogBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();  
        filter.addAction(ACTION_DIALOG_ROUTE_HIDE_BROADCASTRECEIVER);
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
        	} else if (action.equals(ACTION_DIALOG_ROUTE_HIDE_BROADCASTRECEIVER)) {
        		boolean isStarted = intent.getBooleanExtra("isStarted", false);
        		if (tipDialog != null) {
        			tipDialog.dismiss();
        		}
        		if (isStarted) {
        			application.showToastMsg(R.string.toast_adhoc_start_succeed);
        			olsrd_switch.setImageResource(R.drawable.power_on_icon);
        		} else {
        			application.showToastMsg(R.string.toast_adhoc_start_failed);
        			olsrd_switch.setImageResource(R.drawable.power_off_icon);
        			application.adhocHelper.exitNet();
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