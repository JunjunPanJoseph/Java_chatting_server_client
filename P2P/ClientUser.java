package assignment.P2P;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

import assignment.Client;
import assignment.EncodeDecode;

public class ClientUser {
	private Client client;
	public String name =  "";
	private boolean running;
	
	
	private Socket socket;
	private DataInputStream datainStream;
	private DataOutputStream dataoutStream;
	
	public ClientUser(Client client, String name){
		this.client = client;
		this.running = false;
		this.name = name;
	}
	public boolean connectTo(String ip, int port) {
	    
		try {
			InetAddress IPAddress = InetAddress.getByName(ip.split("/")[0]);
			socket = new Socket(IPAddress, port);
			
			this.dataoutStream = new DataOutputStream(socket.getOutputStream());
			this.datainStream = new DataInputStream(socket.getInputStream());
		} catch (Exception e) {
			// failed 
			// e.printStackTrace();
			socket = null;
			return false;
		}
		if (socket == null) {
			return false;
		}
		this.running = true;

		return true;
	}
	public boolean setSocket(Socket socket) {
		if (this.socket != null || this.running) {
			return false;
		}
		try {
			this.socket = socket;
			this.dataoutStream = new DataOutputStream(socket.getOutputStream());
			this.datainStream = new DataInputStream(socket.getInputStream());

			String line = datainStream.readLine();
			this.running = true;
			this.name = line;
			
		} catch (IOException e) {
			this.socket = null;
			return false;
		}
		return true;
	}
	public boolean sendPrivateMessage(String message) {
		if (!running()) {
			return false;
		}
		String[] args = {"11", client.getName(), message};
		sendMsgToStream(args);
		return true;
	}

	public boolean processMessage() {
		if (!running) {
			return false;
		}
		//System.out.println("Process msg from ["+ name + "]");
		try {
			if (datainStream.available() > 0) {
				String line = datainStream.readLine();
				System.out.println(this.name + " receive: " + line);
				String[] args = EncodeDecode.decode(line);
				if (args.length >= 2) {
					String username = args[1];
					String message;
					switch (args[0]) {
					case "11":
						// display message
						message = line.substring(args[0].length() + username.length() + 2);
						System.out.println(username + "(private): " + message);
						break;
					case "12":
						// stop server
						stopPrivate();
						System.out.println("User " + username + "stop P2P connection with you!");
						break;
					default:
						System.out.println("Error: invalid message format - " + line);
						break;
					}
				} else {
					System.out.println("Error: invalid message format - " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException in client user - process m");
		}
		
		return true;
	}

	public boolean stopPrivate() {
		if (!running()) {
			return false;
		}
		String[] args = {"12", client.getName()};
		sendMsgToStream(args);
		try {
			socket.close();
			//client.removeClientUser(name);
		} catch (Exception e) {			
			return false;
		}
		this.running = false;
		return true;
	}

	public boolean running() {
		return this.running;
	}
	public void sendMsgToStream(String arg) {
		try {
			System.out.println("Send msg to " + name + arg);
			dataoutStream.writeBytes(arg);
		} catch (IOException e) {
			System.out.println("Error: IO exception in sendMsgToStream");
		}
	}
	public void sendMsgToStream(String[] args) {
		String line = EncodeDecode.encode(args);
		sendMsgToStream(line);
	}

}
