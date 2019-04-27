/*
	Session.java
	Maintains connections between peers in a chat group
	@author Colin Murphy <clm3888@rit.edu>

	Part of data comm homework 3
*/


import java.util.ArrayList;
import java.lang.IllegalArgumentException;
import java.io.IOException;

import java.net.*;
import java.io.*;

import org.json.JSONObject;
import org.json.JSONArray;

public class Session extends Thread {
	//There is always a session, until you try to join someone it's a local session
	public boolean joined = false;

	public ArrayList<Peer> peers;

	public ChatUI ui = null;
	public int serverPort;

	//Users information
	public String name;
	public int zip;
	public int age;

	private ServerSocket server = null;
	private Socket socket = null;

	//Restictions
	int name_max_length = 32;


	public Session(String name, int zip, int age, int port) throws IllegalArgumentException, IOException {
		setUserName(name);
		setAge(age);
		setZip(zip);
		peers = new ArrayList<Peer>();
		this.serverPort = port;
		this.server = new ServerSocket(serverPort);
		this.serverPort = port;
		this.start();

	}

	public void run() {

		//System.out.println("Runnnig");

		//Sessions never stop until the entire program quits
		while (1>0) {
			if (server != null) {
				try {
					Socket sock = server.accept();
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
					BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

					Peer p = new Peer(sock, in, out, this, false, false, 0);
					p.start();
					peers.add(p);
					joined = true;

				}

				catch (Exception e) {
					//e.printStackTrace();
				}

			}

			
			else {
				System.out.print("");
			}
			
		}

	}

	public String asString() {
		return "[" + name + " " + age + " " + zip +"]";
	}

	/*
		Display all users (including self)
	*/
	public void showUsers() {
		//print yourself first
		System.out.println(asString());
		for (Peer p: peers) {
			System.out.println(p.asString());
		}
	}

	/*
		Display all users (including self) that are in this zip
	*/
	public void showZip(int zip) {
		if (this.zip == zip) {
			System.out.println(asString());
		}

		for (Peer p: peers) {
			if (p.zip == zip) {
				System.out.println(p.asString());
			}
		}
	}

	/*
		Display all users (including self) that are of this age
	*/
	public void showAge(int age) {
		if (this.age == age) {
			System.out.println(asString());
		}

		for (Peer p: peers) {
			if (p.age == age) {
				System.out.println(p.asString());
			}
		}
	}

	/*
		Leave the chat
		silent: boolean whether or not it should print that you left
	*/
	public void leave(boolean silent) {
		JSONObject message = new JSONObject();
		message.put("type", "leave");
		for (Peer p:peers) {
			try {
				p.deliver(message.toString());
				p.in.close();
				p.out.close();
				p.sock.close();
				p.joined = false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!silent) {
			System.out.println("[left chat]");
		}

		peers = new ArrayList<Peer>();

		joined = false;
	}

	/*
		Added the boolean requirement later, keep around a no args version in case
	*/
	public void leave() {
		leave(false);
	}

	public JSONArray peersExcluding(Peer exclude) {
		JSONArray peersResp = new JSONArray();

		for (Peer p:peers) {
			//Dont tell a peer about themself
			if (p != exclude) {
				JSONObject peer = new JSONObject();
				peer.put("ip", p.getIP());
				peer.put("port", p.port);
				peersResp.put(peer);
			}

		}

		return peersResp;
	}


	/*
		Connect to a peer
		ip: the ip to connect to
		discover: Whether the peer needs to discover the network
	*/
	public void joinPeer(String ip, int port, boolean discover) throws IOException {

		Socket sock = new Socket();//(ip, port);
		sock.connect(new InetSocketAddress(ip, port), 1000);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

		Peer p = new Peer(sock, in, out, this, true, discover, port);
		p.start();

		peers.add(p);

		joined = true;

	}

	public void joinPortAndIP(int port, String ip) throws IOException {
		joinPeer(ip, port, true);
	}

	//No port provided, assume host uses the same port
	public void joinIP(String ip) throws IOException {
		joinPeer(ip, serverPort, true);
	}

	public void setUserName(String name) throws IllegalArgumentException {
		this.name = name;


	}

	public void setAge(int age) throws IllegalArgumentException {
		if (legalAge(age)) {
			this.age = age;
		}
		else {
			this.age = 200;
		}
	}

	public boolean legalAge(int age) {
		return age > 0 && age < 200;
	}



	public void setZip(int zip) {
		if (legalZip(zip)) {
			this.zip = zip;
		}
		else {
			this.zip = 99999;
		}

	}

	public boolean legalZip(int zip) {
		return zip > 0 && zip <100000;
	}

	/*
		Accepts raw keyboard input from user
		Escapes message and inserts into correct format
		Delivers to each peer
	*/
	public void sendMessage(String message) {

		//Format the message as json and escape the text
		JSONObject m = new JSONObject();
		m.put("type", "message");
		m.put("message", message);

		Peer[] example = new Peer[peers.size()];

		Peer[] peerArr = peers.toArray(example);
		//Deliver to all peers
		for (int i=0; i<peerArr.length; i++) {
			Peer p = peerArr[i];
			p.deliver(m.toString());
		}
	}

}