package ass2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class Global {
	public static void printToConsole(String str) {
		System.out.println(str);
	}
	public static Message getMessageFromInputBuffer(int available, BufferedReader in, DataOutputStream out) {
		if (available <= 0) {
			return null;
		}
		Message message = new Message();
		try {
			String inputLine = in.readLine();
			// System.out.println("Line: " + inputLine);
			if (message.setFromString(inputLine)) {
				return message;
			} else {
				// invalid format
				String errorMsg = "Error. Invalid input line: [" + inputLine + "]";
				printToConsole(errorMsg);
				error(out, errorMsg);
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}
	public static boolean sendMessage(DataOutputStream out, Message message){
		if (out == null) {
			// printToConsole("out stream is null");
			return false;
		}
		try {
			out.writeBytes(message.toString());
			return true;
		} catch (IOException e) {
			printToConsole("Error. Fail to send " + message.type + " message");
			return false;
		}
	}
	public static void error(DataOutputStream out, String errorMsg){
		if (out == null) {
			// printToConsole("out stream is null");
			return;
		}
		Message message = new Message();
		message.type = "error";
		message.message = errorMsg;
		sendMessage(out, message);
	}
	/**
	 * True: valid
	 * False: invalid
	 * @param message
	 * @param senderName
	 * @param token
	 * @param out
	 * @return
	 */
	public static boolean checkMessageValid(
			Message message, String senderName, 
			String token, DataOutputStream out) {
		if (token != null) {
			if (!message.token.equals(token)) {
				error(out, "Error. Invalid token");
				return false;
			}
		}
		if (senderName != null) {
			if (!message.sender.equals(senderName)) {
				error(out, "Error. Invalid sender name");
				return false;
			}
		}
		return true;
	}
	/**
	 * True: valid
	 * False: invalid
	 * @param message
	 * @param senderName
	 * @param token
	 * @param out
	 * @return
	 */
	public static boolean checkMessageNameValid(
		Message message, Map<String, User> senderName, 
		Map<String, User> userName, DataOutputStream out) {
		
		if (senderName != null) {
			if (!senderName.containsKey(message.sender)) {
				error(out, "Error. Sender name not exist");
				return false;
			}
		}
		if (userName != null) {
			if (!userName.containsKey(message.user)) {
				error(out, "Error. User name not exist");
				return false;
			}
		}
		if (message.sender.equals(message.user)) {
			error(out, "Error. User name cannot be the same as sender name.");
			return false;
		}
		return true;
	}
	public static void errorInvalidMessage(DataOutputStream out, Message message) {
		String msg = "Error. Invalid message format ";
		if (message != null) {
			msg += message.toString();
		}
		if (out != null) {
			error(out, msg);
		} else {
			Global.printToConsole(msg);
		}
	}
	
}
