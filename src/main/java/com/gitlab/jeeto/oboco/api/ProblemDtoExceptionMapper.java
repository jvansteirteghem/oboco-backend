package com.gitlab.jeeto.oboco.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import com.gitlab.jeeto.oboco.problem.ProblemDto;
import com.gitlab.jeeto.oboco.problem.ProblemDtoExceptionMapperBase;

@Provider
public class ProblemDtoExceptionMapper extends ProblemDtoExceptionMapperBase {
	@Override
	public Response getResponse(ProblemDto problemDto) {
		ResponseBuilder responseBuilder = Response.status(problemDto.getStatusCode());
		responseBuilder.type(MediaType.APPLICATION_JSON);
		responseBuilder.entity(problemDto);

		return responseBuilder.build();
	}
}
