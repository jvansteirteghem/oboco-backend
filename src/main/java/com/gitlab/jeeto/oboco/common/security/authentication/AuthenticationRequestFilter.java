package com.gitlab.jeeto.oboco.common.security.authentication;

import java.io.IOException;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.api.v1.user.UserService;
import com.gitlab.jeeto.oboco.common.exception.ProblemDto;

@Authentication
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationRequestFilter implements ContainerRequestFilter {
	private static Logger logger = LoggerFactory.getLogger(AuthenticationRequestFilter.class.getName());
	@Inject
	UserService userService;
	@Inject
	UserTokenService tokenService;
	@Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    	Authentication authentication = null;
    	
    	authentication = resourceInfo.getResourceMethod().getAnnotation(Authentication.class);
        if (authentication != null) {
            handleAuthentication(authentication, requestContext);
            return;
        }
        
        authentication = resourceInfo.getResourceClass().getAnnotation(Authentication.class);
        if (authentication != null) {
        	handleAuthentication(authentication, requestContext);
            return;
        }
    }
    
    private void handleAuthentication(Authentication authentication, ContainerRequestContext requestContext) {
    	try {
        	if(authentication.type().equals("BASIC")) {
	            handleBasicAuthentication(requestContext);
        	} else if(authentication.type().equals("BEARER")) {
        		handleBearerAuthentication(requestContext);
        	}
    	} catch (Exception e) {
			logger.error("Error.", e);
			
			ResponseBuilder responseBuilder = Response.status(500);
			responseBuilder.entity(new ProblemDto(500, "PROBLEM", "Problem."));
    		
			requestContext.abortWith(responseBuilder.build());
			
    		return;
		}
    }
    
    private void handleBasicAuthentication(ContainerRequestContext requestContext) throws Exception {
    	String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if(header != null) {
        	if(header.startsWith("Basic")) {
				String encodedString = header.substring("Basic".length()).trim();
		    	String decodedString;
		    	try {
		    		decodedString = new String(Base64.getDecoder().decode(encodedString), "UTF-8");
				} catch (Exception e) {
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Basic realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
					
					requestContext.abortWith(responseBuilder.build());
					
					return;
				}
		    	
		    	StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");
				String name = tokenizer.nextToken();
				String password = tokenizer.nextToken();
				
				User user = userService.getUserByNameAndPassword(name, password);
				
				if(user == null) {
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Basic realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
		    		
					requestContext.abortWith(responseBuilder.build());
					
		    		return;
				}
				
		    	UserPrincipal userPrincipal = new UserPrincipal(user.getName(), user.getRoles());
		    	
		    	UserSecurityContext userSecurityContext = new UserSecurityContext(requestContext.getSecurityContext(), userPrincipal);
		        requestContext.setSecurityContext(userSecurityContext);
        	}
        }
    }
    
    private void handleBearerAuthentication(ContainerRequestContext requestContext) throws Exception {
    	String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if(header != null) {
        	if(header.startsWith("Bearer")) {
		    	String idTokenValue = header.substring("Bearer".length()).trim();
		    	UserToken idToken;
		    	try {
		    		idToken = tokenService.getIdToken(idTokenValue);
				} catch (Exception e) {
					logger.info("The user is not authenticated: invalid userToken: " + e.getMessage());
					
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Bearer realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
					
					requestContext.abortWith(responseBuilder.build());
					
					return;
				}
		    	
		    	User user = userService.getUserByName(idToken.getName());
				
				if(user == null) {
					logger.info("The user is not authenticated: invalid userToken: name.");
					
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Bearer realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
		    		
					requestContext.abortWith(responseBuilder.build());
					
		    		return;
				}
				
				if(user.getUpdateDate() == null || user.getUpdateDate().compareTo(idToken.getStartDate()) >= 0) {
					logger.info("The user is not authenticated: invalid userToken: startDate.");
					
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Bearer realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
		    		
					requestContext.abortWith(responseBuilder.build());
					
		    		return;
				}
		    	
		    	UserPrincipal userPrincipal = new UserPrincipal(user.getName(), user.getRoles());
		    	
		    	UserSecurityContext userSecurityContext = new UserSecurityContext(requestContext.getSecurityContext(), userPrincipal);
		        requestContext.setSecurityContext(userSecurityContext);
        	}
        }
    }
}