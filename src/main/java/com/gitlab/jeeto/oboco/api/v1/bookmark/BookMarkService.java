package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.gitlab.jeeto.oboco.common.GraphHelper;
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
			bookMark = entityManager.createQuery("select bm from BookMark bm join bm.bookMarkReferences bmr where bmr.book.rootBookCollection.id = :rootBookCollectionId and bm.user.id = :userId order by bm.updateDate desc", BookMark.class)
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
	public void deleteBookMarksByUser(User user) throws ProblemException {
		entityManager.createQuery("delete from BookMark bm where bm.user.id = :userId")
			.setParameter("userId", user.getId())
			.executeUpdate();
		
		entityManager.createQuery("delete from BookCollectionMark bcm where bcm.user.id = :userId")
			.setParameter("userId", user.getId())
			.executeUpdate();
	}
	
	public BookCollectionMark getBookCollectionMarkByUserAndBookCollection(User user, BookCollection bookCollection, Graph graph) throws ProblemException {
		EntityGraph<BookCollectionMark> entityGraph = entityManager.createEntityGraph(BookCollectionMark.class);
		
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				Subgraph<BookCollection> bookCollectionEntityGraph = entityGraph.addSubgraph("bookCollection", BookCollection.class);
				
				Graph bookCollectionGraph = graph.get("bookCollection");
				if(bookCollectionGraph != null) {
					if(bookCollectionGraph.containsKey("parentBookCollection")) {
						bookCollectionEntityGraph.addSubgraph("parentBookCollection", BookCollection.class);
					}
				}
			}
		}
		
		BookCollectionMark bookCollectionMark = null;
		
		try {
			bookCollectionMark = entityManager.createQuery("select bcm from BookCollectionMark bcm where bcm.user.id = :userId and bcm.bookCollection.id = :bookCollectionId", BookCollectionMark.class)
				.setParameter("userId", user.getId())
				.setParameter("bookCollectionId", bookCollection.getId())
				.setHint("javax.persistence.loadgraph", entityGraph)
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
	public void createBookMarkReferencesByBook(Book book, Date updateDate) throws ProblemException {
		List<BookMark> bookMarkList = getBookMarksByFile(book.getFileId());
		
		for(BookMark bookMark: bookMarkList) {
			if(bookMark.getNumberOfPages() != book.getNumberOfPages()) {
				Integer page;
				
				if(bookMark.getPage() == 0) {
					page = 0;
				} else {
					page = bookMark.getPage() + book.getNumberOfPages() - bookMark.getNumberOfPages();
					
					if(page < 1) {
						page = 1;
					}
				}
				
				bookMark.setNumberOfPages(book.getNumberOfPages());
				bookMark.setPage(page);
				
				entityManager.merge(bookMark);
			}
			
			BookMarkReference bookMarkReference = new BookMarkReference();
			bookMarkReference.setBook(book);
			bookMarkReference.setBookMark(bookMark);
			
			entityManager.persist(bookMarkReference);
		}
	}
	
	@Transactional
	public void updateBookMarkReferencesByBook(Book book, Date updateDate) throws ProblemException {
		List<BookMark> bookMarkList = getBookMarksByFile(book.getFileId());
		
		for(BookMark bookMark: bookMarkList) {
			if(bookMark.getNumberOfPages() != book.getNumberOfPages()) {
				Integer page;
				
				if(bookMark.getPage() == 0) {
					page = 0;
				} else {
					page = bookMark.getPage() + book.getNumberOfPages() - bookMark.getNumberOfPages();
					
					if(page < 1) {
						page = 1;
					}
				}
				
				bookMark.setNumberOfPages(book.getNumberOfPages());
				bookMark.setPage(page);
				
				entityManager.merge(bookMark);
			}
		}
	}
	
	@Transactional
	public void createOrUpdateOrDeleteBookCollectionMarkByBookCollection(BookCollection bookCollection, Date updateDate) throws ProblemException {
		List<User> userList = entityManager.createQuery("select distinct bmr.bookMark.user from BookMarkReference bmr where bmr.book.bookCollection.id = :bookCollectionId", User.class)
				.setParameter("bookCollectionId", bookCollection.getId())
				.getResultList();
		
		for(User user: userList) {
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, bookCollection, updateDate);
		}
	}
	
	public void createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(User user, BookCollection bookCollection) throws ProblemException {
		createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, bookCollection, null);
	}
	
	public void createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(User user, BookCollection bookCollection, Date updateDate) throws ProblemException {
		Graph graph = GraphHelper.createGraph("()");
		
		BookCollectionMark bookCollectionMark = getBookCollectionMarkByUserAndBookCollection(user, bookCollection, graph);
		
		try {
			String bookCollectionMarkQueryString = " where 1 = 1";
			
			bookCollectionMarkQueryString = bookCollectionMarkQueryString + " and bmr.book.bookCollection.id = :bookCollectionId and bm.user.id = :userId";
			
			if(updateDate != null) {
				bookCollectionMarkQueryString = bookCollectionMarkQueryString + " and bmr.book.updateDate = :updateDate";
			}
			
			Query bookCollectionMarkQuery = entityManager.createQuery("select min(bm.createDate), max(bm.updateDate), sum(bm.page) from BookMark bm join bm.bookMarkReferences bmr" + bookCollectionMarkQueryString);
			bookCollectionMarkQuery.setParameter("userId", user.getId());
			bookCollectionMarkQuery.setParameter("bookCollectionId", bookCollection.getId());
			
			if(updateDate != null) {
				bookCollectionMarkQuery.setParameter("updateDate", updateDate);
			}
			
			Object[] bookCollectionMarkObject = (Object[]) bookCollectionMarkQuery.getSingleResult();
			
			if(bookCollectionMarkObject[0] != null && bookCollectionMarkObject[1] != null && bookCollectionMarkObject[2] != null) {
				if(bookCollectionMark == null) {
					bookCollectionMark = new BookCollectionMark();
					bookCollectionMark.setUser(user);
					bookCollectionMark.setBookCollection(bookCollection);
					bookCollectionMark.setCreateDate((Date) bookCollectionMarkObject[0]);
					bookCollectionMark.setUpdateDate((Date) bookCollectionMarkObject[1]);
					bookCollectionMark.setNumberOfBookPages(bookCollection.getNumberOfBookPages());
					bookCollectionMark.setBookPage(((Long) bookCollectionMarkObject[2]).intValue());
					
					entityManager.persist(bookCollectionMark);
				} else {
					bookCollectionMark.setCreateDate((Date) bookCollectionMarkObject[0]);
					bookCollectionMark.setUpdateDate((Date) bookCollectionMarkObject[1]);
					bookCollectionMark.setNumberOfBookPages(bookCollection.getNumberOfBookPages());
					bookCollectionMark.setBookPage(((Long) bookCollectionMarkObject[2]).intValue());
					
					bookCollectionMark = entityManager.merge(bookCollectionMark);
				}
			} else {
				if(bookCollectionMark != null) {
					entityManager.remove(bookCollectionMark);
				}
			}
		} catch(NoResultException e) {
			
		}
	}
	
	@Transactional
	public BookCollectionMark createOrUpdateBookMarksByUserAndBookCollection(User user, BookCollection bookCollection, Integer bookPage, Graph graph) throws ProblemException {
		Date updateDate = new Date();
		
		Map<Long, BookCollection> referencedBookCollectionMap = new HashMap<Long, BookCollection>();
		
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
				if(bookPage == -1) {
					bookMark.setPage(book.getNumberOfPages());
				} else {
					bookMark.setPage(bookPage);
				}
				
				entityManager.persist(bookMark);
				
				List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
				
				for(Book referencedBook: referencedBookList) {
					BookMarkReference bookMarkReference = new BookMarkReference();
					bookMarkReference.setBook(referencedBook);
					bookMarkReference.setBookMark(bookMark);
					
					entityManager.persist(bookMarkReference);
					
					referencedBookCollectionMap.put(referencedBook.getBookCollection().getId(), referencedBook.getBookCollection());
				}
			} else {
				bookMark.setUpdateDate(updateDate);
				bookMark.setNumberOfPages(book.getNumberOfPages());
				if(bookPage == -1) {
					bookMark.setPage(book.getNumberOfPages());
				} else {
					bookMark.setPage(bookPage);
				}
				
				bookMark = entityManager.merge(bookMark);
				
				List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
				
				for(Book referencedBook: referencedBookList) {
					referencedBookCollectionMap.put(referencedBook.getBookCollection().getId(), referencedBook.getBookCollection());
				}
			}
			
			for(Map.Entry<Long, BookCollection> entry : referencedBookCollectionMap.entrySet()) {
				BookCollection referencedBookCollection = entry.getValue();
				
				createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
			}
		}
		
		BookCollectionMark bookCollectionMark = getBookCollectionMarkByUserAndBookCollection(user, bookCollection, graph);
		
		return bookCollectionMark;
	}
	
	@Transactional
	public void deleteBookMarksByUserAndBookCollection(User user, BookCollection bookCollection) throws ProblemException {
		Map<Long, BookCollection> referencedBookCollectionMap = new HashMap<Long, BookCollection>();
		
		List<Book> bookList = getBookService().getBooksByUserAndBookCollection(user, bookCollection.getId());
		
		for(Book book: bookList) {
			BookMark bookMark = getBookMarkByUserAndFile(user, book.getFileId());
			
			if(bookMark != null) {
				entityManager.remove(bookMark);
				
				List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
				
				for(Book referencedBook: referencedBookList) {
					referencedBookCollectionMap.put(referencedBook.getBookCollection().getId(), referencedBook.getBookCollection());
				}
			}
		}
		
		for(Map.Entry<Long, BookCollection> entry : referencedBookCollectionMap.entrySet()) {
			BookCollection referencedBookCollection = entry.getValue();
			
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
		}
	}
	
	@Transactional
	public BookMarkReference createOrUpdateBookMarkByUserAndBook(User user, Book book, Integer bookPage, Graph graph) throws ProblemException {
		Date updateDate = new Date();
		
		Map<Long, BookCollection> referencedBookCollectionMap = new HashMap<Long, BookCollection>();
		
		BookMark bookMark = getBookMarkByUserAndFile(user, book.getFileId());
		
		if(bookMark == null) {
			bookMark = new BookMark();
			bookMark.setUser(user);
			bookMark.setFileId(book.getFileId());
			bookMark.setCreateDate(updateDate);
			bookMark.setUpdateDate(updateDate);
			bookMark.setNumberOfPages(book.getNumberOfPages());
			if(bookPage == -1) {
				bookMark.setPage(book.getNumberOfPages());
			} else {
				bookMark.setPage(bookPage);
			}
			
			entityManager.persist(bookMark);
			
			List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
			
			for(Book referencedBook: referencedBookList) {
				BookMarkReference bookMarkReference = new BookMarkReference();
				bookMarkReference.setBook(referencedBook);
				bookMarkReference.setBookMark(bookMark);
				
				entityManager.persist(bookMarkReference);
				
				referencedBookCollectionMap.put(referencedBook.getBookCollection().getId(), referencedBook.getBookCollection());
			}
		} else {
			bookMark.setUpdateDate(updateDate);
			bookMark.setNumberOfPages(book.getNumberOfPages());
			if(bookPage == -1) {
				bookMark.setPage(book.getNumberOfPages());
			} else {
				bookMark.setPage(bookPage);
			}
			
			bookMark = entityManager.merge(bookMark);
			
			List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
			
			for(Book referencedBook: referencedBookList) {
				referencedBookCollectionMap.put(referencedBook.getBookCollection().getId(), referencedBook.getBookCollection());
			}
		}
		
		for(Map.Entry<Long, BookCollection> entry : referencedBookCollectionMap.entrySet()) {
			BookCollection referencedBookCollection = entry.getValue();
			
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
		}
		
		BookMarkReference bookMarkReference = getBookMarkReferenceByUserAndBook(user, book, graph);
		
		return bookMarkReference;
	}
	
	@Transactional
	public void deleteBookMarkByUserAndBook(User user, Book book) throws ProblemException {
		Map<Long, BookCollection> referencedBookCollectionMap = new HashMap<Long, BookCollection>();
		
		BookMark bookMark = getBookMarkByUserAndFile(user, book.getFileId());
		
		if(bookMark != null) {
			entityManager.remove(bookMark);
			
			List<Book> referencedBookList = getBookService().getBooksByFile(book.getFileId());
			
			for(Book referencedBook: referencedBookList) {
				referencedBookCollectionMap.put(referencedBook.getBookCollection().getId(), referencedBook.getBookCollection());
			}
		}
		
		for(Map.Entry<Long, BookCollection> entry : referencedBookCollectionMap.entrySet()) {
			BookCollection referencedBookCollection = entry.getValue();
			
			createOrUpdateOrDeleteBookCollectionMarkByUserAndBookCollection(user, referencedBookCollection);
		}
	}
	
	public void loadBookCollectionMarkGraph(User user, BookCollection bookCollection, Graph graph) throws ProblemException {
		List<BookCollection> bookCollectionList = new ArrayList<BookCollection>();
		bookCollectionList.add(bookCollection);
		
		loadBookCollectionMarkGraph(user, bookCollectionList, graph);
	}
	
	@SuppressWarnings("unchecked")
	public void loadBookCollectionMarkGraph(User user, List<BookCollection> bookCollectionList, Graph graph) throws ProblemException {
		List<Long> bookCollectionIdList = new ArrayList<Long>();
		for(BookCollection bookCollection: bookCollectionList) {
			if(bookCollection != null) {
				bookCollectionIdList.add(bookCollection.getId());
			}
		}
		
		Query bookCollectionMarkListQuery = entityManager.createQuery("select bcm, bcm.bookCollection.id from BookCollectionMark bcm where bcm.bookCollection.rootBookCollection.id = :rootBookCollectionId and bcm.user.id = :userId and bcm.bookCollection.id in :bookCollectionIdList");
		bookCollectionMarkListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionMarkListQuery.setParameter("userId", user.getId());
		bookCollectionMarkListQuery.setParameter("bookCollectionIdList", bookCollectionIdList);
		
		List<Object[]> bookCollectionMarkObjectList = (List<Object[]>) bookCollectionMarkListQuery.getResultList();
		
		for(BookCollection bookCollection: bookCollectionList) {
			if(bookCollection != null) {
				List<BookCollectionMark> bookCollectionMarkList = new ArrayList<BookCollectionMark>();
				
				for(Object[] bookCollectionMarkObject: bookCollectionMarkObjectList) {
					BookCollectionMark bookCollectionMark = (BookCollectionMark) bookCollectionMarkObject[0];
					Long bookCollectionId = (Long) bookCollectionMarkObject[1];
					
					if(bookCollection.getId().equals(bookCollectionId)) {
						bookCollectionMarkList.add(bookCollectionMark);
						
						break;
					}
				}
				
				bookCollection.setBookCollectionMarks(bookCollectionMarkList);
			}
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
