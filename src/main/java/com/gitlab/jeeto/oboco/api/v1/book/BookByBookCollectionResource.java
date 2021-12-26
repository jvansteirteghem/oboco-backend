package com.gitlab.jeeto.oboco.api.v1.book;

import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
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

import com.gitlab.jeeto.oboco.api.LinkableDto;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.api.PageableListDtoHelper;
import com.gitlab.jeeto.oboco.api.ProblemDto;
import com.gitlab.jeeto.oboco.common.image.ScaleType;
import com.gitlab.jeeto.oboco.database.Graph;
import com.gitlab.jeeto.oboco.database.GraphHelper;
import com.gitlab.jeeto.oboco.database.Linkable;
import com.gitlab.jeeto.oboco.database.PageableList;
import com.gitlab.jeeto.oboco.database.book.Book;
import com.gitlab.jeeto.oboco.database.book.BookService;
import com.gitlab.jeeto.oboco.database.user.User;
import com.gitlab.jeeto.oboco.problem.Problem;
import com.gitlab.jeeto.oboco.problem.ProblemException;
import com.gitlab.jeeto.oboco.server.authentication.Authentication;
import com.gitlab.jeeto.oboco.server.authentication.UserPrincipal;
import com.gitlab.jeeto.oboco.server.authorization.Authorization;

