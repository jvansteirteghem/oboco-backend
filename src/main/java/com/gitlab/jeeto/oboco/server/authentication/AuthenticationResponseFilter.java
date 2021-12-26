package com.gitlab.jeeto.oboco.server.authentication;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.api.ProblemDto;

@Authentication
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationResponseFilter implements ContainerResponseFilter {
	private static Logger logger = LoggerFactory.getLogger(AuthenticationResponseFilter.class.getName());
	@Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    	Method resourceMethod = resourceInfo.getResourceMethod();
    	if(resourceMethod != null) {
    		Authentication authentication = resourceMethod.getAnnotation(Authentication.class);
	        if (authentication != null) {
	            handleAuthentication(authentication, requestContext, responseContext);
	            return;
	        }
    	}
        
    	Class<?> resourceClass = resourceInfo.getResourceClass();
    	if(resourceClass != null) {
    		Authentication authentication = resourceClass.getAnnotation(Authentication.class);
	        if (authentication != null) {
	        	handleAuthentication(authentication, requestContext, responseContext);
	            return;
	        }
    	}
    }
    
    private void handleAuthentication(Authentication authentication, ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    	try {
        	if(authentication.type().equals("BASIC")) {
	            handleBasicAuthentication(requestContext, responseContext);
        	} else if(authentication.type().equals("BEARER")) {
        		handleBearerAuthentication(requestContext, responseContext);
        	}
    	} catch (Exception e) {
			logger.error("Error.", e);
			
			ResponseBuilder responseBuilder = Response.status(500);
			responseBuilder.entity(new ProblemDto(500, "PROBLEM", "Problem."));
    		
			requestContext.abortWith(responseBuilder.build());
			
    		return;
		}
    }
    
	public void handleBasicAuthentication(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws Exception {
		if(responseContext.getStatus() == 401) {
			if(responseContext.getHeaders().get("WWW-Authenticate") == null) {
				responseContext.getHeaders().add("WWW-Authenticate", "Basic realm=\"oboco\"");
			}
		}
	}
	
	public void handleBearerAuthentication(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws Exception {
		if(responseContext.getStatus() == 401) {
			if(responseContext.getHeaders().get("WWW-Authenticate") == null) {
				responseContext.getHeaders().add("WWW-Authenticate", "Bearer realm=\"oboco\"");
			}
		}
	}
}