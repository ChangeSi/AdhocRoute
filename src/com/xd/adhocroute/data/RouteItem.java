package com.xd.adhocroute.data;

public class RouteItem {

	private String destination;
	private String genmask;
	private String gateway;
	private String metric;
	private String rtpMetricCost;
	
	private String networkInterface;

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getGenmask() {
		return genmask;
	}

	public void setGenmask(String genmask) {
		this.genmask = genmask;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getRtpMetricCost() {
		return rtpMetricCost;
	}

	public void setRtpMetricCost(String rtpMetricCost) {
		this.rtpMetricCost = rtpMetricCost;
	}

	public String getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}
	
}
