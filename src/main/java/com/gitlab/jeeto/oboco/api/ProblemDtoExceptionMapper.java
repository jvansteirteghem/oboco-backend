package com.gitlab.jeeto.oboco.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import com.gitlab.jeeto.oboco.problem.Problem;
import com.gitlab.jeeto.oboco.problem.ProblemExceptionMapperBase;

@Provider
public class ProblemDtoExceptionMapper extends ProblemExceptionMapperBase {
	@Override
	public Response getResponse(Problem problem) {
		ProblemDto problemDto = new ProblemDto();
		problemDto.setStatusCode(problem.getStatusCode());
		problemDto.setCode(problem.getCode());
		problemDto.setDescription(problem.getDescription());
		
		ResponseBuilder responseBuilder = Response.status(problemDto.getStatusCode());
		responseBuilder.type(MediaType.APPLICATION_JSON);
		responseBuilder.entity(problemDto);

		return responseBuilder.build();
	}
}
