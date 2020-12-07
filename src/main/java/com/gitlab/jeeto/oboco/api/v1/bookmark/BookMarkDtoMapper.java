package com.gitlab.jeeto.oboco.api.v1.bookmark;

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
public class BookMarkDtoMapper {
	private BookDtoMapper bookDtoMapper;
	@Inject
	Provider<BookDtoMapper> bookDtoMapperProvider;
	
	private BookDtoMapper getBookDtoMapper() {
		if(bookDtoMapper == null) {
			bookDtoMapper = bookDtoMapperProvider.get();
		}
		return bookDtoMapper;
	}
	
	public BookMarkDto getBookMarkDto(String userName, BookMarkReference bookMarkReference, GraphDto graphDto) throws ProblemException {
		BookMarkDto bookMarkDto = null;
		if(bookMarkReference != null) {
			bookMarkDto = new BookMarkDto();
			bookMarkDto.setId(bookMarkReference.getId());
			
			BookMark bookMark = bookMarkReference.getBookMark();
			if(bookMark != null) {
				bookMarkDto.setUpdateDate(bookMark.getUpdateDate());
				bookMarkDto.setPage(bookMark.getPage());
			}
			
			if(graphDto != null) {
				if(graphDto.containsKey("book")) {
					GraphDto nestedGraphDto = graphDto.get("book");
					
					Book book = bookMarkReference.getBook();
					BookDto bookDto = getBookDtoMapper().getBookDto(userName, book, nestedGraphDto);
					
					bookMarkDto.setBook(bookDto);
				}
			}
		}
		
		return bookMarkDto;
	}
	
	public PageableListDto<BookMarkDto> getBookMarksDto(String userName, PageableList<BookMarkReference> bookMarkReferencePageableList, GraphDto graphDto) throws ProblemException {
		PageableListDto<BookMarkDto> bookMarkPageableListDto = null;
		if(bookMarkReferencePageableList != null) {
			bookMarkPageableListDto = new PageableListDto<BookMarkDto>();
			
			List<BookMarkDto> bookMarkListDto = new ArrayList<BookMarkDto>();
			for(BookMarkReference bookMarkReference: bookMarkReferencePageableList.getElements()) {
				BookMarkDto bookMarkDto = getBookMarkDto(userName, bookMarkReference, graphDto);
				
				bookMarkListDto.add(bookMarkDto);
			}
			bookMarkPageableListDto.setElements(bookMarkListDto);
			bookMarkPageableListDto.setNumberOfElements(bookMarkReferencePageableList.getNumberOfElements());
			bookMarkPageableListDto.setPage(bookMarkReferencePageableList.getPage());
			bookMarkPageableListDto.setPageSize(bookMarkReferencePageableList.getPageSize());
			bookMarkPageableListDto.setFirstPage(bookMarkReferencePageableList.getFirstPage());
			bookMarkPageableListDto.setLastPage(bookMarkReferencePageableList.getLastPage());
			bookMarkPageableListDto.setPreviousPage(bookMarkReferencePageableList.getPreviousPage());
			bookMarkPageableListDto.setNextPage(bookMarkReferencePageableList.getNextPage());
		}
		
		return bookMarkPageableListDto;
	}
}
