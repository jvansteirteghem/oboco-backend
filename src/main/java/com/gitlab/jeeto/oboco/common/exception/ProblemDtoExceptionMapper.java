package com.gitlab.jeeto.oboco.common.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProblemDtoExceptionMapper implements ExceptionMapper<Throwable> {
	private static Logger logger = LoggerFactory.getLogger(ProblemDtoExceptionMapper.class.getName());
	
	public abstract Response getResponse(ProblemDto problemDto);
	
	@Override
	public Response toResponse(Throwable t) {
		ProblemDto problemDto = null;
		
		if(t instanceof ProblemException) {
			ProblemException e = (ProblemException) t;
			Problem problem = e.getProblem();
			
			problemDto = new ProblemDto();
			problemDto.setStatusCode(problem.getStatusCode());
			problemDto.setCode(problem.getCode());
			problemDto.setDescription(problem.getDescription());
		} else if(t instanceof WebApplicationException) {
			WebApplicationException e = (WebApplicationException) t;
			
			problemDto = new ProblemDto();
			problemDto.setStatusCode(e.getResponse().getStatus());
			problemDto.setCode("PROBLEM");
			problemDto.setDescription("Problem.");
		} else {
			logger.error("Error.", t);
			
			problemDto = new ProblemDto();
			problemDto.setStatusCode(500);
			problemDto.setCode("PROBLEM");
			problemDto.setDescription("Problem.");
		}
		
    	return getResponse(problemDto);
	}
}
