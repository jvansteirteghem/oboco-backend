package com.gitlab.jeeto.oboco.api.v1.user;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDtoMapper;
import com.gitlab.jeeto.oboco.common.GraphDto;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class UserDtoMapper {
	private BookCollectionDtoMapper bookCollectionDtoMapper;
	@Inject
	Provider<BookCollectionDtoMapper> bookCollectionDtoMapperProvider;
	
	private BookCollectionDtoMapper getBookCollectionDtoMapper() {
		if(bookCollectionDtoMapper == null) {
			bookCollectionDtoMapper = bookCollectionDtoMapperProvider.get();
		}
		return bookCollectionDtoMapper;
	}
	
	public UserDto getUserDto(User user, GraphDto graphDto) throws ProblemException {
		UserDto userDto = null;
		if(user != null) {
			userDto = new UserDto();
			userDto.setId(user.getId());
			userDto.setName(user.getName());
			userDto.setRoles(user.getRoles());
			userDto.setUpdateDate(user.getUpdateDate());
			
			if(graphDto != null) {
				if(graphDto.containsKey("rootBookCollection")) {
					GraphDto nestedGraphDto = graphDto.get("rootBookCollection");
					
					BookCollection rootBookCollection = user.getRootBookCollection();
					BookCollectionDto rootBookCollectionDto = getBookCollectionDtoMapper().getBookCollectionDto(user, rootBookCollection, nestedGraphDto);
					
					userDto.setRootBookCollection(rootBookCollectionDto);
				}
			}
		}
		
		return userDto;
	}
	
	public PageableListDto<UserDto> getUsersDto(PageableList<User> userPageableList, GraphDto graphDto) throws ProblemException {
		PageableListDto<UserDto> userPageableListDto = null;
		if(userPageableList != null) {
			userPageableListDto = new PageableListDto<UserDto>();
			
			List<UserDto> userListDto = new ArrayList<UserDto>();
			for(User user: userPageableList.getElements()) {
				UserDto userDto = getUserDto(user, graphDto);
				
				userListDto.add(userDto);
			}
			userPageableListDto.setElements(userListDto);
			userPageableListDto.setNumberOfElements(userPageableList.getNumberOfElements());
			userPageableListDto.setPage(userPageableList.getPage());
			userPageableListDto.setPageSize(userPageableList.getPageSize());
			userPageableListDto.setFirstPage(userPageableList.getFirstPage());
			userPageableListDto.setLastPage(userPageableList.getLastPage());
			userPageableListDto.setPreviousPage(userPageableList.getPreviousPage());
			userPageableListDto.setNextPage(userPageableList.getNextPage());
		}
		
		return userPageableListDto;
	}
}
