package assignment;

import java.io.*;
import java.net.*;
import java.util.*;


public class Server {
	private int serverPort;
    private int blockDuration;
    private int timeout;

	private ServerSocket serverSocket; 
    
	private Map<String, User> userMap;
    
    
	public Server(int serverPort, int blockDuration, int timeout) {
		this.serverPort = serverPort;
		this.blockDuration = blockDuration;
		this.timeout = timeout;
		this.serverSocket = null;
		this.userMap = new HashMap<>();
	}
	
	public boolean addUser(User user) {
		if (userMap.containsKey(user.getName())) {
			return false;
		} else {
			userMap.put(user.getName(), user);
			return true;
		}
	}
	public boolean serverStart() {
		try {
			this.serverSocket = new ServerSocket(5001);
			return true;
		} catch (IOException e) {
			serverSocket = null;
			return false;
		}
		
	}
	public Map<String, User> getUserMap(){
		return this.userMap;
	}
	public int getBlockDuration() {
		return blockDuration;
	}
	public int getTimeout() {
		return timeout;
	}
	public void run() throws SocketException {
		// Running server
		if (!serverStart()) {
			System.out.println("Fail to create socket!");
			System.exit(1);
		}
		System.out.println("Server start running");
		System.out.println("Address: " + serverSocket.getInetAddress());
		System.out.println("Port: " + serverSocket.getLocalPort());
		// Accept timeout
		serverSocket.setSoTimeout(1000);
		while(true) {
			try {
				// New connection 
				Socket clientSocket = serverSocket.accept();
				// System.out.println("accept new socket");
				InputStream inputStream = clientSocket.getInputStream();
				OutputStream outputstream = clientSocket.getOutputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				DataOutputStream out = new DataOutputStream(outputstream);
				
				// Improve: encoding messages
				String line = reader.readLine();
				// System.out.println("receive: " + line);
				String[] lineSplit = EncodeDecode.decode(line);
				if (lineSplit.length == 3 && lineSplit[0].compareTo("0") == 0) {
					String name = lineSplit[1];
					if (userMap.containsKey(name)) {
						// Valid user name
						String password = lineSplit[2];
						// If login failed
						if (!userMap.get(name).login(password, clientSocket, inputStream, outputstream)) {
							clientSocket.close();
						}
					} else {
						// Invalid user name
						String[] code = {"1", "0"};
						out.writeBytes(EncodeDecode.encode(code));
					}
				} else {
					// Invalid message
					System.out.println("Invalid connect message");
					String[] code = {"-1", "User name not exist"};
					out.writeBytes(EncodeDecode.encode(code));
					// clientSocket.close();
				}
			} catch (SocketTimeoutException  e) {
				// Polling
				//System.out.println("Socket Time out - stop block");
				for (User user: this.userMap.values()) {
					user.processMessage();
				}
				
			} catch (Exception e){
			} 
			
		}
		
		
	}
	
	
	public static void main(String[] args) throws SocketException {
		int serverPort;
        int blockDuration;
        int timeout;
        if(args.length != 3){
            System.out.println("Usage: java Server server_port block_duration timeout\n");
            // System.exit(1);
            serverPort = 5002;
    		blockDuration = 30;
    		timeout = 120;
        } else {
            serverPort = Integer.parseInt(args[0]);
            blockDuration = Integer.parseInt(args[1]);
            timeout = Integer.parseInt(args[2]);
        }
        
        Server server = new Server(serverPort, blockDuration, timeout);
        
        /**
         *  Load users to memory
         *  Pos: faster
         *  Neg: allocating unnecessary memory
         */
        String accountFile = "credentials.txt";
        File file = new File(accountFile);
        if (file.isFile() && file.exists()) {
            try {
	            InputStreamReader read = new InputStreamReader(new FileInputStream(file));
	            BufferedReader bufferedReader = new BufferedReader(read);
	            String line = null;
	
				while ((line = bufferedReader.readLine()) != null)
				{
				    String[] splitedLine = line.split(" ");
				    server.addUser(new User(server, splitedLine[0], splitedLine[1]));
				}
	            bufferedReader.close();
	            read.close();
			} catch (IOException e) {
				System.out.println("Error: IO exception!" );
				System.exit(1);
			}
            // Server running...
        	server.run();
        	// Server Stop
        	
        } else {
        	System.out.println("Error: account file \"" + accountFile + "\" not exist!" );
        	System.exit(1);
        }
        
	}
}
