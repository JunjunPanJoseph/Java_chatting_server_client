package ass2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;

public class P2PServerUser extends User {
	public P2PServerUser(P2PServer server, String name) {
		super(server, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean checkTimeout(Date time) {
		return false;
	}

	@Override
	public void handleSingleMessage(Message message) {
		//System.out.println(this.name +" P2P Handle: " + message.toString());
		switch (message.type) {
		case "error":			
			Global.printToConsole(message.message);
			break;
		case "private":
			// Display private message
			if (message.token.equals("stop")) {
				this.connected = false;
			} else {
				Global.printToConsole(name + "(private): " + message.message);
			}
			break;
		case "stopprivate":
			// Stop private link with user
			Global.printToConsole(name + "stop the P2P private connection with you.");
			this.connected = false;
			// Remove from list. 
			// Won't cause problems since we copied user list.
			// this.closeSocket();
			server.getUsers().remove(name);
			
			break;
		case "logout":
			this.connected = false;
			break;
		default:
			Global.error(out, "Error. Invalid private message: " + message.toString());
		}
	}

	
}
