package assignment;

import java.io.*;
import java.net.*;
import java.util.*;

import assignment.P2P.ClientUser;

public class Client {
	private String username;
	private String authcode;
	
	private Socket socket;
	private DataInputStream inStream;
	private DataOutputStream outStream;

	
	private ServerSocket serverSocket; 
    private DataInputStream in;
	private DataOutputStream out;
    
    public Map<String, ClientUser> userMap;
    
	public Client(String username, String authcode) {
		this.username = username;
		this.authcode = authcode;
		this.userMap = new HashMap<>();
		this.serverSocket = null;
	}


	private void setServerSocket() {
		try {
			this.serverSocket = new ServerSocket(0);
			serverSocket.setSoTimeout(100);
		} catch (IOException e) {
			System.out.println("Error: fail to create p2p server");
			this.serverSocket = null;
		}
	}
	
	public void removeClientUser(String username) {
		userMap.remove(username);
	}
	
	public void setSocket(Socket clientSocket) {
		this.socket = clientSocket;
	}	
	public String getName() {
		return username;
	}
	public String getAuthcode() {
		return authcode;
	}
	
	public void setDataInputStream(DataInputStream in) {
		this.inStream = in;
		this.in = new DataInputStream(this.inStream);
	}
	public void setDataOutputStream(DataOutputStream out) {
		this.outStream = out;
		this.out = new DataOutputStream(this.outStream);
	}

	public void logoff() {
		for (ClientUser user: userMap.values()) {
			user.stopPrivate();
		}

		String[] code = {"3", username, authcode};
		sendMsgToStream(code);
	}
	
	public void sendMsgToStream(String[] args) {
		try {
			String line = EncodeDecode.encode(args);
			// System.out.println("Server send: " + line);
			out.writeBytes(line);
		} catch (IOException e) {
			System.out.println("Error: IO exception in sendMsgToStream");
		}
	}
	
