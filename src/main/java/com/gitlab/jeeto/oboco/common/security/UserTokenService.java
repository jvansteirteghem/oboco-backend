package com.gitlab.jeeto.oboco.common.security;

import java.util.Date;

import javax.enterprise.context.RequestScoped;

import com.gitlab.jeeto.oboco.common.configuration.Configuration;
import com.gitlab.jeeto.oboco.common.configuration.ConfigurationManager;
import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class UserTokenService {
	private Configuration configuration;
	
	private Configuration getConfiguration() {
		if(configuration == null) {
			ConfigurationManager configurationManager = ConfigurationManager.getInstance();
			configuration = configurationManager.getConfiguration();
		}
		return configuration;
	}
	
	public String getIdTokenValue(String name) throws ProblemException {
		String secret = getConfiguration().getAsString("application.security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("application.security.authentication.idToken.age", "3600") * 1000L;
		
		UserToken idToken = new UserToken();
		idToken.setStartDate(new Date());
		idToken.setStopDate(new Date(idToken.getStartDate().getTime() + age));
		idToken.setName(name);
		
		String idTokenValue = UserTokenHelper.encodeToken(secret, idToken);
		
		return idTokenValue;
	}
	
	public UserToken getIdToken(String idTokenValue) throws ProblemException {
		String secret = getConfiguration().getAsString("application.security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("application.security.authentication.idToken.age", "3600") * 1000L;
		
		UserToken idToken = UserTokenHelper.decodeToken(secret, idTokenValue);
		
		if(idToken.getStopDate().getTime() - idToken.getStartDate().getTime() != age) {
    		throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid: age."));
    	}
		
		return idToken;
	}
	
	public String getRefreshTokenValue(String name) throws ProblemException {
		String secret = getConfiguration().getAsString("application.security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("application.security.authentication.refreshToken.age", "31536000") * 1000L;
		
		UserToken refreshToken = new UserToken();
		refreshToken.setStartDate(new Date());
		refreshToken.setStopDate(new Date(refreshToken.getStartDate().getTime() + age));
		refreshToken.setName(name);
		
		String refreshTokenValue = UserTokenHelper.encodeToken(secret, refreshToken);
		
		return refreshTokenValue;
	}
	
	public UserToken getRefreshToken(String refreshTokenValue) throws ProblemException {
		String secret = getConfiguration().getAsString("application.security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("application.security.authentication.refreshToken.age", "31536000") * 1000L;
		
		UserToken refreshToken = UserTokenHelper.decodeToken(secret, refreshTokenValue);
		
		if(refreshToken.getStopDate().getTime() - refreshToken.getStartDate().getTime() != age) {
    		throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid: age."));
    	}
		
		return refreshToken;
	}
}
