package com.gitlab.jeeto.oboco.problem;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProblemExceptionMapperBase implements ExceptionMapper<Throwable> {
	private static Logger logger = LoggerFactory.getLogger(ProblemExceptionMapperBase.class.getName());
	
public abstract Response getResponse(Problem problem);
	
	@Override
	public Response toResponse(Throwable t) {
		Problem problem = null;
		
		if(t instanceof ProblemException) {
			ProblemException e = (ProblemException) t;
			
			problem = e.getProblem();
		} else if(t instanceof WebApplicationException) {
			WebApplicationException e = (WebApplicationException) t;
			
			problem = new Problem();
			problem.setStatusCode(e.getResponse().getStatus());
			problem.setCode("PROBLEM");
			problem.setDescription("Problem.");
		} else {
			logger.error("Error.", t);
			
			problem = new Problem();
			problem.setStatusCode(500);
			problem.setCode("PROBLEM");
			problem.setDescription("Problem.");
		}
		
    	return getResponse(problem);
	}
}
