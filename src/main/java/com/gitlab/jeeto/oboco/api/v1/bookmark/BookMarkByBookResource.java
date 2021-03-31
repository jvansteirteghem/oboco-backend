package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.gitlab.jeeto.oboco.common.GraphDto;
import com.gitlab.jeeto.oboco.common.GraphDtoHelper;
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
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_BOOK_ID_INVALID, PROBLEM_BOOK_MARK_PAGE_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_MARK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createOrUpdateBookMark(
			@Parameter(name = "bookMark", description = "The bookMark.", required = true) BookMarkDto bookMarkDto) throws ProblemException {
		GraphDto graphDto = GraphDtoHelper.createGraphDto("()");
		GraphDto fullGraphDto = GraphDtoHelper.createGraphDto("(book(bookCollection))");
		
		GraphDtoHelper.validateGraphDto(graphDto, fullGraphDto);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		Book book = bookService.getBookByBookCollectionIdAndId(rootBookCollectionId, bookId);
		
		if(book == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_ID_INVALID", "The book.id is invalid."));
		}
		
		if(bookMarkDto.getPage() == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_MARK_PAGE_INVALID", "The bookMark.page is invalid: bookMark.page is null."));
		}
		
		if(bookMarkDto.getPage() < 0 || bookMarkDto.getPage() > book.getNumberOfPages()) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_MARK_PAGE_INVALID", "The bookMark.page is invalid: bookMark.page is < 0 or bookMark.page is > book.numberOfPages."));
		}
		
		Date updateDate = new Date();
		
		BookMarkReference bookMarkReference = bookMarkService.getBookMarkReferenceByBookCollectionIdAndUserIdAndBookId(rootBookCollectionId, user.getId(), bookId);
		
		if(bookMarkReference == null) {
			BookMark bookMark = new BookMark();
			bookMark.setUser(user);
			bookMark.setFileId(book.getFileId());
			bookMark.setUpdateDate(updateDate);
			bookMark.setPage(bookMarkDto.getPage());
			
			List<BookMarkReference> bookMarkReferenceList = new ArrayList<BookMarkReference>();
			
			List<Book> referencedBookList = bookService.getBooksByFileId(book.getFileId());
			
			for(Book referencedBook: referencedBookList) {
				bookMarkReference = new BookMarkReference();
				bookMarkReference.setUser(user);
				bookMarkReference.setFileId(referencedBook.getFileId());
				bookMarkReference.setUpdateDate(updateDate);
				bookMarkReference.setBook(referencedBook);
				bookMarkReference.setBookMark(bookMark);
				bookMarkReference.setRootBookCollection(referencedBook.getRootBookCollection());
				
				bookMarkReferenceList.add(bookMarkReference);
			}
			
			bookMark.setBookMarkReferences(bookMarkReferenceList);
			
			bookMark = bookMarkService.createBookMark(bookMark);
		} else {
			BookMark bookMark = bookMarkReference.getBookMark();
			bookMark.setUpdateDate(updateDate);
			bookMark.setPage(bookMarkDto.getPage());
			
			bookMark = bookMarkService.updateBookMark(bookMark);
			
			bookMarkReference = bookMarkService.getBookMarkReferenceByBookCollectionIdAndUserIdAndBookId(rootBookCollectionId, user.getId(), bookId);
			
			bookMarkDto = bookMarkDtoMapper.getBookMarkDto(user, bookMarkReference, graphDto);
		}
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookMarkDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Delete the bookMark of the book."
	)
	@APIResponses({
		@APIResponse(responseCode = "200"),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_BOOK_ID_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_MARK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@DELETE
	public Response deleteBookMark() throws ProblemException {
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		Book book = bookService.getBookByBookCollectionIdAndId(rootBookCollectionId, bookId);
		
		if(book == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_ID_INVALID", "The book.id is invalid."));
		}
		
		BookMarkReference bookMarkReference = bookMarkService.getBookMarkReferenceByBookCollectionIdAndUserIdAndBookId(rootBookCollectionId, user.getId(), bookId);
		
		if(bookMarkReference == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_MARK_NOT_FOUND", "The bookMark is not found."));
		}
		
		BookMark bookMark = bookMarkReference.getBookMark();
		
		bookMarkService.deleteBookMark(bookMark);
		
		ResponseBuilder responseBuilder = Response.status(200);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the bookMark of the book."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookMark.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookMarkDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID, PROBLEM_BOOK_ID_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_MARK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@GET
	public Response getBookMark(
			@Parameter(name = "graph", description = "The graph. The full graph is (book(bookCollection)).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		GraphDto graphDto = GraphDtoHelper.createGraphDto(graphValue);
		GraphDto fullGraphDto = GraphDtoHelper.createGraphDto("(book(bookCollection))");
		
		GraphDtoHelper.validateGraphDto(graphDto, fullGraphDto);
		
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		Book book = bookService.getBookByBookCollectionIdAndId(rootBookCollectionId, bookId);
		
		if(book == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_ID_INVALID", "The book.id is invalid."));
		}
		
		BookMarkReference bookMarkReference = bookMarkService.getBookMarkReferenceByBookCollectionIdAndUserIdAndId(rootBookCollectionId, user.getId(), bookId);
		
		if(bookMarkReference == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_BOOK_MARK_NOT_FOUND", "The bookMark is not found."));
		}
		
		BookMarkDto bookMarkDto = bookMarkDtoMapper.getBookMarkDto(user, bookMarkReference, graphDto);
	        
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookMarkDto);
		
		return responseBuilder.build();
	}
}
