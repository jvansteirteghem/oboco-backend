package com.gitlab.jeeto.oboco.common.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProblemDtoExceptionMapper implements ExceptionMapper<Exception> {
	private static Logger logger = LoggerFactory.getLogger(ProblemDtoExceptionMapper.class.getName());
	
	public abstract Response getResponse(ProblemDto problemDto);
	
	@Override
	public Response toResponse(Exception e) {
		ProblemDto problemDto = null;
		
		if(e instanceof ProblemException) {
			ProblemException ee = (ProblemException) e;
			Problem problem = ee.getProblem();
			
			problemDto = new ProblemDto();
			problemDto.setStatusCode(problem.getStatusCode());
			problemDto.setCode(problem.getCode());
			problemDto.setDescription(problem.getDescription());
		} else if(e instanceof WebApplicationException) {
			logger.error("Error.", e);
			
			WebApplicationException wae = (WebApplicationException) e;
			problemDto = new ProblemDto();
			problemDto.setStatusCode(wae.getResponse().getStatus());
			problemDto.setCode("PROBLEM");
			problemDto.setDescription("Problem.");
		} else {
			logger.error("Error.", e);
			
			problemDto = new ProblemDto();
			problemDto.setStatusCode(500);
			problemDto.setCode("PROBLEM");
			problemDto.setDescription("Problem.");
		}
		
    	return getResponse(problemDto);
	}
}
