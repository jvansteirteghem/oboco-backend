package com.gitlab.jeeto.oboco.api.v1.bookscanner;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.gitlab.jeeto.oboco.common.GraphDto;
import com.gitlab.jeeto.oboco.common.GraphDtoHelper;
import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;
import com.gitlab.jeeto.oboco.common.security.Authentication;
import com.gitlab.jeeto.oboco.common.security.Authorization;

@SecurityRequirement(name = "bearerAuth")
@Authentication(type = "BEARER")
@Authorization(roles = { "ADMINISTRATOR" })
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookScannerResource {
	@Inject
	Instance<BookScannerService> bookScannerServiceProvider;
	
	@Operation(
		description = "Get the bookScanners."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookScanners.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookScannerDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@GET
	public Response getBookScanners(
			@Parameter(name = "graph", description = "The graph. The full graph is ().", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		GraphDto graphDto = GraphDtoHelper.createGraphDto(graphValue);
		GraphDto fullGraphDto = GraphDtoHelper.createGraphDto("()");
		
		GraphDtoHelper.validateGraphDto(graphDto, fullGraphDto);
		
		List<BookScannerDto> bookScannerListDto = new ArrayList<BookScannerDto>();
		
		for(BookScannerService bookScannerService: bookScannerServiceProvider) {
			BookScannerDto bookScannerDto = new BookScannerDto();
			bookScannerDto.setId(bookScannerService.getId());
			bookScannerDto.setStatus(bookScannerService.getStatus().toString());
			
			bookScannerListDto.add(bookScannerDto);
        }
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookScannerListDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the bookScanner."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookScanner.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookScannerDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_SCANNER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("{bookScannerId}")
	@GET
	public Response getBookScanner(
			@Parameter(name = "bookScannerId", description = "The id of the bookScanner.", required = true) @PathParam("bookScannerId") String bookScannerId,
			@Parameter(name = "graph", description = "The graph. The full graph is ().", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		GraphDto graphDto = GraphDtoHelper.createGraphDto(graphValue);
		GraphDto fullGraphDto = GraphDtoHelper.createGraphDto("()");
		
		GraphDtoHelper.validateGraphDto(graphDto, fullGraphDto);
		
		BookScannerService bookScannerService = bookScannerServiceProvider.select(NamedLiteral.of(bookScannerId)).get();
		
		if(bookScannerService == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_SCANNER_NOT_FOUND", "The bookScanner is not found."));
		}
				
		BookScannerDto bookScannerDto = new BookScannerDto();
		bookScannerDto.setId(bookScannerService.getId());
		bookScannerDto.setStatus(bookScannerService.getStatus().toString());
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookScannerDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Start the bookScanner."
	)
	@APIResponses({
		@APIResponse(responseCode = "200"),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_SCANNER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("{bookScannerId}/start")
	@POST
	public void startBookScanner(
			@Parameter(name = "bookScannerId", description = "The id of the bookScanner.", required = true) @PathParam("bookScannerId") String bookScannerId, 
			@Suspended AsyncResponse asyncResponse) {
		try {
			BookScannerService bookScannerService = bookScannerServiceProvider.select(NamedLiteral.of(bookScannerId)).get();
			
			if(bookScannerService == null) {
				throw new ProblemException(new Problem(404, "PROBLEM_BOOK_SCANNER_NOT_FOUND", "The bookScanner is not found."));
			}
			
			if(bookScannerService.getStatus().equals(BookScannerServiceStatus.STOPPED)) {
				bookScannerService.start();
				
				ResponseBuilder responseBuilder = Response.status(200);
				
				asyncResponse.resume(responseBuilder.build());
			} else {
				throw new ProblemException(new Problem(400, "PROBLEM_BOOK_SCANNER_STATUS_INVALID", "The bookScanner.status is invalid."));
			}
		} catch(ProblemException e) {
			Problem problem = e.getProblem();
			
			ProblemDto problemDto = new ProblemDto();
			problemDto.setStatusCode(problem.getStatusCode());
			problemDto.setCode(problem.getCode());
			problemDto.setDescription(problem.getDescription());
			
			ResponseBuilder responseBuilder = Response.status(problemDto.getStatusCode());
			responseBuilder.entity(problemDto);
			
			asyncResponse.resume(responseBuilder.build());
		}
	}
	
	@Operation(
		description = "Stop the bookScanner."
	)
	@APIResponses({
		@APIResponse(responseCode = "200"),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_SCANNER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("{bookScannerId}/stop")
	@POST
	public Response stopBookScanner(
			@Parameter(name = "bookScannerId", description = "The id of the bookScanner.", required = true) @PathParam("bookScannerId") String bookScannerId) throws ProblemException {
		BookScannerService bookScannerService = bookScannerServiceProvider.select(NamedLiteral.of(bookScannerId)).get();
		
		if(bookScannerService == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_SCANNER_NOT_FOUND", "The bookScanner is not found."));
		}
		
		if(bookScannerService.getStatus().equals(BookScannerServiceStatus.STARTED)) {
			bookScannerService.stop();
			
			ResponseBuilder responseBuilder = Response.status(200);
			
			return responseBuilder.build();
		} else {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_SCANNER_STATUS_INVALID", "The bookScanner.status is invalid."));
		}
	}
}
