package com.gitlab.jeeto.oboco.common.security;

import java.util.Date;

import javax.enterprise.context.RequestScoped;

import com.gitlab.jeeto.oboco.common.DateHelper;
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
	
	public String getAccessTokenValue(String name) throws ProblemException {
		String secret = getConfiguration().getAsString("security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("security.authentication.accessToken.age", "3600") * 1000L;
		
		UserToken accessToken = new UserToken();
		accessToken.setStartDate(DateHelper.getDate());
		accessToken.setStopDate(new Date(accessToken.getStartDate().getTime() + age));
		accessToken.setName(name);
		
		String accessTokenValue = UserTokenHelper.encodeToken(secret, accessToken);
		
		return accessTokenValue;
	}
	
	public UserToken getAccessToken(String accessTokenValue) throws ProblemException {
		String secret = getConfiguration().getAsString("security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("security.authentication.accessToken.age", "3600") * 1000L;
		
		UserToken accessToken = UserTokenHelper.decodeToken(secret, accessTokenValue);
		
		if(accessToken.getStopDate().getTime() - accessToken.getStartDate().getTime() != age) {
    		throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid: age."));
    	}
		
		return accessToken;
	}
	
	public String getRefreshTokenValue(String name) throws ProblemException {
		String secret = getConfiguration().getAsString("security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("security.authentication.refreshToken.age", "31536000") * 1000L;
		
		UserToken refreshToken = new UserToken();
		refreshToken.setStartDate(DateHelper.getDate());
		refreshToken.setStopDate(new Date(refreshToken.getStartDate().getTime() + age));
		refreshToken.setName(name);
		
		String refreshTokenValue = UserTokenHelper.encodeToken(secret, refreshToken);
		
		return refreshTokenValue;
	}
	
	public UserToken getRefreshToken(String refreshTokenValue) throws ProblemException {
		String secret = getConfiguration().getAsString("security.authentication.secret", "secret");
		Long age = getConfiguration().getAsLong("security.authentication.refreshToken.age", "31536000") * 1000L;
		
		UserToken refreshToken = UserTokenHelper.decodeToken(secret, refreshTokenValue);
		
		if(refreshToken.getStopDate().getTime() - refreshToken.getStartDate().getTime() != age) {
    		throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid: age."));
    	}
		
		return refreshToken;
	}
}
