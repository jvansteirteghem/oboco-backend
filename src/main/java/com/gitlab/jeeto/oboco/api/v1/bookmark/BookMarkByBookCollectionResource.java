package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.gitlab.jeeto.oboco.api.v1.book.Book;
import com.gitlab.jeeto.oboco.api.v1.book.BookService;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionService;
import com.gitlab.jeeto.oboco.api.v1.user.User;
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
public class BookMarkByBookCollectionResource {
	@Context
    SecurityContext securityContext;
	@Inject
	BookMarkService bookMarkService;
	@Inject
	BookCollectionService bookCollectionService;
	@Inject
	BookService bookService;
	
	private Long bookCollectionId;
	
	public void setBookCollectionId(Long bookCollectionId) {
		this.bookCollectionId = bookCollectionId;
	}
	
	public BookMarkByBookCollectionResource() {
		super();
	}
	
	@Operation(
		description = "Create or update the bookMarks of the books of the bookCollection. The bookMark.page is the last page of the book."
	)
	@APIResponses({
		@APIResponse(responseCode = "200"),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_BOOK_COLLECTION_ID_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_MARK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createOrUpdateBookMarksByBookCollection() throws ProblemException {
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		BookCollection bookCollection = bookCollectionService.getBookCollectionByBookCollectionIdAndId(rootBookCollectionId, bookCollectionId);
		
		if(bookCollection == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_COLLECTION_ID_INVALID", "The bookCollection.id is invalid."));
		}
		
		Date updateDate = new Date();
		
		List<Book> bookList = bookCollection.getBooks();
		
		for(Book book: bookList) {
			BookMarkReference bookMarkReference = bookMarkService.getBookMarkReferenceByBookCollectionIdAndUserIdAndBookId(rootBookCollectionId, user.getId(), book.getId());
			
			if(bookMarkReference == null) {
				BookMark bookMark = new BookMark();
				bookMark.setUser(user);
				bookMark.setFileId(book.getFileId());
				bookMark.setUpdateDate(updateDate);
				bookMark.setPage(book.getNumberOfPages());
				
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
				
				bookMarkService.createBookMark(bookMark);
			} else {
				BookMark bookMark = bookMarkReference.getBookMark();
				bookMark.setUpdateDate(updateDate);
				bookMark.setPage(book.getNumberOfPages());
				
				bookMark = bookMarkService.updateBookMark(bookMark);
			}
		}
		
		ResponseBuilder responseBuilder = Response.status(200);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Delete the bookMarks of the books of the bookCollection."
	)
	@APIResponses({
		@APIResponse(responseCode = "200"),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_BOOK_COLLECTION_ID_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_BOOK_MARK_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@DELETE
	public Response deleteBookMarksByBookCollection() throws ProblemException {
		User user = ((UserPrincipal) securityContext.getUserPrincipal()).getUser();
		
		if(user.getRootBookCollection() == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND", "The user.rootBookCollection is not found."));
		}
		
		Long rootBookCollectionId = user.getRootBookCollection().getId();
		
		BookCollection bookCollection = bookCollectionService.getBookCollectionByBookCollectionIdAndId(rootBookCollectionId, bookCollectionId);
		
		if(bookCollection == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_BOOK_COLLECTION_ID_INVALID", "The bookCollection.id is invalid."));
		}
		
		List<Book> bookList = bookCollection.getBooks();
		
		for(Book book: bookList) {
			BookMarkReference bookMarkReference = bookMarkService.getBookMarkReferenceByBookCollectionIdAndUserIdAndBookId(rootBookCollectionId, user.getId(), book.getId());
			
			if(bookMarkReference != null) {
				BookMark bookMark = bookMarkReference.getBookMark();
				
				bookMarkService.deleteBookMark(bookMark);
			}
		}
		
		ResponseBuilder responseBuilder = Response.status(200);
		
		return responseBuilder.build();
	}
}