	public static void main(String[] args) throws Exception {
		InetAddress IPAddress;
		int serverPort;
		if(args.length != 2){
	        System.out.println("Usage: java UDPClinet localhost PortNo");
	        // System.exit(1);
		    IPAddress = InetAddress.getByName("0.0.0.0");
		    serverPort = 5001;
	    } else {

			// Define socket parameters, address and Port No
		    IPAddress = InetAddress.getByName(args[0]);
		    serverPort = Integer.parseInt(args[1]);
		    
	    	
	    }
        Socket clientSocket;
        OutputStream outToServer;
        DataOutputStream out;
        InputStream inFromServer;
        DataInputStream in = null;
        Scanner inStream = new Scanner(System.in);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        
        // Login loop
        
        Client client;
    	do {
	        client = null;
	        while (client == null) {
	    		System.out.print("Username: ");
	    		String username = inStream.nextLine().trim();
	    		System.out.print("Password: ");
	    		String password = inStream.nextLine().trim();
				String[] code = {"0", username, password};

	            clientSocket = new Socket(IPAddress, serverPort);
	            outToServer = clientSocket.getOutputStream();
	            out = new DataOutputStream(outToServer);
	            inFromServer = clientSocket.getInputStream();
	            in = new DataInputStream(inFromServer);
	    		
	            out.writeBytes(EncodeDecode.encode(code));
	    		
	    		String returnLine = in.readLine();
	    		String[] lineDecode = EncodeDecode.decode(returnLine);
	    		System.out.println(returnLine);
	    		switch (lineDecode[0]) {
	    		case "2":
	    			// Success
	    			System.out.println("Welcome to the greatest messaging application ever!");
	    			client = new Client(username, lineDecode[1]);
	    			client.setSocket(clientSocket);
	    			client.setServerSocket();
	    			client.setDataOutputStream(out);
	    			client.setDataInputStream(in);
	    			break;
	    		case "1":
	    			// Failed
	    			switch(lineDecode[1]) {
	    			case "-1":
						// Repeated login
	    				System.out.println("This account is loggined in other terminal. Please contact admin. ");
	    				break;
	    			case "3":
	    				// Blocked
		    			System.out.println("Invalid Password. Your account has been blocked. Please try again later");
	    				break;
	    			case "1":
	    			case "2":
					default:
						// Invalid password;
		    			System.out.println("Invalid Password. ");
		    			break;
	    			}
	    			
	    			clientSocket.close();
					break;
					
	    		case "-1":
	    			System.out.println(lineDecode[1]);
	    			clientSocket.close();
	    			break;
				default:
					System.out.println("Error: unexpected return format! [" + returnLine + "]");
	    			clientSocket.close();
					System.exit(1);
	    		}
	    	} 
	    	System.out.println("Login success!");
	    	
            String input;
	    	boolean logged = true;
            
            
            
	    	while (logged) {
                // Keyboard input
	    		if (System.in.available() > 0) {		  

		    		input = bufferedReader.readLine();
		    		if (input.compareTo("Exit") == 0) {
		    			client.logoff();
		    			
		    			System.exit(0);
		    		}
		    		String[] strs = input.split(" ");
		    		String username;
		    		String message;
		    		int time;
		    		
		    		switch (strs[0]) {
		    		case "message":
		    			if (strs.length < 3) {
		    				error("message <user> <message>");
		    			} else {
		    				username = strs[1];
		    				message = input.substring(strs[0].length() + strs[1].length() + 2);
		    				String[] code = {"4", 
		    						client.getName(), 
		    						client.getAuthcode(), 
		    						username, 
		    						message};
		    				client.sendMsgToStream(code);
		    			}
		    			break;
		    		case "broadcast":
		    			if (strs.length < 2) {
		    				error("broadcast <message> ");
		    			} else {
		    				message = input.substring(strs[0].length() + 1);
		    				String[] code = {"5", 
		    						client.getName(), 
		    						client.getAuthcode(), 
		    						message};
		    				client.sendMsgToStream(code);
		    			}
		    			break;
		    		case "whoelse":
		    			if (strs.length != 1) {
		    				error("whoelse");
		    			} else {
		    				String[] code = {"6", 
		    						client.getName(), 
		    						client.getAuthcode(), 
		    						"0"
		    				};
		    				client.sendMsgToStream(code);
		    			}
		    			break;
		    		case "whoelsesince":
		    			if (strs.length != 2) {
		    				error("whoelsesince <time>");
		    			} else {
		    				try {
		    					
		    					time = Integer.parseInt(strs[1]);
			    				String[] code = {"6", 
			    						client.getName(), 
			    						client.getAuthcode(), 
			    						"" + time
			    				};
			    				client.sendMsgToStream(code);
		    					
		    				} catch (Exception e){
			    				error("<time> need be integer ");
		    				}
		    			}
		    			break;
		    		case "block":
		    			if (strs.length != 2) {
		    				error("block <user>");
		    			} else {
		    				username = strs[1];
		    				String[] code = {"7", 
		    						client.getName(), 
		    						client.getAuthcode(), 
		    						username
		    				};
		    				client.sendMsgToStream(code);
		    			}
		    			break;
		    		case "unblock":
		    			if (strs.length != 2) {
		    				error("unblock <user>");
		    			} else {
		    				username = strs[1];
		    				String[] code = {"8", 
		    						client.getName(), 
		    						client.getAuthcode(), 
		    						username
		    				};
		    				client.sendMsgToStream(code);
		    			}
		    			break;
		    		case "logout":
		    			client.logoff();
		    			logged = false;
		    			break;
		    		case "startprivate":
	    				username = strs[1];
	    				if (client.userMap.containsKey(username)) {
	    					System.out.println("Error: Private messaging to " + username + " enabled ");
	    				} else {
		    				String[] code = {"9", 
		    						client.getName(), 
		    						client.getAuthcode(), 
		    						username
		    				};
		    				client.sendMsgToStream(code);
			    			break;
	    				}
		    		case "private":
		    			if (strs.length >= 3) {
		    				username = strs[1].trim();
		    				message = input.substring(strs[0].length() + strs[1].length() + 2);
		    				if (!client.userMap.containsKey(username)) {
		    					System.out.println("Error: Private messaging to " + username + " not enabled ");
		    				} else if (!client.userMap.get(username).running()) {
		    					System.out.println("Error: " + username + " socket is not running");
		    				} else {
		    					if (!client.userMap.get(username).sendPrivateMessage(message)) {
		    						System.out.println("Error: send message failed");
		    					}
		    				}
		    			} else {
		    				System.out.println("Error: no msg entered.");
		    			}
		    			break;
		    		case "stopprivate":
	    				username = strs[1];
	    				if (!client.userMap.containsKey(username)) {
	    					System.out.println("Error: Private messaging to " + username + " not enabled ");
	    				} else if (!client.userMap.get(username).running()) {
	    					System.out.println("Error: " + username + " socket is not running");
	    				} else {
	    					if (client.userMap.get(username).stopPrivate()) {
	    						System.out.println("Stop private connection with " + username);
	    					} else {
								System.out.println("Error. stop private failed");
	    					}
	    				}
		    			break;
					default:
						if (input.compareTo("") != 0) {
							System.out.println("Error. Invalid command");
						}
						break;
		    		
		    		}
	    		} 
	    		
                // Processing server message
	    		if (in.available() > 0) {
		    		String returnLine = in.readLine();
		    		String[] lineDecode = EncodeDecode.decode(returnLine);
		    		
		    		switch (lineDecode[0]) {
		    		case "-1":
		    			// Server message
		    			System.out.println(lineDecode[1]);
		    			if (lineDecode[1].compareTo("Timeout.") == 0) {
		    				// Log out
		    				logged = false;
                            // ======== 
                            client.logoff();
		    			}
		    			break;
		    		case "4":
		    			// Message from players
		    			System.out.println(lineDecode[1] + ": " + lineDecode[2]);
		    			break;
		    		case "6":
		    			// users list
		    			int i = 1;
		    			while (i < lineDecode.length) {
		    				System.out.println(lineDecode[i]);
		    				i++;
		    			}
		    			break;
		    		case "9":
		    			// prepare for connection, send ip, port to user
		    			// accept format: 9 username
		    			// send format: 10 username authcode user ip port
	    				String[] code = {"10", 
	    						client.getName(), 
	    						client.getAuthcode(), 
	    						lineDecode[1], 
	    						client.serverSocket.getInetAddress().toString(), 
	    						"" + client.serverSocket.getLocalPort()
	    				};
	    				client.sendMsgToStream(code);
		    			
		    			break;
		    		case "10":
		    			// accept format: 10 username ip port  
		    			// create client and connect to server
		    			ClientUser newUser = new ClientUser(client, lineDecode[1]);
		    			if (!newUser.connectTo(lineDecode[2], Integer.parseInt(lineDecode[3]))) {
		    				// Fail to connect 
		    				System.out.println("Error: fail to connect to user: " + lineDecode[1]);
		    			} else {
		    				System.out.println("Start private messaging	with " + lineDecode[1]);
		    				newUser.sendMsgToStream(client.username + "\n");
		    				client.userMap.put(lineDecode[1], newUser);
		    			}
		    			break;
		    		}
	    		}
	    		// Server socket
                try {
					// New connection 
					Socket newConnection = client.serverSocket.accept();

	    			System.out.println("Accept connection! ");
	    			ClientUser newUser = new ClientUser(client, "");
	    			newUser.setSocket(newConnection);
	    			System.out.println("Accept connection from " + newUser.name);
	    			client.userMap.put(newUser.name, newUser);
				} catch (Exception e){
				} 
	    		// Polling
				List<String> removeList = new ArrayList<>();
				for (ClientUser user: client.userMap.values()) {
					if (user.running()) {
						user.processMessage();
					}
					if (!user.running()) {
						removeList.add(user.name);
					}
				}
				// remove users that are free
				for (String name: removeList) {
					client.userMap.remove(name);
				}
	    	}
    	} while(true);
	}
	private static void error(String string) {
		System.out.println(string);		
	}

}
