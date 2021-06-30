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
	Instance<BookScannerService> bookScannerServiceProvider;
	
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    	String path = requestContext.getUriInfo().getPath();
    	if(path.startsWith("v1/books") || path.startsWith("v1/bookCollections") || path.startsWith("v1/bookMarks")) {
    		for(BookScannerService bookScannerService: bookScannerServiceProvider) {
    			if(bookScannerService.getStatus().equals(BookScannerServiceStatus.STOPPED) == false) {
    				ResponseBuilder responseBuilder = Response.status(503);
					responseBuilder.entity(new ProblemDto(503, "PROBLEM_BOOK_SCANNER_STATUS_INVALID", "The bookScanner.status is invalid: " + bookScannerService.getStatus() + "."));
		    		
					requestContext.abortWith(responseBuilder.build());
    			}
    		}
    	}
    }
}