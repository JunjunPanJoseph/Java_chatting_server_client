package ass2;

import java.io.*;

public class Message {
	private String seperator = "_";
	
	public String type = "";
	public String sender = "";
	public String password = "";
	
	public String token = "";
	public String user = "";
	public String message = "";
	
	public String time = "";
	public String ip = "";
	public String port = "";
	
	public void reset() {
		type = "";
		sender = "";
		password = "";
		
		token = "";
		user = "";
		message = "";
		
		time = "";
		ip = "";
		port = "";
	}
	
	public long getTime() {
		return Long.parseLong(time);
	}
	
	public int getPort() {
		return Integer.parseInt(port);
	}
	public String toString() {
		return " " + type + seperator
			+ " " + sender + seperator
			+ " " + password + seperator
			
			+ " " + token + seperator
			+ " " + user + seperator
			+ " " + message + seperator
			
			+ " " + time + seperator
			+ " " + ip + seperator
			+ " " + port + "\n";				
	}
	
	public boolean setFromString(String line){
		reset();
		try{
			// Global.printToConsole("Set from line: " + line);
			String[] lineSplit = line.split(seperator);
			// Global.printToConsole("leng: " + lineSplit.length);
			type = lineSplit[0].trim();
			sender = lineSplit[1].trim();
			password = lineSplit[2].trim();
			
			token = lineSplit[3].trim();
			user = lineSplit[4].trim();
			message = lineSplit[5].trim();
			
			time = lineSplit[6].trim();
			ip = lineSplit[7].trim();
			port = lineSplit[8].trim();
			
			return true;
		} catch (Exception e) {
			reset();
			return false;
		}
		
	}
	public static boolean exist(String s) {
		return s.trim().compareTo("") != 0;
	}
	public void show() {
		Global.printToConsole("type: [" + type + "]");
		Global.printToConsole("sender: [" + sender + "]");
		Global.printToConsole("password: [" + password + "]");
		
		Global.printToConsole("token: [" + token + "]");
		Global.printToConsole("user: [" + user + "]");
		Global.printToConsole("message: [" + message + "]");
		
		Global.printToConsole("time: [" + time + "]");
		Global.printToConsole("ip: [" + ip + "]");
		Global.printToConsole("port: [" + port + "]");
		
	}
	
}
