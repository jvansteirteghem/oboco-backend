package com.gitlab.jeeto.oboco.common.security;

import java.security.Principal;

import com.gitlab.jeeto.oboco.api.v1.user.User;

public class UserPrincipal implements Principal {
	private User user;
	public UserPrincipal(User user) {
		super();
		this.user = user;
	}
	@Override
	public String getName() {
		return user.getName();
	}
	
	public User getUser() {
		return user;
	}
}
