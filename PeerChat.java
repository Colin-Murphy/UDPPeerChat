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


		
	}


	public static void main(String[] args) {
		new PeerChat(args);

	}
}