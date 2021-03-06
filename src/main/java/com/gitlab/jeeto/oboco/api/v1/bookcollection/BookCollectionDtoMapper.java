package com.gitlab.jeeto.oboco.api.v1.bookcollection;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookCollectionDtoMapper {
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
