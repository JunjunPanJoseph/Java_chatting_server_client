package assignment;

import java.io.*;
import java.net.*;
import java.util.*;


import assignment.States.*;

public class User {
	private Server server;
	private String name;	
	private int authCode;
	
	private UserState currState;
	private UserStateBlocked blockState;
	private UserStateUnlogged unloggedState;
	private UserStateLogged loggedState;
	
	private Date logoutTime;
	private Date blockEndTime;
	private Date latestAction;
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private DataInputStream datainStream;
	private DataOutputStream dataoutStream;
	
	public Set<String> blackList;
	private List<String> messageQueue;
	
	
	public User(Server server, String name, String password) {
		this.server = server;
		
		this.name = name;
		this.logoutTime = new Date();
		this.blockEndTime = new Date();
		this.latestAction = new Date();
		
		this.logoutTime.setTime(0);
		
		
		this.authCode = -1;
		
		this.blockState = new UserStateBlocked(this, password);
		this.unloggedState = new UserStateUnlogged(this, password);
		this.loggedState = new UserStateLogged(this, password);
		
		this.currState = this.unloggedState;
		
		this.blackList = new HashSet<>();
		this.messageQueue = new ArrayList<>();
	}
	
	public boolean login(
			String password, 
			Socket clientSocket, 
			InputStream inputStream, 
			OutputStream outputStream) throws IOException {
		DataOutputStream tmpDataoutStream = new DataOutputStream(outputStream);
		
		if (currState == loggedState) {
			String[] code = {"1", "-1"};
			tmpDataoutStream.writeBytes(EncodeDecode.encode(code));
		}
		if (currState == blockState) {
			Date currTime = new Date();
			if (currTime.after(blockEndTime)) {
				currState.unblock();
			}
		}
		if (currState.login(password)) {
			// Login success
			
			this.socket = clientSocket;
			this.inputStream = inputStream;
			this.outputStream = outputStream;
			this.dataoutStream = new DataOutputStream(outputStream);
			this.datainStream = new DataInputStream(inputStream);
			String[] code = {"2", ""+this.authCode};
			// dataoutStream.writeChars(EncodeDecode.encode(code));
			sendMsgToStream(code);
			boardCast("Server", name + " logged in", true);
			
			this.logoutTime = new Date();
			this.blockEndTime = new Date();
			this.latestAction = new Date();
			
			return true;
		} else {
			// Login fail
			System.out.println("login failed");
			String[] code = {"1", ""+currState.getNBlocked()};
			tmpDataoutStream.writeBytes(EncodeDecode.encode(code));
			return false;
		}
	}
	public boolean sendMsg(String username, String message) {
		if (blackList.contains(username)) {
			return false;
		} else {
			String[] code = {"4", username, message};
			messageQueue.add(EncodeDecode.encode(code));
			return true;
		}
	}
	private void sendAllMsg() {
		// Send all message in queue
		for(String msg: messageQueue) {
			sendMsgToStream(msg);
		}
		messageQueue.clear();
	}
	private boolean boardCast(String name, String message, boolean reqiredLogin) {
		boolean retval = true;
		for (User user: server.getUserMap().values()) {
			if (user == this) {
				continue;
			}
			if (reqiredLogin && !user.online()) {
				continue;
			}
			if (!user.sendMsg(name, message)) {
				retval = false;
			}
		}
		return retval;
	}
	public void processMessage() {
		if (!this.online()) {
			// Offline
			return;
		}
		Map<String, User> map;
		Date now = new Date();
		now.setTime(now.getTime() - server.getTimeout() * 1000);
		if (now.after(latestAction)) {
			// Logout
			sendErrorMsg("Timeout.");
			try {
				logout();
			} catch (IOException e) {
			}
		} else {
			// Send curr message
			sendAllMsg();
			// Handle new request
			try {
				// To avoid blocking
				if (datainStream.available() > 0) {
					// Have messages from client
					
					this.latestAction = new Date();
					
					String line = datainStream.readLine();
					System.out.println(this.name + " receive: " + line);
					String[] args = EncodeDecode.decode(line);
					if (args.length < 3) {
						sendErrorMsg("Error: invalid encoding: [" + line + "]");
					} else if(args[1].compareTo(this.name) != 0 || args[2].compareTo("" + this.authCode) != 0) {
						sendErrorMsg("Error: invalid username / authcode: [" + line + "]");
					} else {
						switch (args[0]) {
						case "3":
							logout();
							
							break;
						case "4":
							// send message
							if (args.length != 5) {
								sendErrorMsg("Error: invalid arg num - should be 5, but it is " + args.length);
							} else if (!server.getUserMap().containsKey(args[3])){
								sendErrorMsg("Error: invalid user name " + args[3]);
							} else {
								if (!server.getUserMap().get(args[3]).sendMsg(this.name, args[4])) {
									// Black list
									sendErrorMsg("Your message could not be delivered as the recipient has blocked you");
								}
							}
							break;
						case "5":
							// boardcast message
							if (args.length != 4) {
								sendErrorMsg("Error: invalid arg num - should be 4, but it is " + args.length);
							} else {
								if (!boardCast(this.name, args[3], true)) {
									// Black list
									sendErrorMsg("Your message could not be delivered to some recipients");
								}
							}
							break;
						case "6":
							Date currTime = new Date();
							long ms = 0;
							if (args.length == 4) {
								ms = 1000 * Integer.parseInt(args[3]);
							}
							currTime.setTime(currTime.getTime() - ms);
							List<String> userList = new ArrayList<>();
							userList.add("6");
							for (User user: server.getUserMap().values()) {
								if (user == this) {
									continue;
								}
								if (user.online() || user.getLogoutTime().after(currTime)) {
									userList.add(user.getName());
								}
								
							}
							sendMsgToStream(userList);
							
							
							
							break;
	
						case "7":
							// block
							if (args.length != 4 || !server.getUserMap().containsKey(args[3])) {
								sendErrorMsg("Error. Invalid user");
							} else if (args[3].compareTo(this.name) == 0) {
								sendErrorMsg("Error. Cannot block self");
							} else if (this.blackList.contains(args[3])){
								sendErrorMsg("Error. "+ args[3] + " already blocked");
							} else {
								this.blackList.add(args[3]);
								sendErrorMsg(args[3] + " is blocked");
							}
							break;
						case "8":
							// unblock
							if (args.length != 4 || !server.getUserMap().containsKey(args[3])) {
								sendErrorMsg("Error. Invalid user");
							} else if (args[3].compareTo(this.name) == 0) {
								sendErrorMsg("Error. Cannot unblock self");
							} else if (!this.blackList.contains(args[3])){
								sendErrorMsg("Error. "+ args[3] + " was not blocked");
							} else {
								this.blackList.remove(args[3]);
								sendErrorMsg(args[3] + " is unblocked");
							}
							break;
						case "9":
							// start private
							String userName = args[3];
							map = server.getUserMap();
							if (!map.containsKey(userName)) {
								sendErrorMsg("Error: user name not exist");
							} else if (!map.get(userName).online()) {
								sendErrorMsg("Error: user off line");
							} else if (map.get(userName).blackList.contains(userName)) {
								sendErrorMsg("Error: user off line");
							} else {
								// acq ip from client
								String[] code = {"9", this.name};
								map.get(userName).sendMsgToStream(code);
							}
							break;
						case "10":
							map = server.getUserMap();
							if (!map.containsKey(args[3])) {
							} else if (!map.get(args[3]).online()) {
							} else {
								String[] tmpCode = {"10", this.name, args[4], args[5]};
								map.get(args[3]).sendMsgToStream(tmpCode);
							}
							break;
						default:
							sendErrorMsg("Error: invalid encoding: [" + line + "]");
							break;
						}
					}	
					
				}
			} catch (IOException e) {
			}
		}
		// End of checking active time
		
	}

