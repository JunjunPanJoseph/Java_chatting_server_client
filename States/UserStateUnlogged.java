package assignment.States;

import assignment.User;

import java.util.Date;

public class UserStateUnlogged extends UserState {

	public UserStateUnlogged(User user, String password) {
		super(user, password);
	}

	@Override
	public boolean login(String password) {
		if (this.password.compareTo(password) == 0) {
			this.nBlocked = 0;
			user.randAutoCode();
			this.user.setCurrState(user.getLoggedState());
			return true;
		} else {
			this.nBlocked++;
			if (this.nBlocked == 3) {
				this.nBlocked = 0;
				this.user.setCurrState(user.getBlockState());
				Date curr = new Date();
				curr.setTime(curr.getTime() + user.getServer().getBlockDuration() * 1000);
				this.user.setBlockEndTime(curr);
			}
			return false;
		}
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
		return false;
	}

	@Override
	public boolean blocked() {
		return false;
	}

}
