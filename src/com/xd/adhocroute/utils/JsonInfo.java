package com.xd.adhocroute.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.xd.adhocroute.data.Gateway;
import com.xd.adhocroute.data.HNA;
import com.xd.adhocroute.data.Interface;
import com.xd.adhocroute.data.Link;
import com.xd.adhocroute.data.MID;
import com.xd.adhocroute.data.Neighbor;
import com.xd.adhocroute.data.Node;
import com.xd.adhocroute.data.OlsrDataDump;
import com.xd.adhocroute.data.Plugin;
import com.xd.adhocroute.data.Route;

public class JsonInfo {

	private String lastCommand = "";

	String host = "127.0.0.1";
	int port = 8118;

	ObjectMapper mapper = null;

	final Set<String> supportedCommands = new HashSet<String>(
			Arrays.asList(new String[] {
					// combined reports
					"all", // all of the JSON info
					"runtime", // all of the runtime status reports
					"startup", // all of the startup config reports
					// individual runtime reports
					"gateways", // gateways
					"hna", // Host and Network Association
					"interfaces", // network interfaces
					"links", // links
					"mid", // MID
					"neighbors", // neighbors
					"routes", // routes
					"topology", // mesh network topology
					"runtime", // all the runtime info in a single report
					// the rest don't change at runtime, so they're separate
					"config", // the current running config info
					"plugins", // loaded plugins and their config
					// only non-JSON output, can't be combined with others
					"olsrd.conf", // current config in olsrd.conf format
			}));

	public JsonInfo() {
	}

	public JsonInfo(String sethost) {
		host = sethost;
	}

	public JsonInfo(String sethost, int setport) {
		host = sethost;
		port = setport;
	}

	private boolean isCommandStringValid(String cmdString) {
		boolean isValid = true;
		if (!cmdString.equals(lastCommand)) {
			lastCommand = cmdString;
			for (String s : cmdString.split("/")) {
				if ( !s.equals("") && !supportedCommands.contains(s)) {
					System.out.println("Unsupported command: " + s);
					isValid = false;
				}
			}
		}
		return isValid;
	}
	
	String[] request(String req) throws IOException {
		Socket sock = null;
		BufferedReader in = null;
		PrintWriter out = null;
		List<String> retlist = new ArrayList<String>();

		try {
			sock = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()), 8192);
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + host);
			return new String[0];
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for socket to " + host + ":"
					+ Integer.toString(port));
			return new String[0];
		}
		out.println(req);
		String line;
		while ((line = in.readLine()) != null) {
			if (!line.equals(""))
				retlist.add(line);
		}
		out.close();
		in.close();
		sock.close();

		return retlist.toArray(new String[retlist.size()]);
	}
	public String command(String cmdString) {
		String[] data = new String[0];
		String ret = "";

		isCommandStringValid(cmdString);
		try {
			data = request(cmdString);
		} catch (IOException e) {
			System.err.println("Failed to read data from " + host + ":"
					+ Integer.toString(port));
		}
		for (String s : data) {
			ret += s + "\n";
		}
		return ret;
	}
	public OlsrDataDump parseCommand(String cmd) {
		if (mapper == null)
			mapper = new ObjectMapper();
		OlsrDataDump ret = new OlsrDataDump();
		try {
			String dump = command(cmd);
			if (! dump.contentEquals(""))
				ret = mapper.readValue(dump, OlsrDataDump.class);
			ret.setRaw(dump);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ret.config == null)
			ret.config = new com.xd.adhocroute.data.Config();
		if (ret.gateways == null)
			ret.gateways = Collections.emptyList();
		if (ret.hna == null)
			ret.hna = Collections.emptyList();
		if (ret.interfaces == null)
			ret.interfaces = Collections.emptyList();
		if (ret.links == null)
			ret.links = Collections.emptyList();
		if (ret.mid == null)
			ret.mid = Collections.emptyList();
		if (ret.neighbors == null)
			ret.neighbors = Collections.emptyList();
		if (ret.topology == null)
			ret.topology = Collections.emptyList();
		if (ret.plugins == null)
			ret.plugins = Collections.emptyList();
		if (ret.routes == null)
			ret.routes = Collections.emptyList();
		return ret;
	}

	public OlsrDataDump all() {
		return parseCommand("/all");
	}
	public OlsrDataDump runtime() {
		return parseCommand("/interfaces");
	}
	public OlsrDataDump startup() {
		return parseCommand("/interfaces");
	}
	public Collection<Neighbor> neighbors() {
		return parseCommand("/neighbors").neighbors;
	}
	public Collection<Link> links() {
		return parseCommand("/links").links;
	}
	public Collection<Route> routes() {
		return parseCommand("/routes").routes;
	}
	public Collection<HNA> hna() {
		return parseCommand("/hna").hna;
	}
	public Collection<MID> mid() {
		return parseCommand("/mid").mid;
	}
	public Collection<Node> topology() {
		return parseCommand("/topology").topology;
	}
	public Collection<Interface> interfaces() {
		return parseCommand("/interfaces").interfaces;
	}
	public Collection<Gateway> gateways() {
		return parseCommand("/gateways").gateways;
	}

	public com.xd.adhocroute.data.Config config() {
		return parseCommand("/config").config;
	}

	public Collection<Plugin> plugins() {
		return parseCommand("/plugins").plugins;
	}

	public String olsrdconf() {
		return command("/olsrd.conf");
	}
	
	public static void main(String[] args) throws IOException {
		JsonInfo jsoninfo = new JsonInfo();
		OlsrDataDump dump = jsoninfo.all();
		System.out.println("gateways:");
		for (Gateway g : dump.gateways)
			System.out.println("\t" + g.ipAddress);
		System.out.println("hna:");
		for (HNA h : dump.hna)
			System.out.println("\t" + h.destination);
		System.out.println("Interfaces:");
		for (Interface i : dump.interfaces)
			System.out.println("\t" + i.name);
		System.out.println("Links:");
		for (Link l : dump.links)
			System.out.println("\t" + l.localIP + " <--> " + l.remoteIP);
		System.out.println("MID:");
		for (MID m : dump.mid)
			System.out.println("\t" + m.ipAddress);
		System.out.println("Neighbors:");
		for (Neighbor n : dump.neighbors)
			System.out.println("\t" + n.ipv4Address);
		System.out.println("Plugins:");
		for (Plugin p : dump.plugins)
			System.out.println("\t" + p.plugin);
		System.out.println("Routes:");
		for (Route r : dump.routes)
			System.out.println("\t" + r.destination);
		System.out.println("Topology:");
		for (Node node : dump.topology)
			System.out.println("\t" + node.destinationIP);
	}
}