@SecurityRequirement(name = "bearerAuth")
@Authentication(type = "BEARER")
@Authorization(roles = { "USER" })
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookByBookCollectionResource {
	@Context
    SecurityContext securityContext;
	@Context
	UriInfo uriInfo;
	@Inject
	BookService bookService;
	@Inject
	BookDtoMapper bookDtoMapper;
	
	private Long bookCollectionId;
	
	public void setBookCollectionId(Long bookCollectionId) {
		this.bookCollectionId = bookCollectionId;
	}
	
	public BookByBookCollectionResource() {
		super();
	}
	
	@Operation(
		description = "Get the books of the bookCollection."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The books.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookPageableListDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_PAGE_INVALID, PROBLEM_PAGE_SIZE_INVALID, PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("")
	@GET
	public Response getBooksByBookCollection(
			@Parameter(name = "filterType", description = "The filterType. The filterType is ALL, NEW, TO_READ, LATEST_READ, READ, READING or UNREAD.", required = false) @QueryParam("filterType") BookFilterType filterType, 
			@Parameter(name = "page", description = "The page. The page is >= 1.", required = false) @DefaultValue("1") @QueryParam("page") Integer page, 
			@Parameter(name = "pageSize", description = "The pageSize. The pageSize is >= 1 and <= 100.", required = false) @DefaultValue("25") @QueryParam("pageSize") Integer pageSize, 
			@Parameter(name = "graph", description = "The graph. The full graph is (bookCollection,bookMark).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		PageableListDtoHelper.validatePageableList(page, pageSize);
		
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(bookCollection,bookMark)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		PageableList<Book> bookPageableList;
		
		if(BookFilterType.ALL.equals(filterType)) {
			bookPageableList = bookService.getAllBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		} else if(BookFilterType.NEW.equals(filterType)) {
			bookPageableList = bookService.getNewBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		} else if(BookFilterType.TO_READ.equals(filterType)) {
			bookPageableList = bookService.getToReadBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		} else if(BookFilterType.LATEST_READ.equals(filterType)) {
			bookPageableList = bookService.getLatestReadBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		} else if(BookFilterType.READ.equals(filterType)) {
			bookPageableList = bookService.getReadBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		} else if(BookFilterType.READING.equals(filterType)) {
			bookPageableList = bookService.getReadingBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		} else if(BookFilterType.UNREAD.equals(filterType)) {
			bookPageableList = bookService.getUnreadBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		} else {
			bookPageableList = bookService.getBooksByUserAndBookCollection(user, bookCollectionId, page, pageSize, graph);
		}
		
		PageableListDto<BookDto> bookPageableListDto = bookDtoMapper.getBooksDto(bookPageableList, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookPageableListDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the linkable book of the bookCollection."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The linkable book.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookLinkableDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("{bookId: [0-9]+}")
	@GET
	public Response getLinkableBookByBookCollection(
			@Parameter(name = "bookId", description = "The id of the book.", required = false) @PathParam("bookId") Long bookId, 
			@Parameter(name = "graph", description = "The graph. The full graph is (bookCollection,bookMark).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(bookCollection,bookMark)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Linkable<Book> bookLinkable = bookService.getLinkableBookByUserAndBookCollection(user, bookCollectionId, bookId, graph);
		LinkableDto<BookDto> bookLinkableDto = bookDtoMapper.getBooksDto(bookLinkable, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookLinkableDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the first page of the first book of the bookCollection as *.jpg."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The page.", content = @Content(mediaType = "image/jpeg")),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND, PROBLEM_BOOK_COLLECTION_NOT_FOUND, PROBLEM_BOOK_NOT_FOUND, PROBLEM_BOOK_PAGE_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "503", description = "The problem: PROBLEM_BOOK_SCANNER_STATUS_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("FIRST/pages/1.jpg")
	@GET
	@Produces({"image/jpeg"})
	public Response getBookPageAsByBookCollection(
			@Parameter(name = "scaleType", description = "The scaleType. The scaleType is DEFAULT.", required = false) @QueryParam("scaleType") ScaleType scaleType, 
			@Parameter(name = "scaleWidth", description = "The scaleWidth.", required = false) @QueryParam("scaleWidth") Integer scaleWidth, 
			@Parameter(name = "scaleHeight", description = "The scaleHeight.", required = false) @QueryParam("scaleHeight") Integer scaleHeight, 
			@Context Request request) throws ProblemException {
		Integer page = 1;
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Book book = null;
		
		PageableList<Book> bookPageableList = bookService.getBooksByUserAndBookCollection(user, bookCollectionId, 1, 1, null);
		
		if(bookPageableList.getElements() != null && bookPageableList.getElements().size() == 1) {
			book = bookPageableList.getElements().get(0);
		}
		
		if(book == null) {
        	throw new ProblemException(new Problem(404, "PROBLEM_BOOK_NOT_FOUND", "The book is not found."));
        }
        
        if(page < 1 || page > book.getNumberOfPages()) {
        	throw new ProblemException(new Problem(404, "PROBLEM_BOOK_PAGE_NOT_FOUND", "The bookPage is not found."));
        }
        
        String tagValue = book.getFileId();
        if(page != null) {
        	tagValue = tagValue + "-page" + page;
        }
        if(scaleType != null) {
        	tagValue = tagValue + "-scaleType" + scaleType;
        }
        if(scaleWidth != null) {
        	tagValue = tagValue + "-scaleWidth" + scaleWidth;
        }
        if(scaleHeight != null) {
        	tagValue = tagValue + "-scaleHeight" + scaleHeight;
        }
        
        EntityTag tag = new EntityTag(tagValue);
        
        Date updateDate = book.getUpdateDate();
		
		CacheControl cacheControl = new CacheControl();
		cacheControl.setMaxAge(300);
		
		ResponseBuilder responseBuilder = request.evaluatePreconditions(updateDate, tag);
		if(responseBuilder != null) {
			responseBuilder.cacheControl(cacheControl);
			
			return responseBuilder.build();
		}
		
		GetBookPageAsStreamingOutput getBookPageAsStreamingOutput = new GetBookPageAsStreamingOutput(book, 1, scaleType, scaleWidth, scaleHeight);
		
		responseBuilder = Response.status(200);
		responseBuilder.cacheControl(cacheControl);
		responseBuilder.tag(tag);
		responseBuilder.lastModified(updateDate);
		responseBuilder.type("image/jpeg");
		responseBuilder.entity(getBookPageAsStreamingOutput);
		
		return responseBuilder.build();
	}
}
