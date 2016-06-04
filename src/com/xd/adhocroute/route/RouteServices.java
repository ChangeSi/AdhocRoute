package com.xd.adhocroute.route;

import java.io.File;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.test.UiThreadTest;

import com.xd.adhocroute.AdhocRouteApp;
import com.xd.adhocroute.MainActivity;
import com.xd.adhocroute.R;

public class RouteServices extends Service {
	public static final String CMD_OLSR = "app_bin/olsrd";
	private static final int ID_FORGROUND = 2;
	private String olsrdPath;
	private String olsrdConfPath;
	private String OLSR_START_CMD = "";
	
	private AdhocRouteApp app = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = (AdhocRouteApp)getApplication();
//		runForground();
		olsrdPath = new File(getDir("bin", Context.MODE_PRIVATE), "olsrd").getAbsolutePath();
		olsrdConfPath = new File(getFilesDir(), "olsrd.conf").getAbsolutePath();
		OLSR_START_CMD = olsrdPath + " -f " +  olsrdConfPath; /* + " -i " + inface*/
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		app.serviceStarted(this);
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@SuppressWarnings("unused")
	private void runForground(){
		// 创建一个启动其他Activity的Intent  
        Intent intent = new Intent(this  
            , MainActivity.class);  
        PendingIntent pi = PendingIntent.getActivity(  
        		this, 0, intent, 0);  
        Notification notification = new Notification.Builder(this)  
            // 设置显示在状态栏的通知提示信息  
            .setTicker("路由程序正在运行")
            // 设置通知的图标  
            .setSmallIcon(R.drawable.ic_launcher)
            // 设置通知内容的标题  
            .setContentTitle("Adhoc路由")  
            // 设置通知内容  
            .setContentText("路由程序正在运行...")  
            .setWhen(System.currentTimeMillis())  
            // 设改通知将要启动程序的Intent  
            .setContentIntent(pi)
            .getNotification();
        startForeground(ID_FORGROUND, notification);
	}
	
	@Override
	public void onDestroy() {
		app.serviceDestroy();
		super.onDestroy();
	}
	
	public void startOLSR() {
		app.startProcess(OLSR_START_CMD);
	}
	
	public void stopOLSR() {
		app.stopProcess(CMD_OLSR);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}