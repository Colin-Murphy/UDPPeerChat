import java.net.*;
import java.io.*;

public class ForwardListener extends Thread {

	public int port;
	public Session s;
	public ServerSocket server;

	public ForwardListener(int port, Session s) {
		this.port = port;
		this.s = s;
	}
	
	public void run() {

		//System.out.println("Forward server listening");
		//System.out.println(this.port);

		try {
			server = new ServerSocket(port);
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		while (1>0) {
			try {
				//System.out.println("Waiting for connection");
				//System.out.println(server.getLocalPort());
				Socket sock = server.accept();
				//System.out.println("Got connection");
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

				String message = in.readLine();
				//System.out.println("Got forwarded message");
				in.close();
				s.processMessage(message, sock.getInetAddress(), sock.getInetAddress().toString());
			}

			catch (Exception e) {
				//e.printStackTrace();
			}
		}

	}

	public void close() {
		try {
			server.close();
		}

		catch (Exception e) {
			//e.printStackTrace();
		}
	}
}