package com.gitlab.jeeto.oboco.common.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.spi.HttpRequest;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionService;
import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.api.v1.user.UserService;
import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class AuthenticationResource {
	@Context
	HttpRequest httpRequest;
	@Inject
	UserService userService;
	@Inject
	UserTokenService userTokenService;
	@Inject
	BookCollectionService bookCollectionService;
	
	@Operation(
		description = "Create a userId by userNamePassword."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "A userId.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserIdDto.class))),
		@APIResponse(responseCode = "400", description = "A problem: PROBLEM_USER_NAME_PASSWORD_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "A problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@Path("")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    public Response createUserIdByUserNamePassword(
    		@Parameter(name = "userNamePassword", description = "A userNamePassword.", required = true) UserNamePasswordDto userNamePasswordDto) throws ProblemException {
		User user = null;
		
		if(userNamePasswordDto.getName().equals("test")) {
			if(userNamePasswordDto.getPassword().equals("test") == false) {
				throw new ProblemException(new Problem(400, "PROBLEM_USER_NAME_PASSWORD_INVALID", "The user.name or user.password is invalid."));
			}
			
			String remoteAddress;
			List<String> remoteAddresses = httpRequest.getHttpHeaders().getRequestHeader("X-Forwarded-For");
			if(remoteAddresses != null) {
				remoteAddress = remoteAddresses.get(0);
			} else {
				remoteAddress = httpRequest.getRemoteAddress();
			}
			
			userNamePasswordDto.setName(userNamePasswordDto.getName() + "-" + remoteAddress);
			
			user = userService.getUserByName(userNamePasswordDto.getName());
			
			if(user == null) {
				user = new User();
				user.setName(userNamePasswordDto.getName());
				user.setPassword(userNamePasswordDto.getPassword());
				
				List<String> roles = new ArrayList<String>();
				roles.add("USER");
				
				user.setRoles(roles);
				user.setUpdateDate(new Date());
				
				BookCollection rootBookCollection = bookCollectionService.getRootBookCollectionByName("DEFAULT");
				
				user.setRootBookCollection(rootBookCollection);
				
				user = userService.createUser(user);
			}
		} else {
			user = userService.getUserByNameAndPassword(userNamePasswordDto.getName(), userNamePasswordDto.getPassword());
			
			if(user == null) {
				throw new ProblemException(new Problem(400, "PROBLEM_USER_NAME_PASSWORD_INVALID", "The user.name or user.password is invalid."));
			}
		}
		
		String idTokenValue = userTokenService.getIdTokenValue(user.getName());
		
		String refreshTokenValue = userTokenService.getRefreshTokenValue(user.getName());
        
		UserIdDto userIdDto = new UserIdDto();
		userIdDto.setName(user.getName());
		userIdDto.setRoles(user.getRoles());
		userIdDto.setIdToken(idTokenValue);
		userIdDto.setRefreshToken(refreshTokenValue);
        
        ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userIdDto);
		
		return responseBuilder.build();
    }
	
	@Operation(
		description = "create a userId by userToken."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "A userId.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserIdDto.class))),
		@APIResponse(responseCode = "400", description = "A problem: PROBLEM_USER_TOKEN_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "A problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
	})
	@POST
    @Path("refresh")
	@Consumes(MediaType.APPLICATION_JSON)
    public Response createUserIdByUserToken(
    		@Parameter(name = "userToken", description = "A userToken.", required = true) UserTokenDto userTokenDto) throws ProblemException {
		String refreshTokenValue = userTokenDto.getToken();
		
		UserToken refreshToken = userTokenService.getRefreshToken(refreshTokenValue);
		
		User user = userService.getUserByName(refreshToken.getName());
		
		if(user == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid."));
		}
		
		if(user.getUpdateDate() == null || user.getUpdateDate().compareTo(refreshToken.getStartDate()) >= 0) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_TOKEN_INVALID", "The userToken is invalid."));
		}
		
        String idTokenValue = userTokenService.getIdTokenValue(user.getName());
        
        UserIdDto userIdDto = new UserIdDto();
		userIdDto.setName(user.getName());
		userIdDto.setRoles(user.getRoles());
		userIdDto.setIdToken(idTokenValue);
		userIdDto.setRefreshToken(refreshTokenValue);
        
        ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userIdDto);
		
		return responseBuilder.build();
    }
}
