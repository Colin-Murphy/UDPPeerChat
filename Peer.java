/*
	Peer.java
	Represents a single peer and handles the networking for that one peer
	@author Colin Murphy <clm3888@rit.edu>

	Part of data comm homework 3
*/


public class Peer {
	public String name = null;
	public int age = 0;
	public int zip = 0;


	private String ip;
	private Session s = null;


	public Peer(String name, int age, int zip, String ip, Session s) {
		this.name = name;
		this.age = age;
		this.zip = zip;
		this.ip = ip;
		this.s = s;
	}

	/*
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
	*/


	public void deliver(String message) {
		/*
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
		*/
	}

	/*
		Really java... I can't overwrite toString()?
	*/
	public String asString() {
		return "[" + name + "@" + this.ip + " " + age + " " + zip +"]";
	}

	public String asSimpleString() {
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

	/*
	public String getIP() {
		String ip = sock.getRemoteSocketAddress().toString();
		ip = ip.substring(1,ip.indexOf(":"));
		return ip;
	}
	*/
}