package com.gitlab.jeeto.oboco.server.authentication;

import java.io.IOException;
import java.lang.reflect.Method;
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

import com.gitlab.jeeto.oboco.api.ProblemDto;
import com.gitlab.jeeto.oboco.database.Graph;
import com.gitlab.jeeto.oboco.database.GraphHelper;
import com.gitlab.jeeto.oboco.database.user.User;
import com.gitlab.jeeto.oboco.database.user.UserService;

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
    	Method resourceMethod = resourceInfo.getResourceMethod();
    	if(resourceMethod != null) {
    		Authentication authentication = resourceMethod.getAnnotation(Authentication.class);
	        if (authentication != null) {
	            handleAuthentication(authentication, requestContext);
	            return;
	        }
    	}
        
    	Class<?> resourceClass = resourceInfo.getResourceClass();
    	if(resourceClass != null) {
    		Authentication authentication = resourceClass.getAnnotation(Authentication.class);
	        if (authentication != null) {
	        	handleAuthentication(authentication, requestContext);
	            return;
	        }
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
				
				Graph graph = GraphHelper.createGraph("(rootBookCollection)");
				
				User user = userService.getUserByNameAndPassword(name, password, graph);
				
				if(user == null) {
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Basic realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
		    		
					requestContext.abortWith(responseBuilder.build());
					
		    		return;
				}
				
		    	UserPrincipal userPrincipal = new UserPrincipal(user);
		    	
		    	UserSecurityContext userSecurityContext = new UserSecurityContext(requestContext.getSecurityContext(), userPrincipal);
		        requestContext.setSecurityContext(userSecurityContext);
        	}
        }
    }
    
    private void handleBearerAuthentication(ContainerRequestContext requestContext) throws Exception {
    	String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if(header != null) {
        	if(header.startsWith("Bearer")) {
		    	String accessTokenValue = header.substring("Bearer".length()).trim();
		    	UserToken accessToken;
		    	try {
		    		accessToken = tokenService.getAccessToken(accessTokenValue);
				} catch (Exception e) {
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Bearer realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
					
					requestContext.abortWith(responseBuilder.build());
					
					return;
				}
		    	
		    	Graph graph = GraphHelper.createGraph("(rootBookCollection)");
		    	
		    	User user = userService.getUserByName(accessToken.getName(), graph);
				
				if(user == null) {
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Bearer realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
		    		
					requestContext.abortWith(responseBuilder.build());
					
		    		return;
				}
				
				if(user.getUpdateDate() == null || user.getUpdateDate().compareTo(accessToken.getStartDate()) > 0) {
					ResponseBuilder responseBuilder = Response.status(401);
					responseBuilder.header("WWW-Authenticate", "Bearer realm=\"oboco\"");
					responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
		    		
					requestContext.abortWith(responseBuilder.build());
					
		    		return;
				}
		    	
		    	UserPrincipal userPrincipal = new UserPrincipal(user);
		    	
		    	UserSecurityContext userSecurityContext = new UserSecurityContext(requestContext.getSecurityContext(), userPrincipal);
		        requestContext.setSecurityContext(userSecurityContext);
        	}
        }
    }
}