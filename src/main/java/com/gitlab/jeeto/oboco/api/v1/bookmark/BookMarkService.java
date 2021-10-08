package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Subgraph;
import javax.transaction.Transactional;

import com.gitlab.jeeto.oboco.api.v1.book.Book;
import com.gitlab.jeeto.oboco.api.v1.book.BookService;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookMarkService {
	@Inject
	EntityManager entityManager;
	private BookService bookService;
	@Inject
	Provider<BookService> bookServiceProvider;
	
	private BookService getBookService() {
		if(bookService == null) {
			bookService = bookServiceProvider.get();
		}
		return bookService;
	}
	
	public BookMarkService() {
		super();
	}
	
	public BookMark getLatestBookMarkByUser(User user) throws ProblemException {
		BookMark bookMark = null;
		
		try {
			bookMark = entityManager.createQuery("select bm from BookMark bm join bm.bookMarkReferences bmr where bmr.book.rootBookCollection.id = :rootBookCollectionId and bmr.bookMark.user.id = :userId order by bm.updateDate desc", BookMark.class)
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.setParameter("userId", user.getId())
				.setMaxResults(1)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
		return bookMark;
	}
	
	public BookMarkReference getBookMarkReferenceByUserAndBook(User user, Book book, Graph graph) throws ProblemException {
		BookMarkReference bookMarkReference = null;
		
		try {
			EntityGraph<BookMarkReference> entityGraph = entityManager.createEntityGraph(BookMarkReference.class);
			entityGraph.addSubgraph("bookMark", BookMark.class);
			
			if(graph != null) {
				if(graph.containsKey("book")) {
					Subgraph<Book> bookEntityGraph = entityGraph.addSubgraph("book", Book.class);
					
					Graph bookGraph = graph.get("book");
					if(bookGraph != null) {
						if(bookGraph.containsKey("bookCollection")) {
							bookEntityGraph.addSubgraph("bookCollection", BookCollection.class);
						}
					}
				}
			}
			
			bookMarkReference = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.book.rootBookCollection.id = :rootBookCollectionId and bmr.bookMark.user.id = :userId and bmr.book.id = :bookId", BookMarkReference.class)
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.setParameter("userId", user.getId())
				.setParameter("bookId", book.getId())
				.setHint("javax.persistence.loadgraph", entityGraph)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMarkReference;
	}
	
	public PageableList<BookMarkReference> getBookMarkReferencesByUser(User user, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookMarkReference> entityGraph = entityManager.createEntityGraph(BookMarkReference.class);
		entityGraph.addSubgraph("bookMark", BookMark.class);
		
		if(graph != null) {
			if(graph.containsKey("book")) {
				Subgraph<Book> bookEntityGraph = entityGraph.addSubgraph("book", Book.class);
				
				Graph bookGraph = graph.get("book");
				if(bookGraph != null) {
					if(bookGraph.containsKey("bookCollection")) {
						bookEntityGraph.addSubgraph("bookCollection", BookCollection.class);
					}
				}
			}
		}
		
		Long bookMarkListSize = (Long) entityManager.createQuery("select count(bmr.id) from BookMarkReference bmr where bmr.book.rootBookCollection.id = :rootBookCollectionId and bmr.bookMark.user.id = :userId")
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.setParameter("userId", user.getId())
				.getSingleResult();
		
		List<BookMarkReference> bookMarkList = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.book.rootBookCollection.id = :rootBookCollectionId and bmr.bookMark.user.id = :userId order by bmr.bookMark.updateDate desc", BookMarkReference.class)
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.setParameter("userId", user.getId())
				.setHint("javax.persistence.loadgraph", entityGraph)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
        
        PageableList<BookMarkReference> bookMarkPageableList = new PageableList<BookMarkReference>(bookMarkList, bookMarkListSize, page, pageSize);
        
        return bookMarkPageableList;
	}
	
	public List<BookMarkReference> getBookMarkReferencesByBook(Long bookId) throws ProblemException {
		List<BookMarkReference> bookMarkReferenceList = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.book.id = :bookId", BookMarkReference.class)
				.setParameter("bookId", bookId)
				.getResultList();
		
        return bookMarkReferenceList;
	}
	
	@Transactional
	public void deleteBookMarkReferences(Date updateDate) throws ProblemException {
		entityManager.createQuery("delete from BookMarkReference bmr where bmr.updateDate != :updateDate")
			.setParameter("updateDate", updateDate)
			.executeUpdate();
	}
	
	@Transactional
	public void deleteBookMarksByUser(User user) throws ProblemException {
		entityManager.createQuery("delete from BookMarkReference bmr where bmr.bookMark.user.id = :userId")
			.setParameter("userId", user.getId())
			.executeUpdate();
			
		entityManager.createQuery("delete from BookMark bm where bm.user.id = :userId")
			.setParameter("userId", user.getId())
			.executeUpdate();
	}
	
	public BookCollectionMark getBookCollectionMarkByUserAndBookCollection(User user, Long bookCollectionId) throws ProblemException {
		BookCollectionMark bookCollectionMark = null;
		
		try {
			bookCollectionMark = entityManager.createQuery("select bcm from BookCollectionMark bcm where bcm.user.id = :userId and bcm.bookCollection.id = :bookCollectionId", BookCollectionMark.class)
				.setParameter("userId", user.getId())
				.setParameter("bookCollectionId", bookCollectionId)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookCollectionMark;
	}
	
	public BookMark getBookMarkByUserAndFile(User user, String fileId) throws ProblemException {
		BookMark bookMark = null;
		
		try {
			bookMark = entityManager.createQuery("select bm from BookMark bm where bm.user.id = :userId and bm.fileId = :fileId", BookMark.class)
				.setParameter("userId", user.getId())
				.setParameter("fileId", fileId)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMark;
	}
	
	public List<BookMark> getBookMarksByFile(String fileId) throws ProblemException {
		List<BookMark> bookMarkList = entityManager.createQuery("select bm from BookMark bm where bm.fileId = :fileId", BookMark.class)
				.setParameter("fileId", fileId)
				.getResultList();
		
        return bookMarkList;
	}
	
	@Transactional
	public void createBookMarkReferencesByBook(Book book) throws ProblemException {
		List<BookMark> bookMarkList = getBookMarksByFile(book.getFileId());
		
		for(BookMark bookMark: bookMarkList) {
			BookMarkReference bookMarkReference = new BookMarkReference();
			bookMarkReference.setBook(book);
			bookMarkReference.setBookMark(bookMark);
			bookMarkReference.setCreateDate(book.getUpdateDate());
			bookMarkReference.setUpdateDate(book.getUpdateDate());
			
			entityManager.persist(bookMarkReference);
			
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(bookMark.getUser(), book.getBookCollection());
		}
	}
	
	@Transactional
	public void updateBookMarkReferencesByBook(Book book) throws ProblemException {
		List<BookMarkReference> bookMarkReferenceList = getBookMarkReferencesByBook(book.getId());
		
		for(BookMarkReference bookMarkReference: bookMarkReferenceList) {
			bookMarkReference.setUpdateDate(book.getUpdateDate());
			
			bookMarkReference = entityManager.merge(bookMarkReference);
		}
	}
	
	public void createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(User user, BookCollection bookCollection) throws ProblemException {
		BookCollectionMark bookCollectionMark = getBookCollectionMarkByUserAndBookCollection(user, bookCollection.getId());
		
		try {
			Query bookCollectionMarkQuery = entityManager.createQuery("select min(bm.createDate), max(bm.updateDate), (select sum(b.numberOfPages) from Book b where b.rootBookCollection.id = :rootBookCollectionId and b.bookCollection.id = :bookCollectionId), sum(bm.page) from BookMark bm join bm.bookMarkReferences bmr where bmr.book.rootBookCollection.id = :rootBookCollectionId and bmr.book.bookCollection.id = :bookCollectionId and bm.user.id = :userId");
			bookCollectionMarkQuery.setParameter("userId", user.getId());
			bookCollectionMarkQuery.setParameter("rootBookCollectionId", bookCollection.getRootBookCollection().getId());
			bookCollectionMarkQuery.setParameter("bookCollectionId", bookCollection.getId());
			
			Object[] bookCollectionMarkObject = (Object[]) bookCollectionMarkQuery.getSingleResult();
			
			if(bookCollectionMark == null) {
				bookCollectionMark = new BookCollectionMark();
				bookCollectionMark.setUser(user);
				bookCollectionMark.setBookCollection(bookCollection);
				bookCollectionMark.setCreateDate((Date) bookCollectionMarkObject[0]);
				bookCollectionMark.setUpdateDate((Date) bookCollectionMarkObject[1]);
				bookCollectionMark.setNumberOfPages(((Long) bookCollectionMarkObject[2]).intValue());
				bookCollectionMark.setPage(((Long) bookCollectionMarkObject[3]).intValue());
				
				entityManager.persist(bookCollectionMark);
			} else {
				bookCollectionMark.setUpdateDate((Date) bookCollectionMarkObject[1]);
				bookCollectionMark.setPage(((Long) bookCollectionMarkObject[3]).intValue());
				
				bookCollectionMark = entityManager.merge(bookCollectionMark);
			}
		} catch(NoResultException e) {
			if(bookCollectionMark != null) {
				entityManager.remove(bookCollectionMark);
			}
		}
	}
	
	public boolean hasBookCollection(List<BookCollection> bookCollectionList, Long bookCollectionId) {
		for(BookCollection referencedBookCollection: bookCollectionList) {
			if(referencedBookCollection.getId().equals(bookCollectionId)) {
				return true;
			}
		}
		return false;
	}
	
	@Transactional
	public void createOrUpdateBookMarksByUserAndBookCollection(User user, BookCollection bookCollection) throws ProblemException {
		Date updateDate = new Date();
		
		List<BookCollection> referencedBookCollectionList = new ArrayList<BookCollection>();
		
		List<Book> bookList = getBookService().getBooksByUserAndBookCollection(user, bookCollection.getId());
		
		for(Book book: bookList) {
			BookMark bookMark = getBookMarkByUserAndFile(user, book.getFileId());
			
			if(bookMark == null) {
				bookMark = new BookMark();
				bookMark.setUser(user);
				bookMark.setFileId(book.getFileId());
				bookMark.setCreateDate(updateDate);
				bookMark.setUpdateDate(updateDate);
				bookMark.setNumberOfPages(book.getNumberOfPages());
				bookMark.setPage(book.getNumberOfPages());
				
				entityManager.persist(bookMark);
				
				List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
				
				for(Book referencedBook: referencedBookList) {
					BookMarkReference bookMarkReference = new BookMarkReference();
					bookMarkReference.setBook(referencedBook);
					bookMarkReference.setBookMark(bookMark);
					bookMarkReference.setCreateDate(updateDate);
					bookMarkReference.setUpdateDate(updateDate);
					
					entityManager.persist(bookMarkReference);
					
					if(hasBookCollection(referencedBookCollectionList, referencedBook.getBookCollection().getId()) == false ) {
						referencedBookCollectionList.add(referencedBook.getBookCollection());
					}
				}
			} else {
				bookMark.setUpdateDate(updateDate);
				bookMark.setPage(book.getNumberOfPages());
				
				bookMark = entityManager.merge(bookMark);
				
				List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
				
				for(Book referencedBook: referencedBookList) {
					if(hasBookCollection(referencedBookCollectionList, referencedBook.getBookCollection().getId()) == false ) {
						referencedBookCollectionList.add(referencedBook.getBookCollection());
					}
				}
			}
			
			for(BookCollection referencedBookCollection: referencedBookCollectionList) {
				createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
			}
		}
	}
	
	@Transactional
	public void deleteBookMarksByUserAndBookCollection(User user, BookCollection bookCollection) throws ProblemException {
		List<BookCollection> referencedBookCollectionList = new ArrayList<BookCollection>();
		
		List<Book> bookList = getBookService().getBooksByUserAndBookCollection(user, bookCollection.getId());
		
		for(Book book: bookList) {
			BookMark bookMark = getBookMarkByUserAndFile(user, book.getFileId());
			
			if(bookMark != null) {
				entityManager.remove(bookMark);
				
				List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
				
				for(Book referencedBook: referencedBookList) {
					if(hasBookCollection(referencedBookCollectionList, referencedBook.getBookCollection().getId()) == false ) {
						referencedBookCollectionList.add(referencedBook.getBookCollection());
					}
				}
			}
		}
		
		for(BookCollection referencedBookCollection: referencedBookCollectionList) {
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
		}
	}
	
	@Transactional
	public BookMarkReference createOrUpdateBookMarkByUserAndBook(User user, Book book, Integer bookPage, Graph graph) throws ProblemException {
		Date updateDate = new Date();
		
		List<BookCollection> referencedBookCollectionList = new ArrayList<BookCollection>();
		
		BookMark bookMark = getBookMarkByUserAndFile(user, book.getFileId());
		
		if(bookMark == null) {
			bookMark = new BookMark();
			bookMark.setUser(user);
			bookMark.setFileId(book.getFileId());
			bookMark.setCreateDate(updateDate);
			bookMark.setUpdateDate(updateDate);
			bookMark.setNumberOfPages(book.getNumberOfPages());
			bookMark.setPage(bookPage);
			
			entityManager.persist(bookMark);
			
			List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
			
			for(Book referencedBook: referencedBookList) {
				BookMarkReference bookMarkReference = new BookMarkReference();
				bookMarkReference.setBook(referencedBook);
				bookMarkReference.setBookMark(bookMark);
				bookMarkReference.setCreateDate(updateDate);
				bookMarkReference.setUpdateDate(updateDate);
				
				entityManager.persist(bookMarkReference);
				
				if(hasBookCollection(referencedBookCollectionList, referencedBook.getBookCollection().getId()) == false ) {
					referencedBookCollectionList.add(referencedBook.getBookCollection());
				}
			}
		} else {
			bookMark.setUpdateDate(updateDate);
			bookMark.setPage(bookPage);
			
			bookMark = entityManager.merge(bookMark);
			
			List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
			
			for(Book referencedBook: referencedBookList) {
				if(hasBookCollection(referencedBookCollectionList, referencedBook.getBookCollection().getId()) == false ) {
					referencedBookCollectionList.add(referencedBook.getBookCollection());
				}
			}
		}
		
		for(BookCollection referencedBookCollection: referencedBookCollectionList) {
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
		}
		
		BookMarkReference bookMarkReference = getBookMarkReferenceByUserAndBook(user, book, graph);
		
		return bookMarkReference;
	}
	
	@Transactional
	public void deleteBookMarkByUserAndBook(User user, Book book) throws ProblemException {
		List<BookCollection> referencedBookCollectionList = new ArrayList<BookCollection>();
		
		BookMark bookMark = getBookMarkByUserAndFile(user, book.getFileId());
		
		if(bookMark != null) {
			entityManager.remove(bookMark);
			
			List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
			
			for(Book referencedBook: referencedBookList) {
				if(hasBookCollection(referencedBookCollectionList, referencedBook.getBookCollection().getId()) == false ) {
					referencedBookCollectionList.add(referencedBook.getBookCollection());
				}
			}
		}
		
		for(BookCollection referencedBookCollection: referencedBookCollectionList) {
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
		}
	}
	
	public void loadBookMarkGraph(User user, Book book, Graph graph) throws ProblemException {
		List<Book> bookList = new ArrayList<Book>();
		bookList.add(book);
		
		loadBookMarkGraph(user, bookList, graph);
	}
	
	@SuppressWarnings("unchecked")
	public void loadBookMarkGraph(User user, List<Book> bookList, Graph graph) throws ProblemException {
		EntityGraph<BookMarkReference> entityGraph = entityManager.createEntityGraph(BookMarkReference.class);
		entityGraph.addSubgraph("bookMark", BookMark.class);
		
		List<Long> bookIdList = new ArrayList<Long>();
		for(Book book: bookList) {
			if(book != null) {
				bookIdList.add(book.getId());
			}
		}
		
		Query bookMarkReferenceListQuery = entityManager.createQuery("select bmr, bmr.book.id from BookMarkReference bmr where bmr.book.rootBookCollection.id = :rootBookCollectionId and bmr.bookMark.user.id = :userId and bmr.book.id in :bookIdList");
		bookMarkReferenceListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookMarkReferenceListQuery.setParameter("userId", user.getId());
		bookMarkReferenceListQuery.setParameter("bookIdList", bookIdList);
		bookMarkReferenceListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		
		List<Object[]> bookMarkReferenceObjectList = (List<Object[]>) bookMarkReferenceListQuery.getResultList();
		
		for(Book book: bookList) {
			if(book != null) {
				List<BookMarkReference> bookMarkReferenceList = new ArrayList<BookMarkReference>();
				
				for(Object[] bookMarkReferenceObject: bookMarkReferenceObjectList) {
					BookMarkReference bookMarkReference = (BookMarkReference) bookMarkReferenceObject[0];
					Long bookId = (Long) bookMarkReferenceObject[1];
					
					if(book.getId().equals(bookId)) {
						bookMarkReferenceList.add(bookMarkReference);
						
						break;
					}
				}
				
				book.setBookMarkReferences(bookMarkReferenceList);
			}
		}
	}
}
