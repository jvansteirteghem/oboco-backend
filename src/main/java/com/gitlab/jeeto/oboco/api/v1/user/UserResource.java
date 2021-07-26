package com.gitlab.jeeto.oboco.api.v1.user;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDtoMapper;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionPageableListDto;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionService;
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

@SecurityRequirement(name = "bearerAuth")
@Authentication(type = "BEARER")
@Authorization(roles = { "ADMINISTRATOR" })
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	@Context
    SecurityContext securityContext;
	@Inject
	UserService userService;
	@Inject
	UserDtoMapper userDtoMapper;
	@Inject
	BookCollectionService bookCollectionService;
	@Inject
	BookCollectionDtoMapper bookCollectionDtoMapper;
	
	@Operation(
		description = "Get the authenticated user."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The authenticated user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Authorization(roles = { "ADMINISTRATOR", "USER" })
	@Path("ME")
	@GET
	public Response getAuthenticatedUser(
			@Parameter(name = "graph", description = "The graph. The full graph is (rootBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(rootBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		String userName = securityContext.getUserPrincipal().getName();
		
		User user = userService.getUserByName(userName, graph);
		
		if(user == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_NOT_FOUND", "The user is not found."));
		}
		
		UserDto userDto = userDtoMapper.getUserDto(user, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Update the password of the authenticated user."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The authenticated user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_USER_PASSWORD_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Authorization(roles = { "ADMINISTRATOR", "USER" })
	@Path("ME/password")
	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAuthenticatedUserPassword( 
			@Parameter(name = "userPassword", description = "The userPassword.", required = true) UserPasswordDto userPasswordDto) throws ProblemException {
		Graph graph = GraphHelper.createGraph("()");
		Graph fullGraph = GraphHelper.createGraph("(rootBookCollection)");

		GraphHelper.validateGraph(graph, fullGraph);
		
		String userName = securityContext.getUserPrincipal().getName();
		
		User user = userService.getUserByName(userName, null);
		
		if(user == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_NOT_FOUND", "The user is not found."));
		}
		
		user = userService.getUserByNameAndPassword(user, userPasswordDto.getPassword());
		
		if(user == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_PASSWORD_INVALID", "The user.password is invalid."));
		}
		
		if(userPasswordDto.getUpdatePassword() == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_PASSWORD_INVALID", "The user.password is invalid: user.password is null."));
		}
		
		if(userPasswordDto.getUpdatePassword().equals("")) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_PASSWORD_INVALID", "The user.password is invalid: user.password is ''."));
		}
		
		user.setPassword(userPasswordDto.getUpdatePassword());
		
		Date updateDate = new Date();
		
		user.setUpdateDate(updateDate);
		
		user = userService.updateUser(user, graph);
		
		UserDto userDto = userDtoMapper.getUserDto(user, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Create the user."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_USER_NAME_INVALID, PROBLEM_USER_PASSWORD_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(
			@Parameter(name = "user", description = "The user.", required = true) UserDto userDto) throws ProblemException {
		Graph graph = GraphHelper.createGraph("()");
		Graph fullGraph = GraphHelper.createGraph("(rootBookCollection)");

		GraphHelper.validateGraph(graph, fullGraph);
		
		if(userDto.getName() == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_NAME_INVALID", "The user.name is invalid: user.name is null."));
		}
		
		if(userDto.getName().equals("")) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_NAME_INVALID", "The user.name is invalid: user.name is ''."));
		}
		
		User user = userService.getUserByName(userDto.getName(), null);
		
		if(user != null) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_NAME_INVALID", "The user.name is invalid: user.name is not unique."));
		}
		
		if(userDto.getPassword() == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_PASSWORD_INVALID", "The user.password is invalid: user.password is null."));
		}
		
		if(userDto.getPassword().equals("")) {
			throw new ProblemException(new Problem(400, "PROBLEM_USER_PASSWORD_INVALID", "The user.password is invalid: user.password is ''."));
		}
		
		user = new User();
		user.setName(userDto.getName());
		user.setPassword(userDto.getPassword());
		user.setRoles(userDto.getRoles());
		
		Date updateDate = new Date();
		
		user.setCreateDate(updateDate);
		user.setUpdateDate(updateDate);
		
		if(userDto.getRootBookCollection() != null) {
			BookCollection rootBookCollection = bookCollectionService.getRootBookCollectionById(userDto.getRootBookCollection().getId(), null);
			
			user.setRootBookCollection(rootBookCollection);
		}
		
		user = userService.createUser(user, graph);
		
		userDto = userDtoMapper.getUserDto(user, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Update the user."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_USER_PASSWORD_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("{userId}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(
			@Parameter(name = "userId", description = "The id of the user.", required = true) @PathParam("userId") Long userId, 
			@Parameter(name = "user", description = "The user.", required = true) UserDto userDto) throws ProblemException {
		Graph graph = GraphHelper.createGraph("()");
		Graph fullGraph = GraphHelper.createGraph("(rootBookCollection)");

		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = userService.getUserById(userId, null);
		
		if(user == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_NOT_FOUND", "The user is not found."));
		}
		
		if(userDto.getPassword() != null) {
			if(userDto.getPassword().equals("")) {
				throw new ProblemException(new Problem(400, "PROBLEM_USER_PASSWORD_INVALID", "The user.password is invalid: user.password is ''."));
			}
		}
		
		user.setPassword(userDto.getPassword());
		user.setRoles(userDto.getRoles());
		
		Date updateDate = new Date();
		
		user.setUpdateDate(updateDate);
		
		if(userDto.getRootBookCollection() != null) {
			BookCollection rootBookCollection = bookCollectionService.getRootBookCollectionById(userDto.getRootBookCollection().getId(), null);
			
			user.setRootBookCollection(rootBookCollection);
		}
		
		user = userService.updateUser(user, graph);
		
		userDto = userDtoMapper.getUserDto(user, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Delete the user."
	)
	@APIResponses({
		@APIResponse(responseCode = "200"),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("{userId}")
	@DELETE
	public Response deleteUser(
			@Parameter(name = "userId", description = "The id of the user.", required = true) @PathParam("userId") Long userId) throws ProblemException {
		User user = userService.getUserById(userId, null);
		
		if(user == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_NOT_FOUND", "The user is not found."));
		}
		
		userService.deleteUser(user);
		
		ResponseBuilder responseBuilder = Response.status(200);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the users."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The pageable list of users.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPageableListDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_PAGE_INVALID, PROBLEM_PAGE_SIZE_INVALID, PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("")
	@GET
	public Response getUsers(
			@Parameter(name = "page", description = "The page. The page is >= 1.", required = false) @DefaultValue("1") @QueryParam("page") Integer page, 
			@Parameter(name = "pageSize", description = "The pageSize. The pageSize is >= 1 and <= 100.", required = false) @DefaultValue("25") @QueryParam("pageSize") Integer pageSize, 
			@Parameter(name = "graph", description = "The graph. The full graph is (rootBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		PageableListDtoHelper.validatePageableList(page, pageSize);
		
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(rootBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		PageableList<User> userPageableList = userService.getUsers(page, pageSize, graph);
		PageableListDto<UserDto> userPageableListDto = userDtoMapper.getUsersDto(userPageableList, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userPageableListDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the user."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "404", description = "The problem: PROBLEM_USER_NOT_FOUND", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("{userId}")
	@GET
	public Response getUser(
			@Parameter(name = "userId", description = "The id of the user.", required = true) @PathParam("userId") Long userId, 
			@Parameter(name = "graph", description = "The graph. The full graph is (rootBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(rootBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		User user = userService.getUserById(userId, graph);
		
		if(user == null) {
			throw new ProblemException(new Problem(404, "PROBLEM_USER_NOT_FOUND", "The user is not found."));
		}
		
		UserDto userDto = userDtoMapper.getUserDto(user, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(userDto);
		
		return responseBuilder.build();
	}
	
	@Operation(
		description = "Get the root bookCollections."
	)
	@APIResponses({
		@APIResponse(responseCode = "200", description = "The bookCollections.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookCollectionPageableListDto.class))),
		@APIResponse(responseCode = "400", description = "The problem: PROBLEM_GRAPH_INVALID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "401", description = "The problem: PROBLEM_USER_NOT_AUTHENTICATED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "403", description = "The problem: PROBLEM_USER_NOT_AUTHORIZED", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class))),
		@APIResponse(responseCode = "500", description = "The problem: PROBLEM", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDto.class)))
    })
	@Path("bookCollections")
	@GET
	public Response getRootBookCollections(
			@Parameter(name = "graph", description = "The graph. The full graph is (parentBookCollection).", required = false) @DefaultValue("()") @QueryParam("graph") String graphValue) throws ProblemException {
		Graph graph = GraphHelper.createGraph(graphValue);
		Graph fullGraph = GraphHelper.createGraph("(parentBookCollection)");
		
		GraphHelper.validateGraph(graph, fullGraph);
		
		List<BookCollection> bookCollectionList = bookCollectionService.getRootBookCollections(graph);
		
		List<BookCollectionDto> bookCollectionListDto = bookCollectionDtoMapper.getBookCollectionsDto(bookCollectionList, graph);
		
		ResponseBuilder responseBuilder = Response.status(200);
		responseBuilder.entity(bookCollectionListDto);
		
		return responseBuilder.build();
	}
}
