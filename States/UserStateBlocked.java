package assignment.States;

import assignment.User;

public class UserStateBlocked extends UserState {

	public UserStateBlocked(User user, String password) {
		super(user, password);
		this.nBlocked = 3;
	}

	@Override
	public boolean login(String password) {
		return false;
	}

	@Override
	public boolean logoff() {
		return false;
	}

	@Override
	public boolean checkAuthCode(int authcode) {
		return false;
	}

	@Override
	public boolean unblock() {
		user.setCurrState(user.getUnloggedState());
		return true;
	}

	@Override
	public boolean blocked() {
		return true;
	}

}
