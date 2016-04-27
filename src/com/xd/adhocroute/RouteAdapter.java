package com.xd.adhocroute;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.xd.adhocroute.data.RouteItem;

public class RouteAdapter extends BaseAdapter implements ListAdapter {

	public Context context;
	private List<RouteItem> routeTables;
	public RouteAdapter(List<RouteItem> routeTables, Context context) {
		this.context = context;
		this.routeTables = routeTables;
	}
	public void update(List<RouteItem> routeTables){
		this.routeTables = routeTables;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		return routeTables.size();
	}

	@Override
	public Object getItem(int position) {
		return routeTables.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_route, null);
			holder = new ViewHolder();
			holder.tvDest = (TextView) convertView.findViewById(R.id.tv_dest);
			holder.tvmask = (TextView) convertView.findViewById(R.id.tv_mask);
			holder.tvGateway = (TextView) convertView.findViewById(R.id.tv_gateway);
			holder.tvMetric = (TextView) convertView.findViewById(R.id.tv_metric);
			holder.tvETX = (TextView) convertView.findViewById(R.id.tv_etx);
			holder.tvInterface = (TextView) convertView.findViewById(R.id.tv_interface);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvDest.setText(routeTables.get(position).getDestination());
		holder.tvmask.setText(routeTables.get(position).getGenmask() + "");
		holder.tvGateway.setText(routeTables.get(position).getGateway());
		holder.tvMetric.setText(routeTables.get(position).getMetric() + "");
		holder.tvETX.setText(routeTables.get(position).getRtpMetricCost() + "");
		holder.tvInterface.setText(routeTables.get(position).getNetworkInterface());
		return convertView;
	}

	static class ViewHolder {
		public TextView tvDest;
		public TextView tvmask;
		public TextView tvGateway;
		public TextView tvMetric;
		public TextView tvETX;
		public TextView tvInterface;
	}
}
