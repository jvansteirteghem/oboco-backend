package com.gitlab.jeeto.oboco.api.v1.bookcollection;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import com.gitlab.jeeto.oboco.api.v1.book.Book;
import com.gitlab.jeeto.oboco.api.v1.book.BookDto;
import com.gitlab.jeeto.oboco.api.v1.book.BookDtoMapper;
import com.gitlab.jeeto.oboco.common.GraphDto;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookCollectionDtoMapper {
	private BookDtoMapper bookDtoMapper;
	@Inject
	Provider<BookDtoMapper> bookDtoMapperProvider;
	
	private BookDtoMapper getBookDtoMapper() {
		if(bookDtoMapper == null) {
			bookDtoMapper = bookDtoMapperProvider.get();
		}
		return bookDtoMapper;
	}
	
	public BookCollectionDto getBookCollectionDto(String userName, BookCollection bookCollection, GraphDto graphDto) throws ProblemException {
		BookCollectionDto bookCollectionDto = null;
		if(bookCollection != null) {
			bookCollectionDto = new BookCollectionDto();
			bookCollectionDto.setId(bookCollection.getId());
			bookCollectionDto.setUpdateDate(bookCollection.getUpdateDate());
			bookCollectionDto.setName(bookCollection.getName());
			bookCollectionDto.setNumberOfBookCollections(bookCollection.getNumberOfBookCollections());
			bookCollectionDto.setNumberOfBooks(bookCollection.getNumberOfBooks());
			
			if(graphDto != null) {
				if(graphDto.containsKey("parentBookCollection")) {
					GraphDto nestedGraphDto = graphDto.get("parentBookCollection");
					
					BookCollection parentBookCollection = bookCollection.getParentBookCollection();
					BookCollectionDto parentBookCollectionDto = getBookCollectionDto(userName, parentBookCollection, nestedGraphDto);
					
					bookCollectionDto.setParentBookCollection(parentBookCollectionDto);
				}
				
				if(graphDto.containsKey("bookCollections")) {
					GraphDto nestedGraphDto = graphDto.get("bookCollections");
					
					List<BookCollection> bookCollectionList = bookCollection.getBookCollections();
					List<BookCollectionDto> bookCollectionListDto = getBookCollectionsDto(userName, bookCollectionList, nestedGraphDto);
					
					bookCollectionDto.setBookCollections(bookCollectionListDto);
				}
				
				if(graphDto.containsKey("books")) {
					GraphDto nestedGraphDto = graphDto.get("books");
					
					List<Book> bookList = bookCollection.getBooks();
					List<BookDto> bookListDto = getBookDtoMapper().getBooksDto(userName, bookList, nestedGraphDto);
					
					bookCollectionDto.setBooks(bookListDto);
				}
			}
		}
		
		return bookCollectionDto;
	}
	
	public List<BookCollectionDto> getBookCollectionsDto(String userName, List<BookCollection> bookCollectionList, GraphDto graphDto) throws ProblemException {
		List<BookCollectionDto> bookCollectionListDto = null;
		if(bookCollectionList != null) {
			bookCollectionListDto = new ArrayList<BookCollectionDto>();
			
			for(BookCollection bookCollection: bookCollectionList) {
				BookCollectionDto bookCollectionDto = getBookCollectionDto(userName, bookCollection, graphDto);
				
				bookCollectionListDto.add(bookCollectionDto);
			}
		}
		
		return bookCollectionListDto;
	}
	
	public PageableListDto<BookCollectionDto> getBookCollectionsDto(String userName, PageableList<BookCollection> bookCollectionPageableList, GraphDto graphDto) throws ProblemException {
		PageableListDto<BookCollectionDto> bookCollectionPageableListDto = null;
		if(bookCollectionPageableList != null) {
			bookCollectionPageableListDto = new PageableListDto<BookCollectionDto>();
			
			List<BookCollectionDto> bookCollectionListDto = new ArrayList<BookCollectionDto>();
			for(BookCollection bookCollection: bookCollectionPageableList.getElements()) {
				BookCollectionDto bookCollectionDto = getBookCollectionDto(userName, bookCollection, graphDto);
				
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
