package ass2;

import java.io.*;
import java.util.*;
import java.net.*;

public abstract class Server {
	private ServerSocket socket; 
	private Map<String, User> users;
	private List <User> tmpUsersList;
	public Server(int server_port) throws IOException {
		socket = new ServerSocket(server_port);
		// Avoid blocking / deadlock
		socket.setSoTimeout(100);
		users = new HashMap<>();
		tmpUsersList = new ArrayList<>();
	}

	public boolean addUser(String name, User user) {
		if (users.containsKey(name)) {
			return false;
		} else {
			users.put(name, user);
			return true;
		}
	}
	
	public abstract void handleClientConnect(Socket client) throws IOException;
	
	public void handleServerMessage() {
		// Handle new connection, input, etc
		// new connection
		try {
			Socket client = socket.accept();
			// Global.printToConsole("Accept connect to server");
			handleClientConnect(client);
			// handle new connection
			
		} catch (IOException e) {
			// Timeout, stop blocking
		}
		// handle message send to users

		tmpUsersList.clear();
		for (User user: users.values()) {
			tmpUsersList.add(user);
		}
		for (User user: tmpUsersList) {
			if (user.isConnected()) {
				// System.out.println("Online user: " + user.name);
				user.handleUserMessage();
			}
		}
		tmpUsersList.clear();
	}
	public String getServerIp() {
		return socket.getLocalSocketAddress().toString().split("/")[0];
	}
	
	public String getIpAddr() {
		return socket.getInetAddress().toString().split("/")[0];
	}
	
	public int getPort() {
		return socket.getLocalPort();
	}
	public Map<String, User> getUsers(){
		return users;
	}
	
	
	public static void main(String[] args) throws IOException {
		int server_port = 12345;
        int block_duration = 30;
        int timeout = 120;
        // Read command line arguments
        if(args.length != 3){
        	Global.printToConsole("Usage: java Server server_port block_duration timeout\n");
            // System.exit(1);
        } else {
        	server_port = Integer.parseInt(args[0]);
        	block_duration = Integer.parseInt(args[1]);
        	timeout = Integer.parseInt(args[2]);
        }
        CenterServer server = new CenterServer(
        		server_port, 
        		"credentials.txt", 
        		block_duration, 
        		timeout);
        Global.printToConsole("IP: " + server.getIpAddr());
        Global.printToConsole("Port: " + server.getPort());
        Global.printToConsole("Server running...");
        while (true) {
        	server.handleServerMessage();
        }
	}
}