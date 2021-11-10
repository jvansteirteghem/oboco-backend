package com.gitlab.jeeto.oboco.api.v1.bookcollection;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import com.gitlab.jeeto.oboco.api.v1.bookmark.BookCollectionMark;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookCollectionMarkDto;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookCollectionMarkDtoMapper;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;


@RequestScoped
public class BookCollectionDtoMapper {
	private BookCollectionMarkDtoMapper bookCollectionMarkDtoMapper;
	@Inject
	Provider<BookCollectionMarkDtoMapper> bookCollectionMarkDtoMapperProvider;
	
	private BookCollectionMarkDtoMapper getBookCollectionMarkDtoMapper() {
		if(bookCollectionMarkDtoMapper == null) {
			bookCollectionMarkDtoMapper = bookCollectionMarkDtoMapperProvider.get();
		}
		return bookCollectionMarkDtoMapper;
	}
	
	public BookCollectionDto getBookCollectionDto(BookCollection bookCollection, Graph graph) throws ProblemException {
		BookCollectionDto bookCollectionDto = null;
		if(bookCollection != null) {
			bookCollectionDto = new BookCollectionDto();
			bookCollectionDto.setId(bookCollection.getId());
			bookCollectionDto.setCreateDate(bookCollection.getCreateDate());
			bookCollectionDto.setUpdateDate(bookCollection.getUpdateDate());
			bookCollectionDto.setName(bookCollection.getName());
			bookCollectionDto.setNumberOfBookCollections(bookCollection.getNumberOfBookCollections());
			bookCollectionDto.setNumberOfBooks(bookCollection.getNumberOfBooks());
			
			if(graph != null) {
				if(graph.containsKey("parentBookCollection")) {
					Graph parentBookCollectionGraph = graph.get("parentBookCollection");
					
					BookCollection parentBookCollection = bookCollection.getParentBookCollection();
					BookCollectionDto parentBookCollectionDto = getBookCollectionDto(parentBookCollection, parentBookCollectionGraph);
					
					bookCollectionDto.setParentBookCollection(parentBookCollectionDto);
				}
				
				if(graph.containsKey("bookCollectionMark")) {
					Graph bookCollectionMarkGraph = graph.get("bookCollectionMark");
					
					BookCollectionMark bookCollectionMark = null;
					
					List<BookCollectionMark> bookCollectionMarkList = bookCollection.getBookCollectionMarks();
					if(bookCollectionMarkList != null && bookCollectionMarkList.size() == 1) {
						bookCollectionMark = bookCollectionMarkList.get(0);
					}
					
					BookCollectionMarkDto bookCollectionMarkDto = getBookCollectionMarkDtoMapper().getBookCollectionMarkDto(bookCollectionMark, bookCollectionMarkGraph);
					
					bookCollectionDto.setBookCollectionMark(bookCollectionMarkDto);
				}
			}
		}
		
		return bookCollectionDto;
	}
	
	public List<BookCollectionDto> getBookCollectionsDto(List<BookCollection> bookCollectionList, Graph graph) throws ProblemException {
		List<BookCollectionDto> bookCollectionListDto = null;
		if(bookCollectionList != null) {
			bookCollectionListDto = new ArrayList<BookCollectionDto>();
			
			for(BookCollection bookCollection: bookCollectionList) {
				BookCollectionDto bookCollectionDto = getBookCollectionDto(bookCollection, graph);
				
				bookCollectionListDto.add(bookCollectionDto);
			}
		}
		
		return bookCollectionListDto;
	}
	
	public PageableListDto<BookCollectionDto> getBookCollectionsDto(PageableList<BookCollection> bookCollectionPageableList, Graph graph) throws ProblemException {
		PageableListDto<BookCollectionDto> bookCollectionPageableListDto = null;
		if(bookCollectionPageableList != null) {
			bookCollectionPageableListDto = new PageableListDto<BookCollectionDto>();
			
			List<BookCollectionDto> bookCollectionListDto = new ArrayList<BookCollectionDto>();
			for(BookCollection bookCollection: bookCollectionPageableList.getElements()) {
				BookCollectionDto bookCollectionDto = getBookCollectionDto(bookCollection, graph);
				
				bookCollectionListDto.add(bookCollectionDto);
			}
			bookCollectionPageableListDto.setElements(bookCollectionListDto);
			bookCollectionPageableListDto.setNumberOfElements(bookCollectionPageableList.getNumberOfElements());
			bookCollectionPageableListDto.setPage(bookCollectionPageableList.getPage());
			bookCollectionPageableListDto.setPageSize(bookCollectionPageableList.getPageSize());
			bookCollectionPageableListDto.setFirstPage(bookCollectionPageableList.getFirstPage());
			bookCollectionPageableListDto.setLastPage(bookCollectionPageableList.getLastPage());
			bookCollectionPageableListDto.setPreviousPage(bookCollectionPageableList.getPreviousPage());
			bookCollectionPageableListDto.setNextPage(bookCollectionPageableList.getNextPage());
		}
		
		return bookCollectionPageableListDto;
	}
}
