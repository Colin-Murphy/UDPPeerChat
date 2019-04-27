/*
	Peer.java
	Represents a single peer and handles the networking for that one peer
	@author Colin Murphy <clm3888@rit.edu>

	Part of data comm homework 3
*/

import java.net.*;
import java.io.*;

import org.json.JSONObject;
import org.json.JSONArray;

public class Peer extends Thread {
	public String name = null;
	public int age = 0;
	public int zip = 0;

	public Socket sock = null;
	public BufferedReader in = null;
	public BufferedWriter out = null;

	private Session s = null;

	//Whether or not the connection to this peer was created by "me" or if they initiated it
	private boolean initiated;

	//Whether or not the peer needs to be queried for a list of peers
	private boolean discover;

	public boolean joined = false;

	public int port;

	public Peer(Socket sock, BufferedReader in, BufferedWriter out, Session s, boolean initiated, boolean discover, int port) {
		this.sock = sock;
		this.in = in;
		this.out = out;
		this.s = s;
		this.initiated = initiated;
		this.discover = discover;
		this.port = port;
	}

	public void run() {
		try {
			//Peer hasn't fully joined
			if (!joined) {
				//This peer initiated the connection, so it must introduce itself
				if (initiated) {
					//Tell them my identity
					JSONObject message = new JSONObject();
					message.put("type", "join");
					message.put("name", s.name);
					message.put("age", s.age);
					message.put("zip", s.zip);
					message.put("port", s.serverPort);

					deliver(message.toString());

					//Get their response
					JSONObject input = new JSONObject(in.readLine());

					try {
						setUserName(input.get("name").toString());
						setZip(Integer.parseInt(input.get("zip").toString()));
						setAge(Integer.parseInt(input.get("age").toString()));
						this.port = Integer.parseInt(input.get("port").toString());
						joined = true;
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					//Send a who to this peer to learn the network
					if (discover) {
						message = new JSONObject();
						message.put("type", "who");
						deliver(message.toString());

						message = new JSONObject(in.readLine());

						JSONArray peers = message.getJSONArray("peers");

						for (int i = 0; i < peers.length(); i++) {
							JSONObject peer = peers.getJSONObject(i);
							String ip = peer.get("ip").toString();
							int port = Integer.parseInt(peer.get("port").toString());
							s.joinPeer(ip, port, false);
						}
						
						System.out.println("[joined chat with " + (s.peers.size()+1) + " members]");
					}



				}

				//Didn't start the connection, wait for them to introduce themselves
				else {
					JSONObject input = new JSONObject(in.readLine());

					try {
						setUserName(input.get("name").toString());
						setZip(Integer.parseInt(input.get("zip").toString()));
						setAge(Integer.parseInt(input.get("age").toString()));
						this.port = Integer.parseInt(input.get("port").toString());
						joined = true;
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					//Tell them my identity
					JSONObject message = new JSONObject();
					message.put("type", "join-reply");
					message.put("name", s.name);
					message.put("age", s.age);
					message.put("zip", s.zip);
					message.put("port", s.serverPort);

					deliver(message.toString());
					System.out.println("[member joined: " + name +"@" + getIP() +" " + zip + " " + age + "]");

					
				}

			}

			//Fully joined peer, read and handle their messages indefinitely
			while (joined) {
				JSONObject message = null;
				try {
					message = new JSONObject(in.readLine());
				}
				catch (NullPointerException e) {
					return;
					//Error json doesn't like it when you rip its sockets away, too bad
				}

				String type = message.get("type").toString();

				switch (type) {
					case "message":
						System.out.println("<" + name + "> " + message.get("message").toString());
						break;
					case "leave":
						try {
							in.close();
							out.close();
							sock.close();
							joined = false;
							s.peers.remove(this);
							System.out.println("[" + name + "@" + getIP() + " left the chat]");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "who":
						message = new JSONObject();
						message.put("type", "who-reply");
						message.put("peers", s.peersExcluding(this));

						deliver(message.toString());
						break;

				}

				
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deliver(String message) {
		try {
			out.write(message);
			out.newLine();
			out.flush();
		}

		//Connection died, disconnect this peer
		catch (SocketException e) {
			try {
				in.close();
				out.close();
				sock.close();
			}
			catch (Exception ex) {
				//Can't use the socket and can't close it, guess I'll just die then
			}
			joined = false;
			s.peers.remove(this);
			System.out.println("[" + name + "@" + getIP() + " left the chat]");
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
		Really java... I can't overwrite toString()?
	*/
	public String asString() {
		return "[" + name + " " + age + " " + zip +"]";
	}

	public void setUserName(String name) {
		this.name = name;

	}

	public void setZip(int zip) throws IllegalArgumentException {
		if (s.legalZip(zip)) {
			this.zip = zip;
		}
		else {
			this.zip = 99999;
		}

	}

	public void setAge(int age) throws IllegalArgumentException {
		if (s.legalAge(age)) {
			this.age = age;
		}
		else {
			this.age = 200;
		}
	}

	public String getIP() {
		String ip = sock.getRemoteSocketAddress().toString();
		ip = ip.substring(1,ip.indexOf(":"));
		return ip;
	}
}