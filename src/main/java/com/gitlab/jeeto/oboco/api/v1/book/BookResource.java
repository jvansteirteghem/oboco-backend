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
import javax.ws.rs.container.ResourceContext;
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

import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkByBookResource;
import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.common.GraphDto;
import com.gitlab.jeeto.oboco.common.GraphDtoHelper;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.PageableListDtoHelper;
import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;
import com.gitlab.jeeto.oboco.common.image.ScaleType;
import com.gitlab.jeeto.oboco.common.security.Authentication;
import com.gitlab.jeeto.oboco.common.security.Authorization;
import com.gitlab.jeeto.oboco.common.security.UserPrincipal;

@SecurityRequirement(name = "bearerAuth")
@Authentication(type = "BEARER")
@Authorization(roles = { "USER" })
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {
	@Context
    SecurityContext securityContext;
	@Context
	ResourceContext resourceContext;
	@Context
	UriInfo uriInfo;
	@Inject
	BookService bookService;
	@Inject
	BookDtoMapper bookDtoMapper;
	
	@Operation(
		description = "Get the books."
    )
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The books.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookPageableListDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_PAGE_INVALID, PROBLEM_PAGE_SIZE_INVALID, PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@GET
	public Response getBooks(
			@Parameter(name = "name", description = "The name of the book.", required = false) @QueryParam("name") String name, 
			@Parameter(name = "page", description = "The page. The page is >= 1.", required = false) @DefaultValue("1") @QueryParam("page") Integer page, 
			@Parameter(name = "pageSize", description = "The pageSize. The pageSize is >= 1 and <= 100.", required = false) @DefaultValue("25") @QueryParam("pageSize") Integer pageSize, 
			@Parameter(name = "graph", description = "The graph. The full graph is (bookCollection,bookMark).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		PageableListDtoHelper.validatePageableList(page, pageSize);
		
		GraphDto graphDto = GraphDtoHelper.createGraphDto(graphValue);
		GraphDto fullGraphDto = GraphDtoHelper.createGraphDto("(bookCollection,bookMark)");
		
		GraphDtoHelper.validateGraphDto(graphDto, fullGraphDto);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		PageableList<Book> bookPageableList = null;
		
		if(uriInfo.getQueryParameters().containsKey("name")) {
			bookPageableList = bookService.getBooksByBookCollectionIdAndName(rootBookCollectionId, name, page, pageSize);
		} else {
			bookPageableList = bookService.getBooksByBookCollectionId(rootBookCollectionId, page, pageSize);
		}
		
		PageableListDto<BookDto> bookPageableListDto = bookDtoMapper.getBooksDto(user, bookPageableList, graphDto);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookPageableListDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the book."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The book.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("{bookId}")
	@GET
	public Response getBook(
			@Parameter(name = "bookId", description = "The id of the book.", required = true) @PathParam("bookId") Long bookId, 
			@Parameter(name = "graph", description = "The graph. The full graph is (bookCollection,bookMark).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		GraphDto graphDto = GraphDtoHelper.createGraphDto(graphValue);
		GraphDto fullGraphDto = GraphDtoHelper.createGraphDto("(bookCollection,bookMark)");
		
		GraphDtoHelper.validateGraphDto(graphDto, fullGraphDto);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		Book book = bookService.getBookByBookCollectionIdAndId(rootBookCollectionId, bookId);
		
		if(book == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_NOT_FOUND", "The book is not found."));
		}
		
		BookDto bookDto = bookDtoMapper.getBookDto(user, book, graphDto);
	        
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the book as *.cbz."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The book.", content = @Content(mediaType = "application/octet-stream")),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("{bookId}.cbz")
	@GET
	public Response getBookAs(
			@Parameter(name = "bookId", description = "The id of the book.", required = true) @PathParam("bookId") Long bookId, 
			@Context Request request) throws ProblemException {
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		Book book = bookService.getBookByBookCollectionIdAndId(rootBookCollectionId, bookId);
		
        if(book == null) {
        	throw new ProblemException(new Problem(404, "PROBLEM_BOOK_NOT_FOUND", "The book is not found."));
        }
        
        String tagValue = book.getFileId();
        
        EntityTag tag = new EntityTag(tagValue);
        
        Date updateDate = book.getUpdateDate();
		
		CacheControl cacheControl = new CacheControl();
		cacheControl.setMaxAge(300);
		
		ResponseBuilder responseBuilder = request.evaluatePreconditions(updateDate, tag);
		if(responseBuilder != null) {
			responseBuilder.cacheControl(cacheControl);
			
			return responseBuilder.build();
		}
		
		GetBookAsStreamingOutput getBookAsStreamingOutput = new GetBookAsStreamingOutput(book);
		
		responseBuilder = Response.status(200);
		responseBuilder.cacheControl(cacheControl);
		responseBuilder.tag(tag);
		responseBuilder.lastModified(updateDate);
		responseBuilder.header("Content-Disposition", "attachment; filename=\"" + book.getName() + ".cbz\"");
		responseBuilder.type("application/zip");
		responseBuilder.entity(getBookAsStreamingOutput);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the page as *.jpg."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The page.", content = @Content(mediaType = "image/jpeg")),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("{bookId}/pages/{page}.jpg")
	@GET
	@Produces({"image/jpeg"})
	public Response getBookPageAs(
			@Parameter(name = "bookId", description = "The id of the book.", required = true) @PathParam("bookId") Long bookId, 
			@Parameter(name = "page", description = "The page. The page is >= 1 and <= book.numberOfPages.", required = false) @DefaultValue("1") @PathParam("page") Integer page, 
			@Parameter(name = "scaleType", description = "The scaleType. The scaleType is DEFAULT, FIT or FILL.", required = false) @QueryParam("scaleType") ScaleType scaleType, 
			@Parameter(name = "scaleWidth", description = "The scaleWidth.", required = false) @QueryParam("scaleWidth") Integer scaleWidth, 
			@Parameter(name = "scaleHeight", description = "The scaleHeight.", required = false) @QueryParam("scaleHeight") Integer scaleHeight, 
			@Context Request request) throws ProblemException {
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		Book book = bookService.getBookByBookCollectionIdAndId(rootBookCollectionId, bookId);
		
        if(book == null) {
        	throw new ProblemException(new Problem(404, "PROBLEM_BOOK_NOT_FOUND", "The book is not found."));
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
		
		GetBookPageAsStreamingOutput getBookPageAsStreamingOutput = new GetBookPageAsStreamingOutput(book, page, scaleType, scaleWidth, scaleHeight);
		
		responseBuilder = Response.status(200);
		responseBuilder.cacheControl(cacheControl);
		responseBuilder.tag(tag);
		responseBuilder.lastModified(updateDate);
		responseBuilder.type("image/jpeg");
		responseBuilder.entity(getBookPageAsStreamingOutput);
		
		return responseBuilder.build();
	}
	
	@Path("{bookId}/bookMark")
	public BookMarkByBookResource getBookMarkByBookResource(
			@Parameter(name = "bookId", description = "The id of the book.", required = true) @PathParam("bookId") Long bookId) {
		BookMarkByBookResource bookMarkByBookResource = resourceContext.getResource(BookMarkByBookResource.class);
		bookMarkByBookResource.setBookId(bookId);
		
		return bookMarkByBookResource;
	}
}
