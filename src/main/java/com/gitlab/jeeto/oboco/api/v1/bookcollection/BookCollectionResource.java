package com.gitlab.jeeto.oboco.api.v1.bookcollection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.gitlab.jeeto.oboco.api.v1.book.BookByBookCollectionResource;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkByBookCollectionResource;
import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.GraphHelper;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.PageableListDtoHelper;
import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;
import com.gitlab.jeeto.oboco.common.security.Authentication;
import com.gitlab.jeeto.oboco.common.security.Authorization;
import com.gitlab.jeeto.oboco.common.security.UserPrincipal;

@SecurityRequirement(name = "bearerAuth")
@Authentication(type = "BEARER")
@Authorization(roles = { "USER" })
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookCollectionResource {
	@Context
    SecurityContext securityContext;
	@Context
	ResourceContext resourceContext;
	@Context
	UriInfo uriInfo;
	@Inject
	BookCollectionService bookCollectionService;
	@Inject
	BookCollectionDtoMapper bookCollectionDtoMapper;
	
	@Operation(
		description = "Get the bookCollections."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookCollections.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookCollectionPageableListDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_PAGE_INVALID, PROBLEM_PAGE_SIZE_INVALID, PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("")
	@GET
	public Response getBookCollections(
			@Parameter(name = "parentBookCollectionId", description = "The id of the parent of the bookCollection.", required = false) @QueryParam("parentBookCollectionId") Long parentBookCollectionId, 
			@Parameter(name = "name", description = "The name of the bookCollection.", required = false) @QueryParam("name") String name, 
			@Parameter(name = "page", description = "The page. The page is >= 1.", required = false) @DefaultValue("1") @QueryParam("page") Integer page, 
			@Parameter(name = "pageSize", description = "The pageSize. The pageSize is >= 1 and <= 100.", required = false) @DefaultValue("25") @QueryParam("pageSize") Integer pageSize, 
			@Parameter(name = "graph", description = "The graph. The full graph is (parentBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		PageableListDtoHelper.validatePageableList(page, pageSize);
		
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(parentBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		PageableList<BookCollection> bookCollectionPageableList = null;
		
		if(uriInfo.getQueryParameters().containsKey("parentBookCollectionId")) {
			if(uriInfo.getQueryParameters().containsKey("name")) {
				bookCollectionPageableList = bookCollectionService.getBookCollectionsByUserAndName(user, parentBookCollectionId, name, page, pageSize, graph);
			} else {
				bookCollectionPageableList = bookCollectionService.getBookCollectionsByUser(user, parentBookCollectionId, page, pageSize, graph);
			}
		} else {
			if(uriInfo.getQueryParameters().containsKey("name")) {
				bookCollectionPageableList = bookCollectionService.getBookCollectionsByUserAndName(user, name, page, pageSize, graph);
			} else {
				bookCollectionPageableList = bookCollectionService.getBookCollectionsByUser(user, page, pageSize, graph);
			}
		}
		
		PageableListDto<BookCollectionDto> bookCollectionPageableListDto = bookCollectionDtoMapper.getBookCollectionsDto(bookCollectionPageableList, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookCollectionPageableListDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the root bookCollection."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The root bookCollection.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookCollectionDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND, PROBLEM_BOOK_COLLECTION_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("ROOT")
	@GET
	public Response getRootBookCollection(
			@Parameter(name = "graph", description = "The graph. The full graph is (parentBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(parentBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		BookCollection bookCollection = bookCollectionService.getRootBookCollectionById(rootBookCollectionId, graph);
		
		if(bookCollection == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_COLLECTION_NOT_FOUND", "The bookCollection is not found."));
		}
		
		BookCollectionDto bookCollectionDto = bookCollectionDtoMapper.getBookCollectionDto(bookCollection, graph);
	        
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookCollectionDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the latest bookCollections."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The latest bookCollections.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookCollectionPageableListDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_PAGE_INVALID, PROBLEM_PAGE_SIZE_INVALID, PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("LATEST/bookCollections")
	@GET
	public Response getLatestBookCollections(
			@Parameter(name = "name", description = "The name of the bookCollection.", required = false) @QueryParam("name") String name, 
			@Parameter(name = "page", description = "The page. The page is >= 1.", required = false) @DefaultValue("1") @QueryParam("page") Integer page, 
			@Parameter(name = "pageSize", description = "The pageSize. The pageSize is >= 1 and <= 100.", required = false) @DefaultValue("25") @QueryParam("pageSize") Integer pageSize, 
			@Parameter(name = "graph", description = "The graph. The full graph is (parentBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		PageableListDtoHelper.validatePageableList(page, pageSize);
		
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(parentBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		PageableList<BookCollection> bookCollectionPageableList = bookCollectionService.getLatestBookCollectionsByUserAndName(user, name, page, pageSize, graph);
		
		PageableListDto<BookCollectionDto> bookCollectionPageableListDto = bookCollectionDtoMapper.getBookCollectionsDto(bookCollectionPageableList, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookCollectionPageableListDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the bookCollection."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookCollection.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookCollectionDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND, PROBLEM_BOOK_COLLECTION_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("{bookCollectionId}")
	@GET
	public Response getBookCollection(
			@Parameter(name = "bookCollectionId", description = "The id of the bookCollection.", required = true) @PathParam("bookCollectionId") Long bookCollectionId, 
			@Parameter(name = "graph", description = "The graph. The full graph is (parentBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(parentBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		BookCollection bookCollection = bookCollectionService.getBookCollectionByUserAndId(user, bookCollectionId, graph);
		
		if(bookCollection == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_COLLECTION_NOT_FOUND", "The bookCollection is not found."));
		}
		
		BookCollectionDto bookCollectionDto = bookCollectionDtoMapper.getBookCollectionDto(bookCollection, graph);
	        
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookCollectionDto);
		
		return responseBuilder.build();
	}
	
	@Path("{bookCollectionId}/books")
	public BookByBookCollectionResource getBookByBookCollectionResource(
			@Parameter(name = "bookCollectionId", description = "The id of the bookCollection.", required = true) @PathParam("bookCollectionId") Long bookCollectionId) {
		BookByBookCollectionResource bookByBookCollectionResource = resourceContext.getResource(BookByBookCollectionResource.class);
		bookByBookCollectionResource.setBookCollectionId(bookCollectionId);
		
		return bookByBookCollectionResource;
	}
	
	@Path("{bookCollectionId}/bookMarks")
	public BookMarkByBookCollectionResource getBookMarkByBookCollectionResource(
			@Parameter(name = "bookCollectionId", description = "The id of the bookCollection.", required = true) @PathParam("bookCollectionId") Long bookCollectionId) {
		BookMarkByBookCollectionResource bookMarkByBookCollectionResource = resourceContext.getResource(BookMarkByBookCollectionResource.class);
		bookMarkByBookCollectionResource.setBookCollectionId(bookCollectionId);
		
		return bookMarkByBookCollectionResource;
	}
}
