package com.gitlab.jeeto.oboco.api;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import com.gitlab.jeeto.oboco.api.v1.book.BookResource;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionResource;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkResource;
import com.gitlab.jeeto.oboco.api.v1.bookscanner.BookScannerResource;
import com.gitlab.jeeto.oboco.api.v1.user.UserResource;
import com.gitlab.jeeto.oboco.common.security.AuthenticationResource;

@OpenAPIDefinition(
    info = @Info(
    		title = "Api.",
    		version = "v1",
            description = "Api."
    ), 
    servers = {
    	@Server(
    		url = "/"
    	)
    }
)
@SecurityScheme(securitySchemeName = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "jwt")
@Path("api/v1")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class ApiResource {
	@Context
	ResourceContext resourceContext;
	
	@Path("books")
	public BookResource getBookResource() {
		return resourceContext.getResource(BookResource.class);
	}
	
	@Path("bookCollections")
	public BookCollectionResource getBookCollectionResource() {
		return resourceContext.getResource(BookCollectionResource.class);
	}
	
	@Path("bookMarks")
	public BookMarkResource getBookMarkResource() {
		return resourceContext.getResource(BookMarkResource.class);
	}
	
	@Path("bookScanners")
	public BookScannerResource getBookScannerResource() {
		return resourceContext.getResource(BookScannerResource.class);
	}
	
	@Path("users")
	public UserResource getUserResource() {
		return resourceContext.getResource(UserResource.class);
	}
	
	@Path("authentication")
	public AuthenticationResource getAuthenticationResource() {
		return resourceContext.getResource(AuthenticationResource.class);
	}
}
