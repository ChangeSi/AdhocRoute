package com.xd.adhocroute.data;

public class RouteItem2 {

	private String destination;
	private int genmask;
	private String gateway;
	private int metric;
	private int rtpMetricCost;
	
	private String networkInterface;
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public int getGenmask() {
		return genmask;
	}
	public void setGenmask(int genmask) {
		this.genmask = genmask;
	}
	public String getGateway() {
		return gateway;
	}
	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	public int getMetric() {
		return metric;
	}
	public void setMetric(int metric) {
		this.metric = metric;
	}
	public int getRtpMetricCost() {
		return rtpMetricCost;
	}
	public void setRtpMetricCost(int rtpMetricCost) {
		this.rtpMetricCost = rtpMetricCost;
	}
	public String getNetworkInterface() {
		return networkInterface;
	}
	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}
	
}
