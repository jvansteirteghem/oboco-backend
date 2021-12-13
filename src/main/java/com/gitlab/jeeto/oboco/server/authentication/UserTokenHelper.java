package com.gitlab.jeeto.oboco.server.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.gitlab.jeeto.oboco.problem.Problem;
import com.gitlab.jeeto.oboco.problem.ProblemException;

public class UserTokenHelper {
	public static String encodeToken(String secret, UserToken token) throws ProblemException {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
		    String value = JWT.create()
		        .withIssuer("oboco")
		        .withIssuedAt(token.getStartDate())
		        .withExpiresAt(token.getStopDate())
		        .withSubject(token.getName())
		        .sign(algorithm);
		    return value;
		} catch(Exception e){
			throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid."));
		}
	}
	
	public static UserToken decodeToken(String secret, String tokenValue) throws ProblemException {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
		    JWTVerifier verifier = JWT.require(algorithm)
		        .withIssuer("oboco")
		        .build();
		    DecodedJWT jwt = verifier.verify(tokenValue);
		    
		    UserToken token = new UserToken();
		    token.setStartDate(jwt.getIssuedAt());
		    token.setStopDate(jwt.getExpiresAt());
		    token.setName(jwt.getSubject());
		    return token;
		} catch(Exception e){
			throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid."));
		}
	}
}
