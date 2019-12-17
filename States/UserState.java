package assignment.States;

import assignment.User;

public abstract class UserState {
	protected User user;
	protected String password;
	protected int nBlocked;
	
	public UserState(User user, String password) {
		this.user = user;
		this.password = password;
		this.nBlocked = 0;
	}
	
	
	public abstract boolean login(String password); 
	public abstract boolean logoff(); 
	public abstract boolean checkAuthCode(int authcode);
	public abstract boolean unblock();
	public abstract boolean blocked();
	public int getNBlocked() {
		return nBlocked;
	}
}
