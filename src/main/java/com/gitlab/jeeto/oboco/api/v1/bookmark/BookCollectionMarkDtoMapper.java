package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDtoMapper;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookCollectionMarkDtoMapper {
	private BookCollectionDtoMapper bookCollectionDtoMapper;
	@Inject
	Provider<BookCollectionDtoMapper> bookCollectionDtoMapperProvider;
	
	private BookCollectionDtoMapper getBookCollectionDtoMapper() {
		if(bookCollectionDtoMapper == null) {
			bookCollectionDtoMapper = bookCollectionDtoMapperProvider.get();
		}
		return bookCollectionDtoMapper;
	}
	
	public BookCollectionMarkDto getBookCollectionMarkDto(BookCollectionMark bookCollectionMark, Graph graph) throws ProblemException {
		BookCollectionMarkDto bookCollectionMarkDto = null;
		if(bookCollectionMark != null) {
			bookCollectionMarkDto = new BookCollectionMarkDto();
			bookCollectionMarkDto.setId(bookCollectionMark.getId());
			bookCollectionMarkDto.setCreateDate(bookCollectionMark.getCreateDate());
			bookCollectionMarkDto.setUpdateDate(bookCollectionMark.getUpdateDate());
			bookCollectionMarkDto.setNumberOfBookPages(bookCollectionMark.getNumberOfBookPages());
			bookCollectionMarkDto.setBookPage(bookCollectionMark.getBookPage());
			
			if(graph != null) {
				if(graph.containsKey("bookCollection")) {
					Graph bookCollectionGraph = graph.get("bookCollection");
					
					BookCollection bookCollection = bookCollectionMark.getBookCollection();
					BookCollectionDto bookCollectionDto = getBookCollectionDtoMapper().getBookCollectionDto(bookCollection, bookCollectionGraph);
					
					bookCollectionMarkDto.setBookCollection(bookCollectionDto);
				}
			}
		}
		
		return bookCollectionMarkDto;
	}
	
	public PageableListDto<BookCollectionMarkDto> getBookCollectionMarksDto(PageableList<BookCollectionMark> bookCollectionMarkPageableList, Graph graph) throws ProblemException {
		PageableListDto<BookCollectionMarkDto> bookCollectionMarkPageableListDto = null;
		if(bookCollectionMarkPageableList != null) {
			bookCollectionMarkPageableListDto = new PageableListDto<BookCollectionMarkDto>();
			
			List<BookCollectionMarkDto> bookCollectionMarkListDto = new ArrayList<BookCollectionMarkDto>();
			for(BookCollectionMark bookCollectionMark: bookCollectionMarkPageableList.getElements()) {
				BookCollectionMarkDto bookCollectionMarkDto = getBookCollectionMarkDto(bookCollectionMark, graph);
				
				bookCollectionMarkListDto.add(bookCollectionMarkDto);
			}
			bookCollectionMarkPageableListDto.setElements(bookCollectionMarkListDto);
			bookCollectionMarkPageableListDto.setNumberOfElements(bookCollectionMarkPageableList.getNumberOfElements());
			bookCollectionMarkPageableListDto.setPage(bookCollectionMarkPageableList.getPage());
			bookCollectionMarkPageableListDto.setPageSize(bookCollectionMarkPageableList.getPageSize());
			bookCollectionMarkPageableListDto.setFirstPage(bookCollectionMarkPageableList.getFirstPage());
			bookCollectionMarkPageableListDto.setLastPage(bookCollectionMarkPageableList.getLastPage());
			bookCollectionMarkPageableListDto.setPreviousPage(bookCollectionMarkPageableList.getPreviousPage());
			bookCollectionMarkPageableListDto.setNextPage(bookCollectionMarkPageableList.getNextPage());
		}
		
		return bookCollectionMarkPageableListDto;
	}
}
