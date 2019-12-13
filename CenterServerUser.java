package ass2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class CenterServerUser extends User {
	private String password;
	private String token;
	
	private Date timeLatestActivate;
	private Date timeBlocked;
	
	private int nLoginFailed;
	private int block_duration;
	private int timeout;
	
	private Set<String> blockedList;
	
	public CenterServerUser(
			Server server, 
			String name, 
			String password, 
			int block_duration, 
			int timeout) {
		super(server, name);
		this.password = password;		
		this.token = "-1";
		
		this.timeLatestActivate = new Date(0);
		this.timeBlocked = new Date(0);

		this.nLoginFailed = 0;
		this.block_duration = 1000 * block_duration;
		this.timeout = 1000 * timeout;
		
		this.blockedList = new HashSet<>();
	}
	
	@Override
	public boolean loginCheck(Message message, DataOutputStream out) {
		Date currTime = new Date();
		if (message != null) {
			Global.printToConsole("login check receive msg: ");
		}
		System.out.println("curr connect stage: " + isConnected());
		if (isConnected()) {
			// Repeated login
			Global.error(out, "Your account is online!");
			return false;
		} else if (checkLoginBlocked(currTime)) {
			Global.error(out, "Your account is blocked due to multiple login failures. Please try again later.");
			return false;
		} else if (!password.equals(message.password)) {
			// Wrong password
			nLoginFailed++;
			if (nLoginFailed == 3) {
				nLoginFailed = 0;
				timeBlocked = currTime;
				Global.error(out, "Invalid Password. Your account has been blocked. Please try again later.");
			} else {
				Global.error(out, "Invalid Password.");
			}
			return false;
		} else {
			// Success
			nLoginFailed = 0;			
			return true;
		}
	}
	
	
	@Override
	public boolean allow(User user) {
		if (user == null) {
			// Message from server
			return true;
		}
		return !blockedList.contains(user.getName());
	}
	@Override
	public void handleSingleMessage(Message message) {
		if (!Global.checkMessageValid(message, name, token, out)) {
			return;
		}
		// latest activate time
		timeLatestActivate = new Date();
		// switch with valid type code
		Message sendMessage = new Message();
		switch (message.type) {
		case "ip":
			this.ip = message.ip;
			this.port = message.port;
			break;
		case "logout":
			closeSocket();
			break;
		case "message":
			// Check receiver name
			if (Global.checkMessageNameValid(message, null, server.getUsers(), out)) {
				if (!this.sendMessageToUser(server.getUsers().get(message.user), message)) {
					// Blocked
					Global.error(out, "Your message could not be delivered as the recipient has blocked you.");
				}
			}
			break;
		case "broadcast":
			// Only send to only users
			message.type = "message";
			if (!onlineBroadcast(this, message)) {
				Global.error(out, "Your message could not be delivered to some recipients.");
			}
			break;
		case "whoelse":
			sendMessage.type = "whoelse";
			for (User user: server.getUsers().values()) {
				if (user == this)
					continue;
				if (user.isConnected()) {
					sendMessage.message += user.getName() + " ";
				}
			}
			Global.sendMessage(out, sendMessage);
			break;
		case "whoelsesince":
			sendMessage.type = "whoelse";
			for (User user: server.getUsers().values()) {
				if (user == this)
					continue;
				if (user.isConnected()) {
					sendMessage.message += user.getName() + " ";
				} else if (user.getActivateTime() + 1000 * message.getTime() 
							> this.timeLatestActivate.getTime()) {
					sendMessage.message += user.getName() + " ";
				}
			}
			Global.sendMessage(out, sendMessage);
			break;
		case "block":
			// Check user name
			if (Global.checkMessageNameValid(message, null, server.getUsers(), out)) {
				// Name cannot be the same
				if (message.user.equals(name)) {
					Global.error(out, "Error. You cannot block youself");
				} else if (this.blockedList.contains(message.user)) {
					Global.error(out, "Error. yoda was blocked");
				} else {
					this.blockedList.add(message.user);
					Global.error(out, message.user + " is blocked");
				}
			}
			break;
		case "unblock":
			// Check user name
			if (Global.checkMessageNameValid(message, null, server.getUsers(), out)) {
				// Name cannot be the same
				if (message.user.equals(name)) {
					Global.error(out, "Error. You cannot unblock youself");
				} else if (!this.blockedList.contains(message.user)) {
					Global.error(out, "Error. yoda was not blocked");
				} else {
					this.blockedList.remove(message.user);
					Global.error(out, message.user + " is unblocked");
				}
			}
			break;
		case "startprivate":
			if (Global.checkMessageNameValid(message, null, server.getUsers(), out)) {
				User peerUser = server.getUsers().get(message.user);
				if (!peerUser.isConnected()){
					Global.error(out, "Could not start private as the recipient is not online.");
				} else if (!peerUser.allow(this)) {
					// Blocked
					Global.error(out, "Could not start private as the recipient has blocked you.");
				} else {
					message.ip = peerUser.ip;
					message.port = peerUser.port;
					Global.sendMessage(out, message);
				}
			}
			break;
		default:
			// Invalid message
			Global.error(out, "Error. Invalid message: " + message.toString());
			break;
		}
	}
	public boolean onlineBroadcast(User broadcaster, Message message) {
		boolean result = true;
		for (User user: server.getUsers().values()) {
			if (user == this) {
				continue;
			}
			if (user.isConnected()) {
				if (!user.addMessageToList(broadcaster, message)) {
					result = false;
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean setupSocket(Socket socket, BufferedReader in, DataOutputStream out) {
		boolean result = super.setupSocket(socket, in, out);
		
		if (result) {
			this.timeLatestActivate = new Date();
			Random random = new Random();
			this.token = "" + random.nextLong();
			// Send message to client
			Message message = new Message();
			message.type = "login";
			message.token = token;
			message.sender = name;
			Global.sendMessage(out, message);
			
			Message boardcastMsg = new Message();
			boardcastMsg.type = "error";
			boardcastMsg.message = name + " logged in";;
			onlineBroadcast(null, boardcastMsg);
		}
		return result;
	}
	@Override
	public boolean closeSocket() {
		timeLatestActivate = new Date();
		Message message = new Message();
		message.type = "error";
		message.message = name + " logged out";;
		onlineBroadcast(null, message);
		return super.closeSocket();
	}
	/**
	 * True: is blocked
	 * False: not blocked
	 * @param time
	 * @param out
	 * @return
	 */
	
	private boolean checkLoginBlocked(Date time) {
		return (time.getTime() < timeBlocked.getTime() + block_duration);
	}
	/**
	 * True: is Timeout
	 * False: not timeout
	 * @param time
	 * @return
	 */
	@Override 
	public boolean checkTimeout(Date time) {
		/*
		System.out.println("CurrTime: " + time.getTime());
		System.out.println("LatestActivate: " + timeLatestActivate.getTime());
		System.out.println("timeout: " + timeout);
		*/
		boolean result = (time.getTime() > timeLatestActivate.getTime() + timeout) ;
		if (result) {
			Global.error(out, "Timeout");
		}
		return result;
	}
	@Override
	public long getActivateTime() {
		return timeLatestActivate.getTime();
	}
}
