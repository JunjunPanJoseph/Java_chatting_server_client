package ass2;

import java.io.*;
import java.net.*;
import java.util.*;

public abstract class User {
	protected Server server;
	protected String name;
	
	public String ip;
	public String port;
	
	protected Socket socket;
	protected BufferedReader in;
	protected DataOutputStream out;
	
	private List<Message> messages;
	
	public boolean connected;
	
	public User(Server server, String name) {
		this.server = server;
		this.name = name;
		this.connected = false;
		
		this.ip = "";
		this.port = "";
		
		this.socket = null;
		this.in = null;
		this.out = null;
		this.messages = new ArrayList<>();
	}
	
	public boolean setupSocket(Socket socket, BufferedReader in, DataOutputStream out) {
		if (isConnected()) {
			Global.printToConsole("Error. User " + name + " is connected when set up socket. ");
			return false;
		} else {
			this.socket = socket;
			this.in = in;
			this.out = out;
			this.connected = true;
			assert(isConnected() == true);
			return true;
		}
	}
	public boolean closeSocket() {
		if (!isConnected()) {
			return false;
		}
		
		try {
			socket.close();
		} catch (IOException e) {
			// e.printStackTrace();
			return false;
		}
		socket = null;
		in = null;
		out = null;
		connected = false;
		return true;
	}
	public void handleUserMessage() {
		if (!isConnected()) {
			return;
		}
		if (checkTimeout(new Date())) {
			System.out.println("Time out!");
			closeSocket();
			return;
		}
		// Send all messages to client
		for (Message message: messages) {
			System.out.println("Send msg to " + name + ": " + message.toString());
			sendMessageToClient(message);
		}
		// Clear msg queue
		messages.clear();
		
		try {
			Message message;
			message = Global.getMessageFromInputBuffer(
					socket.getInputStream().available(), in, out);
			if (message != null) {
				// Check sender
				if (message.sender.equals(name)) {
					handleSingleMessage(message);
				} else {
					Global.error(out, "Error. Sender name did not correspond to receiver.");
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
			Global.printToConsole("Error. IOException when handle user message.");
		}
	}
	
	public boolean sendMessageToClient(Message message) {
		return Global.sendMessage(out, message);
	}
	
	public boolean sendMessageToUser(User userTo, Message message) {
		return userTo.addMessageToList(this, message);
	}
	
	public boolean addMessageToList(User userFrom, Message message) {
		if (allow(userFrom)) {
			messages.add(message);
			return true;
		} else {
			return false;
		}
	}
	public boolean isConnected() {
		return connected;
	}
	
	
	public boolean allow(User user) {
		return true;
	}
		
	public boolean loginCheck(Message message, DataOutputStream out) {
		return true;
	}
	
	public String getName() { 
		return name;
	}
	public long getActivateTime() {
		return 0;
	}
	public abstract boolean checkTimeout(Date time) ;
	public abstract void handleSingleMessage(Message message);


}
