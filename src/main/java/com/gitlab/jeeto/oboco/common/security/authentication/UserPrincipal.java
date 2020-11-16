package com.gitlab.jeeto.oboco.common.security.authentication;

import java.security.Principal;
import java.util.List;

public class UserPrincipal implements Principal {
	private String name;
	private List<String> roles;
	public UserPrincipal(String name, List<String> roles) {
		super();
		this.name = name;
		this.roles = roles;
	}
	@Override
	public String getName() {
		return name;
	}
	public List<String> getRoles() {
		return roles;
	}
}
