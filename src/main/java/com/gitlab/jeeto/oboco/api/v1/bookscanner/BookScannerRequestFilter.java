package com.gitlab.jeeto.oboco.api.v1.bookscanner;

import java.io.IOException;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import com.gitlab.jeeto.oboco.common.exception.ProblemDto;

@Provider
@Priority(Priorities.USER)
public class BookScannerRequestFilter implements ContainerRequestFilter {
	@Inject
	Instance<BookScanner> bookScannerProvider;
	
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    	String path = requestContext.getUriInfo().getPath();
    	// starts with /api/
    	if(path.startsWith("/api/v1/books") || path.startsWith("/api/v1/bookCollections") || path.startsWith("/api/v1/bookMarks")) {
    		for(BookScanner bookScanner: bookScannerProvider) {
    			if(BookScannerStatus.STOPPED.equals(bookScanner.getStatus()) == false) {
    				ResponseBuilder responseBuilder = Response.status(503);
					responseBuilder.entity(new ProblemDto(503, "PROBLEM_BOOK_SCANNER_STATUS_INVALID", "The bookScanner.status is invalid: " + bookScanner.getStatus() + "."));
		    		
					requestContext.abortWith(responseBuilder.build());
    			}
    		}
    	}
    }
}