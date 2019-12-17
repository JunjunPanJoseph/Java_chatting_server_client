package assignment.States;

import assignment.User;

public class UserStateLogged extends UserState {

	public UserStateLogged(User user, String password) {
		super(user, password);
	}

	@Override
	public boolean login(String password) {
		return false;
	}

	@Override
	public boolean logoff() {
		user.setCurrState(user.getUnloggedState());
		return true;
	}

	@Override
	public boolean checkAuthCode(int authcode) {
		return user.getAuthcode() == authcode;
	}

	@Override
	public boolean unblock() {
		return false;
	}

	@Override
	public boolean blocked() {
		return false;
	}
	

}