	public void logout() throws IOException {
		// logout
		this.currState.logoff();
		this.socket.close();
		this.socket = null;
		boardCast("Server", name + " logged out", true);
		this.logoutTime = new Date();
	}


	private Date getLogoutTime() {
		// TODO Auto-generated method stub
		return logoutTime;
	}
	public void sendMsgToStream(List<String> userList) {
		try {
			String line = EncodeDecode.encode(userList);
			System.out.println("Server send to " + this.name + ": " + line);
			dataoutStream.writeBytes(line);
		} catch (IOException e) {
			this.show();
			System.out.println("Error: IO exception in sendMsgToStream");
		}
	}
	public void sendMsgToStream(String[] args) {
		try {
			String line = EncodeDecode.encode(args);
			System.out.println("Server send to " + this.name + ": " + line);
			dataoutStream.writeBytes(line);
		} catch (IOException e) {
			this.show();
			System.out.println("Error: IO exception in sendMsgToStream");
		}
	}
	public void sendMsgToStream(String line) {
		try {
			System.out.println("Server send: " + line);
			dataoutStream.writeBytes(line);
		} catch (IOException e) {
			this.show();
			System.out.println("Error: IO exception in sendMsgToStream");
		}
	}
	public void sendErrorMsg(String errorMsg) {
		String[] args = {"-1", errorMsg};
		sendMsgToStream(args);
	}
	
	public String getName() {
		return name;
	}
	
	
	/**
	 * @return the currState
	 */
	public UserState getCurrState() {
		return currState;
	}

	/**
	 * @param currState the currState to set
	 */
	public void setCurrState(UserState currState) {
		this.currState = currState;
	}

	/**
	 * @return the blockState
	 */
	public UserStateBlocked getBlockState() {
		return blockState;
	}

	/**
	 * @return the unloggedState
	 */
	public UserStateUnlogged getUnloggedState() {
		return unloggedState;
	}

	/**
	 * @return the loggedState
	 */
	public UserStateLogged getLoggedState() {
		return loggedState;
	}
	
	/**
	 * @param blockEndTime the blockEndTime to set
	 */
	public void setBlockEndTime(Date blockEndTime) {
		this.blockEndTime = blockEndTime;
	}

	public void show() {
		System.out.println("Name: " + name);
		
	}
	
	/**
	 * @return the server
	 */
	public Server getServer() {
		return server;
	}

	public void randAutoCode() {
		Random rand = new Random();
		this.authCode = rand.nextInt();
	}

	public int getAuthcode() {
		return this.authCode;
	}
	public boolean online() {
		return this.currState == this.loggedState;
	}
}
