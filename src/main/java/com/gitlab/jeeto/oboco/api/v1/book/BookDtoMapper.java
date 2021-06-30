package com.gitlab.jeeto.oboco.api.v1.book;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDtoMapper;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkDto;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkDtoMapper;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkReference;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.Linkable;
import com.gitlab.jeeto.oboco.common.LinkableDto;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.PageableListDto;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookDtoMapper {
	private BookCollectionDtoMapper bookCollectionDtoMapper;
	@Inject
	Provider<BookCollectionDtoMapper> bookCollectionDtoMapperProvider;
	
	private BookCollectionDtoMapper getBookCollectionDtoMapper() {
		if(bookCollectionDtoMapper == null) {
			bookCollectionDtoMapper = bookCollectionDtoMapperProvider.get();
		}
		return bookCollectionDtoMapper;
	}
	
	private BookMarkDtoMapper bookMarkDtoMapper;
	@Inject
	Provider<BookMarkDtoMapper> bookMarkDtoMapperProvider;
	
	private BookMarkDtoMapper getBookMarkDtoMapper() {
		if(bookMarkDtoMapper == null) {
			bookMarkDtoMapper = bookMarkDtoMapperProvider.get();
		}
		return bookMarkDtoMapper;
	}
	
	public BookDto getBookDto(Book book, Graph graph) throws ProblemException {
		BookDto bookDto = null;
		if(book != null) {
			bookDto = new BookDto();
			bookDto.setId(book.getId());
			bookDto.setCreateDate(book.getCreateDate());
			bookDto.setUpdateDate(book.getUpdateDate());
			bookDto.setName(book.getName());
			bookDto.setNumberOfPages(book.getNumberOfPages());
			
			if(graph != null) {
				if(graph.containsKey("bookCollection")) {
					Graph bookCollectionGraph = graph.get("bookCollection");
					
					BookCollection bookCollection = book.getBookCollection();
					BookCollectionDto bookCollectionDto = getBookCollectionDtoMapper().getBookCollectionDto(bookCollection, bookCollectionGraph);
					
					bookDto.setBookCollection(bookCollectionDto);
				}
				
				if(graph.containsKey("bookMark")) {
					Graph bookMarkGraph = graph.get("bookMark");
					
					BookMarkReference bookMarkReference = null;
					
					List<BookMarkReference> bookMarkReferenceList = book.getBookMarkReferences();
					if(bookMarkReferenceList != null && bookMarkReferenceList.size() == 1) {
						bookMarkReference = bookMarkReferenceList.get(0);
					}
					
					BookMarkDto bookMarkDto = getBookMarkDtoMapper().getBookMarkDto(bookMarkReference, bookMarkGraph);
					
					bookDto.setBookMark(bookMarkDto);
				}
			}
		}
		
		return bookDto;
	}
	
	public List<BookDto> getBooksDto(List<Book> bookList, Graph graph) throws ProblemException {
		List<BookDto> bookListDto = null;
		if(bookList != null) {
			bookListDto = new ArrayList<BookDto>();
			
			for(Book book: bookList) {
				BookDto bookDto = getBookDto(book, graph);
				
				bookListDto.add(bookDto);
			}
		}
		
		return bookListDto;
	}
	
	public LinkableDto<BookDto> getBooksDto(Linkable<Book> bookLinkable, Graph graph) throws ProblemException {
		LinkableDto<BookDto> bookLinkableDto = null;
		if(bookLinkable != null) {
			bookLinkableDto = new LinkableDto<BookDto>();
			
			Book book = bookLinkable.getElement();
			BookDto bookDto = getBookDto(book, graph);
			
			bookLinkableDto.setElement(bookDto);
			
			Book previousBook = bookLinkable.getPreviousElement();
			BookDto previousBookDto = getBookDto(previousBook, graph);
			
			bookLinkableDto.setPreviousElement(previousBookDto);
			
			Book nextBook = bookLinkable.getNextElement();
			BookDto nextBookDto = getBookDto(nextBook, graph);
			
			bookLinkableDto.setNextElement(nextBookDto);
		}
		
		return bookLinkableDto;
	}
	
	public PageableListDto<BookDto> getBooksDto(PageableList<Book> bookPageableList, Graph graph) throws ProblemException {
		PageableListDto<BookDto> bookPageableListDto = null;
		if(bookPageableList != null) {
			bookPageableListDto = new PageableListDto<BookDto>();
			
			List<BookDto> bookListDto = new ArrayList<BookDto>();
			for(Book book: bookPageableList.getElements()) {
				BookDto bookDto = getBookDto(book, graph);
				
				bookListDto.add(bookDto);
			}
			bookPageableListDto.setElements(bookListDto);
			bookPageableListDto.setNumberOfElements(bookPageableList.getNumberOfElements());
			bookPageableListDto.setPage(bookPageableList.getPage());
			bookPageableListDto.setPageSize(bookPageableList.getPageSize());
			bookPageableListDto.setFirstPage(bookPageableList.getFirstPage());
			bookPageableListDto.setLastPage(bookPageableList.getLastPage());
			bookPageableListDto.setPreviousPage(bookPageableList.getPreviousPage());
			bookPageableListDto.setNextPage(bookPageableList.getNextPage());
		}
		
		return bookPageableListDto;
	}
}
