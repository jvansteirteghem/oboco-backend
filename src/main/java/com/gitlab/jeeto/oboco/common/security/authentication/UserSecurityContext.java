package com.gitlab.jeeto.oboco.common.security.authentication;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class UserSecurityContext implements SecurityContext  {
	private SecurityContext securityContext;
	private UserPrincipal userPrincipal;
	
	public UserSecurityContext(SecurityContext securityContext, UserPrincipal userPrincipal) {
		super();
		this.securityContext = securityContext;
		this.userPrincipal = userPrincipal;
	}
	
	@Override
	public String getAuthenticationScheme() {
		return securityContext.getAuthenticationScheme();
	}

	@Override
	public Principal getUserPrincipal() {
		return userPrincipal;
	}

	@Override
	public boolean isSecure() {
		return securityContext.isSecure();
	}

	@Override
	public boolean isUserInRole(String role) {
		return userPrincipal.getRoles().contains(role);
	}
}
