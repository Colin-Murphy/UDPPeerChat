/*
	PeerChat.java
	Handles main execution of program, sets up threads to do the rest
	@author Colin Murphy <clm3888@rit.edu>

	Note: This project makes use of the org.json library, an open source free use libary
	for handling JSON data in java. This library is NOT my original work.

	More information on org.json can be found here: https://mvnrepository.com/artifact/org.json/json

	Part of data comm homework 3
*/

import java.lang.Thread.*;
import java.net.BindException;

public class PeerChat {
	//Holds the session that will eventually be created
	private Session s = null;
	//Chat UI
	private ChatUI ui = null;

	public PeerChat(String[] args) {
		//Expected arg length
		int argsNoPort = 3;
		int argsWithPort = 5;

		try {
			ui = new ChatUI();
			if (args.length != argsWithPort && args.length != argsNoPort) {
				System.err.println("Invalid Arguments: Exiting...");
				System.exit(1);
			}

			//Provided a port to join
			else if (args.length == argsWithPort) {
				s = new Session(args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[1]));
			}

			else {
				s = new Session(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), 8080);

			}

			ui.s = s;
			ui.start();
		}

		catch (BindException e) {
			System.err.println("Port already in use, use -p to join a different one");
			System.exit(1);
		}

		catch (Exception e) {
			System.err.println("Invalid Arguments: Exiting...");
			System.exit(1);
		}

		
	}


	public static void main(String[] args) {
		new PeerChat(args);

	}
}