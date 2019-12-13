package ass2;

import java.io.*;
import java.net.*;

public class CenterServer extends Server {
	public CenterServer(int server_port, String datafile, int block_duration, int timeout) throws IOException {
		super(server_port);
		// Read file
		try {
            BufferedReader reader = new BufferedReader(new FileReader(datafile));
            String line = reader.readLine();
            while (line != null) {
            	String[] lineSplit = line.split(" ");
            	addUser(lineSplit[0], new CenterServerUser(this, lineSplit[0], lineSplit[1], block_duration, timeout));
            	line = reader.readLine();
            }
            reader.close();
		} catch (IOException e) {
			Global.printToConsole("Server error: fail to read account file" );
			System.exit(1);
		}
	}

	@Override
	public void handleClientConnect(Socket client) throws IOException {
		
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
		Message message = Global.getMessageFromInputBuffer(1, inFromClient, outToClient);
		/*
		if (message != null) {
			Global.printToConsole("Recv msg: ");
			message.show();
		}
		*/
		if (message == null) {
			// Failed to get message from client. Close the socket
			Global.printToConsole("Recv msg is null");
			// client.close();
		} else if (!message.type.equals("login")) {
			// Server only handle login
			Global.error(outToClient, "Invalid message.");
			// client.close();
		} else if (!Global.checkMessageNameValid(message, getUsers(), null, outToClient)) {
			// Invalid user name
			// client.close();
		} else if (!getUsers().get(message.sender).loginCheck(message, outToClient)){
			// Login fail
			// client.close();
		} else {
			// Login check passed
			getUsers().get(message.sender).setupSocket(client, inFromClient, outToClient);
		}
	}
	
}
