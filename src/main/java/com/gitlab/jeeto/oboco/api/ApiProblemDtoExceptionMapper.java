package com.gitlab.jeeto.oboco.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import com.gitlab.jeeto.oboco.common.exception.ProblemDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemDtoExceptionMapper;

@Provider
public class ApiProblemDtoExceptionMapper extends ProblemDtoExceptionMapper {
	@Override
	public Response getResponse(ProblemDto problemDto) {
		ResponseBuilder responseBuilder = Response.status(problemDto.getStatusCode());
		responseBuilder.type(MediaType.APPLICATION_JSON);
		responseBuilder.entity(problemDto);

		return responseBuilder.build();
	}
}
