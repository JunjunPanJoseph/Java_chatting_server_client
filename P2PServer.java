package ass2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class P2PServer extends Server{
	public String name;
	public P2PServer(String name) throws IOException {
		super(0);
		this.name = name;
	}
	
	@Override
	public void handleClientConnect(Socket client) throws IOException {
		// Add to user list
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
		Message message = Global.getMessageFromInputBuffer(1, inFromClient, outToClient);

		String sender = message.sender.trim();
		if (getUsers().containsKey(message.sender)) {
		} else {
			P2PServerUser newUser = new P2PServerUser(this, message.sender);
			newUser.setupSocket(client, inFromClient, outToClient);
			Global.error(outToClient, "Start private messaging with " + name);
			getUsers().put(sender, newUser);
		}
		
	}
	public void sendMessageToPeer(String name, Message message) {
		if (!getUsers().containsKey(name)) {
			Global.printToConsole("Error. Private messaging to " + name + " not enabled");
		} else {
			User user = getUsers().get(name);
			if (!user.connected) {
				Global.printToConsole("Error. " + name + "is not online");
			} else {
				user.sendMessageToClient(message);
			}
		}
	}
	public void serverStop() {
		Message message = new Message();
		message.type = "logout";
		for (User u: getUsers().values()) {
			u.sendMessageToClient(message);
		}
	}
	public void connectToPeer(String name, String ip, String port) {
		if (this.getUsers().containsKey(name)) {
			Global.printToConsole("Error. P2P connection is already start with " + name);
		} else {
			try {
				Socket socket = new Socket(ip, Integer.parseInt(port));
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				P2PServerUser newUser = new P2PServerUser(this, name);
				newUser.setupSocket(socket, in, out);
				Message message = new Message();
				message.type = "startprivate";
				message.sender = this.name;
				message.user = name;
				newUser.sendMessageToClient(message);
				
				getUsers().put(name, newUser);
				Global.printToConsole("Start private messaging with " + name);
			} catch (IOException e) {
				// Failed.
				Global.printToConsole("Error. Failed to connect to peer" + name);
				
			}
		}
	}
	
}
