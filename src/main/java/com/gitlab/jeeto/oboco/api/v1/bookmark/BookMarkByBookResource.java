package com.gitlab.jeeto.oboco.api.v1.bookmark;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.gitlab.jeeto.oboco.api.v1.book.Book;
import com.gitlab.jeeto.oboco.api.v1.book.BookService;
import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.GraphHelper;
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
public class BookMarkByBookResource {
	@Context
    SecurityContext securityContext;
	@Inject
	BookMarkService bookMarkService;
	@Inject
	BookMarkDtoMapper bookMarkDtoMapper;
	@Inject
	BookService bookService;
	
	private Long bookId;
	
	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}
	
	public BookMarkByBookResource() {
		super();
	}
	
	@Operation(
		description = "Create or update the bookMark of the book."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookMark.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookMarkDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_BOOK_MARK_PAGE_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND, PROBLEM_BOOK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createOrUpdateBookMarkByBook(
			@Parameter(name = "bookMark", description = "The bookMark.", required = true) BookMarkDto bookMarkDto) throws ProblemException {
		Graph graph = GraphHelper.createGraph("()");
		Graph fullGraph = GraphHelper.createGraph("(book(bookCollection))");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Book book = bookService.getBookByUser(user, bookId, null);
		
		if(book == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_NOT_FOUND", "The book is not found."));
		}
		
		if(bookMarkDto.getPage() == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_MARK_PAGE_INVALID", "The bookMark.page is invalid: bookMark.page is null."));
		}
		
		if(bookMarkDto.getPage() < -1 || bookMarkDto.getPage() > book.getNumberOfPages()) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_MARK_PAGE_INVALID", "The bookMark.page is invalid: bookMark.page is < -1 or bookMark.page is > book.numberOfPages."));
		}
		
		BookMarkReference bookMarkReference = bookMarkService.createOrUpdateBookMarkByUserAndBook(user, book, bookMarkDto.getPage(), graph);
		
		bookMarkDto = bookMarkDtoMapper.getBookMarkDto(bookMarkReference, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookMarkDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Delete the bookMark of the book."
	)
	@APIResponses({
		@APIResponse(responseCode = "200"),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND, PROBLEM_BOOK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("")
	@DELETE
	public Response deleteBookMarkByBook() throws ProblemException {
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Book book = bookService.getBookByUser(user, bookId);
		
		if(book == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_NOT_FOUND", "The book is not found."));
		}
		
		bookMarkService.deleteBookMarkByUserAndBook(user, book);
		
		ResponseBuilder responseBuilder = Response.status(200);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the bookMark of the book."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookMark.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookMarkDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND, PROBLEM_BOOK_NOT_FOUND, PROBLEM_BOOK_MARK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("")
	@GET
	public Response getBookMarkByBook(
			@Parameter(name = "graph", description = "The graph. The full graph is (book(bookCollection)).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(book(bookCollection))");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Book book = bookService.getBookByUser(user, bookId);
		
		if(book == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_NOT_FOUND", "The book is not found."));
		}
		
		BookMarkReference bookMarkReference = bookMarkService.getBookMarkReferenceByUserAndBook(user, book, graph);
		
		if(bookMarkReference == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_MARK_NOT_FOUND", "The bookMark is not found."));
		}
		
		BookMarkDto bookMarkDto = bookMarkDtoMapper.getBookMarkDto(bookMarkReference, graph);
	        
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookMarkDto);
		
		return responseBuilder.build();
	}
}
