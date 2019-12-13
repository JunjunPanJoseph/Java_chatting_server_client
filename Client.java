package ass2;

import java.io.*;
import java.util.*;
import java.net.*;

public class Client {
	
	public static void main(String[] args) throws IOException {
		String server_IP = "0.0.0.0";
        int server_port = 12345;
        List<String> remove_list = new ArrayList<>();
        // Read command line arguments
        if(args.length != 2){
        	Global.printToConsole("Usage: java Client server_IP server_port\n");
            // System.exit(1);
        } else {
        	server_IP = args[0];
        	server_port = Integer.parseInt(args[1]);
        }
        int i = 0;
        BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in));
        
        Socket socket = null;
        BufferedReader clientIn = null; 
        DataOutputStream clientOut = null;
        
        
        String name = "";
        String token = "";
        Message message = null;
        
        P2PServer p2pServer = null;
        // Infinity loop
        while (true) {
        	
        	// read keyboard input
        	boolean loginSuccess = false;
        	while (!loginSuccess) {
        		System.out.print("Username: ");
				String username = stdinReader.readLine().trim();
        		System.out.print("Password: ");
	        	String password = stdinReader.readLine().trim();
	        	socket = new Socket(server_IP, server_port);
	        	clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    		clientOut = new DataOutputStream(socket.getOutputStream());
	    		// Encode message and send to server
	    		message = new Message();
	    		message.type = "login";
	    		message.sender = username;
	    		message.password = password;
	    		
	    		Global.sendMessage(clientOut, message);
	    		
	    		// Receive message from server
	    		message = Global.getMessageFromInputBuffer(1, clientIn, null);
	    		// Global.printToConsole(message.toString());
	    		if (message == null) {
	    			// Error. 
    				Global.errorInvalidMessage(null, null);
	    		} else {
	    			switch (message.type) {
	    			case "login":
	    				// Success
	    				loginSuccess = true;
	    				name = username.trim();
	    				token = message.token;
	    				break;
	    			case "error":
		    			// Failed
	    				Global.printToConsole(message.message);
	    				socket.close();
		    			break;
	    			default:
		    			// default
	    				socket.close();
		    			break;
	    			}
	    		}
        	}
        	// P2P server
        	p2pServer = new P2PServer(name);
        	
        	message = new Message();
        	message.sender = name;
        	message.token = token;
        	message.type = "ip";
        	message.ip = p2pServer.getIpAddr();
        	message.port = p2pServer.getPort() + "";
    		Global.sendMessage(clientOut, message);
    		
    		
        	Global.printToConsole("Welcome to the greatest messaging application ever!");
        	// Main loop
        	boolean online = true;
        	while (online) {
        		remove_list.clear();
            	// Handle message from server
				if (socket.getInputStream().available() > 0) {
		    		message = Global.getMessageFromInputBuffer(1, clientIn, null);
		    		if (message != null) {
			    		switch (message.type) {
			    		case "error":
		    				Global.printToConsole(message.message);
		    				if (message.message.equals("Timeout")) {
		    					online = false;
		    				}
			    			break;
			    		case "message":
			    			Global.printToConsole(message.sender + ": " + message.message);
			    			break;
			    		case "whoelse":
			    			String[] onlineList = message.message.split(" ");
			    			for (String username: onlineList) {
				    			Global.printToConsole(username);
			    			}
			    			break;
			    		case "startprivate":
			    			p2pServer.connectToPeer(message.user, message.ip, message.port);
			    			break;
			    		default:
			    			Global.errorInvalidMessage(null, message);
		    				break;
			    		}
		    		}
				}
				if (online == false) {
					break;
				}
				// Handle keyboard input
				message = new Message();
				message.sender = name;
				message.token = token;
				if (System.in.available() > 0) {
					String inputString = stdinReader.readLine().trim();
					String[] splitedString = inputString.split(" ");
					switch (splitedString[0]) {
					case "logout":
						message.type = "logout";
						break;
					case "message":
						if (splitedString.length < 3) {
			    			Global.printToConsole("Usage: message <user> <message>");
						} else if (splitedString[1].trim().equals(name)){
							Global.printToConsole("Error. Cannot send message to youself!");
						} else {
							message.type = "message";
							message.user = splitedString[1];
							message.message = getMessage(splitedString, 2);
						}
						break;
					case "broadcast":
						if (splitedString.length < 2) {
			    			Global.printToConsole("Usage: broadcast <message>");
						} else {
							message.type = "broadcast";
							message.message = getMessage(splitedString, 1);
						}
						break;
					case "whoelse":
						if (splitedString.length != 1) {
			    			Global.printToConsole("Usage: whoelse");
						} else {
							message.type = "whoelse";
						}
						break;
					case "whoelsesince":
						if (splitedString.length != 2) {
			    			Global.printToConsole("Usage: whoelsesince <time_in_second>");
						} else {
							try {
								message.time = "" + Long.valueOf(splitedString[1]);
								message.type = "whoelsesince";
							} catch (NumberFormatException e) {
				    			Global.printToConsole("Error: Invalid time format");
							}
						}
						break;
					case "block":
						if (splitedString.length != 2) {
			    			Global.printToConsole("Usage: block <user_name>");
						} else {
							message.type = "block";
							message.user = splitedString[1];
						}
						break;
					case "unblock":
						if (splitedString.length != 2) {
			    			Global.printToConsole("Usage: unblock <user_name>");
						} else {
							message.type = "unblock";
							message.user = splitedString[1];
						}
						break;
					case "startprivate":
						// TODO: extra check on connection stage.
						if (splitedString.length != 2) {
			    			Global.printToConsole("Usage: startprivate <user_name>");
						} else if (splitedString[1].trim().equals(name)){
							Global.printToConsole("Error. Cannot connect to youself!");
						} else {
							if (!p2pServer.getUsers().containsKey(splitedString[1].trim())) {
								message.type = "startprivate";
								message.user = splitedString[1];
							} else {
				    			Global.printToConsole("Error. P2P connection is already start with " + splitedString[1]);
							}
						}
						break;
					case "stopprivate":
						if (splitedString.length != 2) {
			    			Global.printToConsole("Usage: stopprivate <user_name>");
						} else if (splitedString[1].trim().equals(name)){
							Global.printToConsole("Error. Cannot stop connect to youself!");
						} else if (!p2pServer.getUsers().containsKey(splitedString[1].trim())){
							Global.printToConsole("Error. Private messaging to hans not enabled. ");
						} else {
							message.type = "private";
							message.token = "stop";
							message.message = "stop, please";
							p2pServer.sendMessageToPeer(splitedString[1].trim(), message);
							remove_list.add(splitedString[1].trim());
							Global.printToConsole("Stop private messaging with " + splitedString[1].trim());
							message.type = "";
						}
						break;
					case "private":
						if (splitedString.length < 3) {
			    			Global.printToConsole("Usage: private <user_name> message");
						} else if (splitedString[1].trim().equals(name)){
							Global.printToConsole("Error. Cannot send private message to youself!");
						} else  {
							message.type = "private";
							message.message = getMessage(splitedString, 2);
							p2pServer.sendMessageToPeer(splitedString[1].trim(), message);
							message.type = "";
						}
						break;
		    		default:
		    			if (!inputString.equals("")) {
		    				Global.printToConsole("Error. Invalid command");
		    			}
	    				break;
					}
					if (!message.type.equals("")) {
						// Send to server
						Global.sendMessage(clientOut, message);
						if (message.type.equals("logout")) {
							online = false;
							p2pServer.serverStop();
						}
					}
				}
				if (online == false) {
					break;
				}
        		// Handle server input
				p2pServer.handleServerMessage();
				for (User user: p2pServer.getUsers().values()) {
					if (user.connected == false) {
						remove_list.add(user.name);
					}
				}
				for (String t: remove_list) {
					p2pServer.getUsers().get(t).closeSocket();
					p2pServer.getUsers().remove(t);
				}
				
        	}
        }
	}

	public static String getMessage(String[] splitedString, int i) {
		String str = "";
		while (i < splitedString.length) {
			str += splitedString[i] + " ";
			i++;
		}
		return str.trim();
	}
}