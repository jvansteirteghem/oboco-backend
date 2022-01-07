package com.gitlab.jeeto.oboco.server.authorization;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.gitlab.jeeto.oboco.api.ProblemDto;

@Authorization
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationRequestFilter implements ContainerRequestFilter {
    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    	Method resourceMethod = resourceInfo.getResourceMethod();
    	if(resourceMethod != null) {
    		Authorization authorization = resourceMethod.getAnnotation(Authorization.class);
	        if (authorization != null) {
	            doAuthorization(authorization, requestContext);
	            return;
	        }
    	}
        
    	Class<?> resourceClass = resourceInfo.getResourceClass();
    	if(resourceClass != null) {
    		Authorization authorization = resourceClass.getAnnotation(Authorization.class);
	        if (authorization != null) {
	            doAuthorization(authorization, requestContext);
	            return;
	        }
    	}
    }

    private void doAuthorization(Authorization authorization, ContainerRequestContext requestContext) {
    	SecurityContext securityContext = requestContext.getSecurityContext();
    	
    	if(securityContext.getUserPrincipal() == null) {
    		ResponseBuilder responseBuilder = Response.status(401);
    		responseBuilder.entity(new ProblemDto(401, "PROBLEM_USER_NOT_AUTHENTICATED", "The user is not authenticated."));
        	
    		requestContext.abortWith(responseBuilder.build());
        	
        	return;
    	}
    	
        for(String role: authorization.roles()) {
            if(securityContext.isUserInRole(role)) {
                return;
            }
        }
        
        ResponseBuilder responseBuilder = Response.status(403);
        responseBuilder.entity(new ProblemDto(403, "PROBLEM_USER_NOT_AUTHORIZED", "The user is not authorized."));
    	
        requestContext.abortWith(responseBuilder.build());
    	
    	return;
    }
}