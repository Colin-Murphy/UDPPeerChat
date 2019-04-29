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
	public ArrayList<InetSocketAddress> forwardAddresses; 

	public ChatUI ui = null;
	public int serverPort;

	public InetAddress group;
	public int port;

	//Users information
	public String name;
	public int zip;
	public int age;

	private ForwardListener forwardServer = null;

	//private ServerSocket server = null;
	private MulticastSocket socket = null;

	//Restictions
	int name_max_length = 32;


	public Session(String name, int zip, int age, int port, String[] forwardArr) {
		setUserName(name);
		setAge(age);
		setZip(zip);

		forwardAddresses = new ArrayList<InetSocketAddress>();

		for (String f: forwardArr) {
			if (f != null && !f.equals("")) {
				System.out.println(f);
				String ip = f.substring(0,f.indexOf(":"));
				int fport = Integer.parseInt(f.substring(f.indexOf(":")+1, f.length()));

				System.out.println(ip);
				System.out.println(fport);

				InetSocketAddress addr = new InetSocketAddress(ip, fport);

				System.out.println(addr.getPort());
				System.out.println(addr.getAddress());

				forwardAddresses.add(addr);
			}
		}
		peers = new ArrayList<Peer>();
		this.serverPort = port;
		//this.server = new ServerSocket(serverPort);
		//this.serverPort = port;
		this.start();

	}


	public void run() {

		//System.out.println("Runnnig");

		//Sessions never stop until the entire program quits
		while (1>0) {
			if (socket != null) {

				DatagramPacket packet = new DatagramPacket(new byte[10000], 10000);
				try {
					socket.receive(packet);
				}
				catch (Exception e) {}
				String data = new String(packet.getData(), 0, packet.getLength());

				//System.out.println(packet.getAddress());

				//System.out.println(packet.getLength());

				//Process the unforwarded message
				processMessage(data, null, packet.getAddress().toString());
				//try {
					/*
					Socket sock = server.accept();
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
					BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

					Peer p = new Peer(sock, in, out, this, false, false, 0);
					p.start();
					peers.add(p);
					joined = true;
					*/

				//}

				//catch (Exception e) {
					//e.printStackTrace();
				//}

			}

			
			else {
				System.out.print("");
			}
			
		}

	}

	public void processMessage(String message, InetAddress forwarder, String ip) {
		JSONObject input = new JSONObject(message);

		ip  = ip.substring(1,ip.length());

		//Message came from me, just ignore it
		if (input.get("sender").toString().equals(name)) {
			return;
		}

		//Message came from a multicast, send to all forwarders
		else if (forwarder == null) {

			for (InetSocketAddress f:forwardAddresses) {
				forward(message, f);
			}
		}

		//It was forwarded, so send it on to all other forwarders
		else {
			for (InetSocketAddress f:forwardAddresses) {
				if (!(f.getAddress().equals(forwarder))) {
					forward(message, f);
				}
			}
		}

		//Forwarding is done, process locally now
		String type = input.get("type").toString();


		switch (type) {
			case "message":
				System.out.println("<" + input.get("sender") + "> " + input.get("message").toString());
				break;
			case "join":
				//Learn about the peer
				String name = input.get("sender").toString();
				int zip = 0;
				int age = 0;
				String myIp = "";
				try {
					zip = Integer.parseInt(input.get("zip").toString());
					age = Integer.parseInt(input.get("age").toString());
				}

				catch (Exception e) {}

				Peer p = new Peer(name, age, zip, ip, this);
				peers.add(p);

				System.out.println("[member joined: " + name +"@" + ip +" " + zip + " " + age + "]");


				//Tell them about me
				JSONObject resp = new JSONObject();
				resp.put("type", "join-reply");
				resp.put("sender", this.name);
				resp.put("age", this.age);
				resp.put("zip", this.zip);

				sendEverywhere(resp);
				break;

			case "join-reply":
				name = input.get("sender").toString();
				zip = 0;
				age = 0;
				try {
					zip = Integer.parseInt(input.get("zip").toString());
					age = Integer.parseInt(input.get("age").toString());
				}

				catch (Exception e) {}

				p = new Peer(name, age, zip, ip, this);
				peers.add(p);
				break;

			case "leave":
				name = input.get("sender").toString();

				for (Peer peer:peers) {
					if (peer.name.equals(name)) {
						peers.remove(peer);
						System.out.println("[" + name + "@" + ip + " left the chat]");
						break;
					}
				}
				break;

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
				System.out.println(p.asSimpleString());
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
				System.out.println(p.asSimpleString());
			}
		}
	}

	/*
		Leave the chat
	*/
	public void leave() {
		JSONObject message = new JSONObject();
		message.put("type", "leave");
		message.put("sender", this.name);
		
		System.out.println("[left chat]");

		peers = new ArrayList<Peer>();

		try {
			socket.close();
			socket = null;
		}

		catch (Exception e) {}

		joined = false;

		//Close forwarding server
		forwardServer.close();
		forwardServer = null;
		
		sendEverywhere(message);
	}

	/*
		Connect to a peer
		ip: the ip to connect to
		discover: Whether the peer needs to discover the network
	*/
		/*
	public void joinPeer(String ip, int port, boolean discover, String[] f) throws IOException {

		Socket sock = new Socket();//(ip, port);
		sock.connect(new InetSocketAddress(ip, port), 1000);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

		Peer p = new Peer(sock, in, out, this, true, discover, port);
		p.start();

		peers.add(p);

		joined = true;

	}
	*/

	public void joinPortAndIP(int port, String ip) throws IOException {
		//joinPeer(ip, port, true);
		socket = new MulticastSocket(port);
		socket.setLoopbackMode(true);

		group = InetAddress.getByName(ip);
		this.port = port;
		try {
			socket.joinGroup(group);

			JSONObject message = new JSONObject();
			message.put("type", "join");
			message.put("sender", this.name);
			message.put("age", this.age);
			message.put("zip", this.zip);

			if (serverPort != 0) {
				forwardServer = new ForwardListener(serverPort, this);
				forwardServer.start();
			}

			sendEverywhere(message);

			joined = true;
			System.out.println("[joined chat]-");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
		Multicast the formatted message
	*/
	public void multicast(JSONObject payload) {
		byte[] buf = payload.toString().getBytes();

		try {
            DatagramSocket s = new DatagramSocket();
     
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            s.send(packet);
            s.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void forward(String message, InetSocketAddress addr) {
		try {
			System.out.println("Forwarding to: ");

			System.out.println(addr.getAddress());
			System.out.println(addr.getPort());

			Socket s = new Socket();
			s.connect(addr); //new InetSocketAddress("10.0.1.2", 10000), 1000);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			out.write(message);
			out.newLine();
			out.flush();

			out.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendEverywhere(JSONObject payload) {
		multicast(payload);

		for (InetSocketAddress addr: forwardAddresses) {
			forward(payload.toString(), addr);
		}
	}

	/*
		Accepts raw keyboard input from user
		Escapes message and inserts into correct format
		Delivers to each peer
	*/
	public void sendMessage(String message) {

		//Format the message as json and escape the text
		JSONObject m = new JSONObject();
		m.put("sender", this.name);
		m.put("type", "message");
		m.put("message", message);

		sendEverywhere(m);

        /*

		try {
			System.out.println(socket.getInterface());
			socket.send(packet);
		}

		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Somethisn wrong");
		}
		*/

		/*

		Peer[] example = new Peer[peers.size()];

		Peer[] peerArr = peers.toArray(example);
		//Deliver to all peers
		for (int i=0; i<peerArr.length; i++) {
			Peer p = peerArr[i];
			p.deliver(m.toString());
		}
		*/
	}

}