package com.xd.adhocroute;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xd.adhocroute.utils.AdhocRun;
import com.xd.adhocroute.utils.IPUtils;
import com.xd.adhocroute.utils.NativeHelper;

public class MainActivity extends Activity implements OnClickListener {

	private ImageButton olsrd_switch;
	private TextView tv_deviceip;
	private AdhocRun adhocRun;

	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		adhocRun = new AdhocRun(this);
		adhocRun.startScanThread();
		olsrd_switch = (ImageButton) findViewById(R.id.ib_olsrd);
		tv_deviceip = (TextView) findViewById(R.id.tv_deviceip);
		olsrd_switch.setOnClickListener(this);
		String localIP = IPUtils.getAdhocIpString();
		if (localIP != null) {
			tv_deviceip.setText(Html.fromHtml(generateDeviceName(localIP)));
		}
		handler = new InfoHandler();
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
			// 1.根据程序运行状态改变按钮状态
			
			// 2.创建Ad-Hoc网络
			adhocRun.constructAdhoc();
			// 3.修改节点IP显示
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					String localIP = IPUtils.getAdhocIpString();
					tv_deviceip.setText(Html.fromHtml(generateDeviceName(localIP)));
				}
			}, 1000);
			
			NativeHelper.setup(this);
			NativeHelper.unzipAssets(this);
			NativeHelper.updateConfig(this);
			
			
			
			
		}
		
	}


	private String generateDeviceName(String dest){
		return "<html>节点<em>" + dest + "</em>的路由表</html>";
	}
	
	
	public class InfoHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			
		}
	}
}
