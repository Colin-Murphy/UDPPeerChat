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

		int len = args.length;

		int i = 0;
		String name = "";
		int age = 0;
		int zip = 0;
		int port = 0;

		String[] forwards = new String[4];
		int fIndex = 0;

		ui = new ChatUI();

		while (i<len) {
			if (len-i == 3) {
				name = args[i];
			}

			else if (len-i == 2) {
				try {
					zip = Integer.parseInt(args[i]);
				}
				catch (Exception e) {}
			}

			else if (len-i == 1) {
				try {
					age = Integer.parseInt(args[i]);
				}

				catch (Exception e) {}
			}

			else {
				if (args[i].equals("-p")) {
					try {
						port = Integer.parseInt(args[i+1]);
						i++;
					}

					catch (Exception e) {}
				}
				else if (args[i].equals("-f")) {
					forwards[fIndex] = args[i+1];
					fIndex++;
					i++;
				}
			}

			i++;
		}

		s = new Session(name, zip, age, port, forwards);
		ui.s = s;
		ui.start();

		/*
		System.out.println(name);
		System.out.println(age);
		System.out.println(zip);
		System.out.println(port);

		for (int j=0; j<fIndex; j++) {
			System.out.println(forwards[j]);
		}
		*/

		/*
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
		*/

		
	}


	public static void main(String[] args) {
		new PeerChat(args);

	}
}