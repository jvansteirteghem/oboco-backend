package com.gitlab.jeeto.oboco.api.v1.book;

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
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMark;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkService;
import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.Linkable;
import com.gitlab.jeeto.oboco.common.NameHelper;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookService {
	@Inject
	EntityManager entityManager;
	private BookMarkService bookMarkService;
	@Inject
	Provider<BookMarkService> bookMarkServiceProvider;
	
	private BookMarkService getBookMarkService() {
		if(bookMarkService == null) {
			bookMarkService = bookMarkServiceProvider.get();
		}
		return bookMarkService;
	}
	
	public BookService() {
		super();
	}
	
	@Transactional
	public Book createBook(Book book) throws ProblemException {
		entityManager.persist(book);
		
        return book;
	}
	
	@Transactional
	public Book updateBook(Book book) throws ProblemException {
		book = entityManager.merge(book);
		
        return book;
	}
	
	public Book getBookByUser(User user, Long id) throws ProblemException {
		return getBookByUser(user, id, null);
	}
	
	public Book getBookByUser(User user, Long id, Graph graph) throws ProblemException {
		Book book = null;
		
		try {
			EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
			if(graph != null) {
				if(graph.containsKey("bookCollection")) {
					entityGraph.addSubgraph("bookCollection", BookCollection.class);
				}
			}
			
			book = entityManager.createQuery("select b from Book b where b.rootBookCollection.id = :rootBookCollectionId and b.id = :id", Book.class)
					.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
					.setParameter("id", id)
					.setHint("javax.persistence.loadgraph", entityGraph)
					.getSingleResult();
			
			if(graph != null) {
				if(graph.containsKey("bookMark")) {
					Graph bookMarkGraph = graph.get("bookMark");
					
					getBookMarkService().loadBookMarkGraph(user, book, bookMarkGraph);
				}
			}
		} catch(NoResultException e) {
			
		}
		
        return book;
	}
	
	public Book getBookByFile(String filePath, Date updateDate) throws ProblemException {
		Book book = null;
		
		try {
			book = entityManager.createQuery("select b from Book b where b.updateDate = :updateDate and b.filePath = :filePath", Book.class)
					.setParameter("updateDate", updateDate)
					.setParameter("filePath", filePath)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return book;
	}
	
	public Book getBookByRootBookCollectionAndFile(Long rootBookCollectionId, String filePath) throws ProblemException {
		Book book = null;
		
		try {
			book = entityManager.createQuery("select b from Book b where b.rootBookCollection.id = :rootBookCollectionId and b.filePath = :filePath", Book.class)
					.setParameter("rootBookCollectionId", rootBookCollectionId)
					.setParameter("filePath", filePath)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return book;
	}
	
	public List<Book> getBooksByFile(String fileId) throws ProblemException {
		List<Book> bookList = entityManager.createQuery("select b from Book b where b.fileId = :fileId", Book.class)
				.setParameter("fileId", fileId)
				.getResultList();
        
        return bookList;
	}
	
	public PageableList<Book> getBooksByUser(User user, Integer page, Integer pageSize, Graph graph) throws ProblemException {
        return getBooksByUser(user, null, null, page, pageSize, graph);
	}
	
	public PageableList<Book> getBooksByUser(User user, BookSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		String bookListQueryString = " where 1 = 1";
		
		bookListQueryString = bookListQueryString + " and b.rootBookCollection.id = :rootBookCollectionId";
		
		String normalizedName = null;
		if(BookSearchType.NAME.equals(searchType)) {
			normalizedName = NameHelper.getNormalizedName(search);
			
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookListQueryString = bookListQueryString + " and b.normalizedName like :normalizedName";
			}
		}
		
		Query bookListSizeQuery = entityManager.createQuery("select count(b.id) from Book b" + bookListQueryString);
		bookListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(BookSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		Long bookListSize = (Long) bookListSizeQuery.getSingleResult();
		
		TypedQuery<Book> bookListQuery = entityManager.createQuery("select b from Book b" + bookListQueryString + " order by b.number asc", Book.class);
		bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(BookSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookListQuery.setFirstResult((page - 1) * pageSize);
		bookListQuery.setMaxResults(pageSize);
		
		List<Book> bookList = bookListQuery.getResultList();
		
		if(graph != null) {
			if(graph.containsKey("bookMark")) {
				Graph bookMarkGraph = graph.get("bookMark");
				
				getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
			}
		}
        
        PageableList<Book> bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
        
        return bookPageableList;
	}
	
	public Linkable<Book> getLinkableBookByUserAndBookCollection(User user, Long bookCollectionId, Long id, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		TypedQuery<Book> bookListQuery = entityManager.createQuery("select b from Book b where b.rootBookCollection.id = :rootBookCollectionId and b.bookCollection.id = :bookCollectionId order by b.number asc", Book.class);
		bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		
		List<Book> bookList = bookListQuery.getResultList();
		
		Linkable<Book> bookLinkable = new Linkable<Book>();
		
		Integer index = 0;
		while(index < bookList.size()) {
			Book book = bookList.get(index);
			
			if(book.getId().equals(id)) {
				if(index - 1 >= 0) {
					Book previousBook = bookList.get(index - 1);
					
					bookLinkable.setPreviousElement(previousBook);
				}
				
				bookLinkable.setElement(book);
				
				if(index + 1 < bookList.size()) {
					Book nextBook = bookList.get(index + 1);
					
					bookLinkable.setNextElement(nextBook);
				}
				break;
			}
			
			index = index + 1;
		}
		
		bookList = new ArrayList<Book>();
		bookList.add(bookLinkable.getPreviousElement());
		bookList.add(bookLinkable.getElement());
		bookList.add(bookLinkable.getNextElement());
		
		if(graph != null) {
			if(graph.containsKey("bookMark")) {
				Graph bookMarkGraph = graph.get("bookMark");
				
				getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
			}
		}
		
		return bookLinkable;
	}
	
	public List<Book> getBooksByUserAndBookCollection(User user, Long bookCollectionId) throws ProblemException {
		String bookListQueryString = " where 1 = 1";
		
		bookListQueryString = bookListQueryString + " and b.rootBookCollection.id = :rootBookCollectionId";
		
		bookListQueryString = bookListQueryString + " and b.bookCollection.id = :bookCollectionId";
		
		TypedQuery<Book> bookListQuery = entityManager.createQuery("select b from Book b" + bookListQueryString + " order by b.number asc", Book.class);
		bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListQuery.setParameter("bookCollectionId", bookCollectionId);
		
		List<Book> bookList = bookListQuery.getResultList();
        
        return bookList;
	}
	
	public PageableList<Book> getBooksByUserAndBookCollection(User user, Long bookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		String bookListQueryString = " where 1 = 1";
		
		bookListQueryString = bookListQueryString + " and b.rootBookCollection.id = :rootBookCollectionId";
		
		bookListQueryString = bookListQueryString + " and b.bookCollection.id = :bookCollectionId";
		
		Query bookListSizeQuery = entityManager.createQuery("select count(b.id) from Book b" + bookListQueryString);
		bookListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListSizeQuery.setParameter("bookCollectionId", bookCollectionId);
		
		Long bookListSize = (Long) bookListSizeQuery.getSingleResult();
		
		TypedQuery<Book> bookListQuery = entityManager.createQuery("select b from Book b" + bookListQueryString + " order by b.number asc", Book.class);
		bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookListQuery.setFirstResult((page - 1) * pageSize);
		bookListQuery.setMaxResults(pageSize);
		
		List<Book> bookList = bookListQuery.getResultList();
		
		if(graph != null) {
			if(graph.containsKey("bookMark")) {
				Graph bookMarkGraph = graph.get("bookMark");
				
				getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
			}
		}
        
        PageableList<Book> bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
        
        return bookPageableList;
	}
	
	public PageableList<Book> getNewBooksByUserAndBookCollection(User user, Long bookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		PageableList<Book> bookPageableList;
		
		try {
			Query createDateQuery = entityManager.createQuery("select max(b.createDate) from Book b");
			
			Date createDate = (Date) createDateQuery.getSingleResult();
			
			String bookListQueryString = " where 1 = 1";
			
			bookListQueryString = bookListQueryString + " and b.rootBookCollection.id = :rootBookCollectionId";
			
			bookListQueryString = bookListQueryString + " and b.bookCollection.id = :bookCollectionId";
			
			bookListQueryString = bookListQueryString + " and b.createDate = :createDate";
			
			Query bookListSizeQuery = entityManager.createQuery("select count(b.id) from Book b" + bookListQueryString);
			bookListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
			bookListSizeQuery.setParameter("bookCollectionId", bookCollectionId);
			bookListSizeQuery.setParameter("createDate", createDate);
			
			Long bookListSize = (Long) bookListSizeQuery.getSingleResult();
			
			TypedQuery<Book> bookListQuery = entityManager.createQuery("select b from Book b" + bookListQueryString + " order by b.number asc", Book.class);
			bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
			bookListQuery.setParameter("bookCollectionId", bookCollectionId);
			bookListQuery.setParameter("createDate", createDate);
			bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
			bookListQuery.setFirstResult((page - 1) * pageSize);
			bookListQuery.setMaxResults(pageSize);
			
			List<Book> bookList = bookListQuery.getResultList();
			
			if(graph != null) {
				if(graph.containsKey("bookMark")) {
					Graph bookMarkGraph = graph.get("bookMark");
					
					getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
				}
			}
	        
	        bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
		} catch(NoResultException e) {
			Long bookListSize = 0L;
			
			List<Book> bookList = new ArrayList<Book>();
			
			bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
		}
		
        return bookPageableList;
	}
	
	public PageableList<Book> getLatestReadBooksByUserAndBookCollection(User user, Long bookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		PageableList<Book> bookPageableList;
		
		try {
			String updateDateQueryString = " where 1 = 1";
			
			updateDateQueryString = updateDateQueryString + " and bmr.rootBookCollection.id = :rootBookCollectionId";
			
			updateDateQueryString = updateDateQueryString + " and bmr.bookCollection.id = :bookCollectionId";
			
			updateDateQueryString = updateDateQueryString + " and bmr.user.id = :userId";
			
			Query updateDateQuery = entityManager.createQuery("select max(bmr.bookMark.updateDate) from BookMarkReference bmr" + updateDateQueryString);
			updateDateQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
			updateDateQuery.setParameter("bookCollectionId", bookCollectionId);
			updateDateQuery.setParameter("userId", user.getId());
			
			Date updateDate = (Date) updateDateQuery.getSingleResult();
			
			String bookListQueryString = " where 1 = 1";
			
			bookListQueryString = bookListQueryString + " and bmr.rootBookCollection.id = :rootBookCollectionId";
			
			bookListQueryString = bookListQueryString + " and bmr.bookCollection.id = :bookCollectionId";
			
			bookListQueryString = bookListQueryString + " and bmr.user.id = :userId";
			
			bookListQueryString = bookListQueryString + " and bmr.bookMark.updateDate = :updateDate";
			
			Query bookListSizeQuery = entityManager.createQuery("select count(bmr.book.id) from BookMarkReference bmr" + bookListQueryString);
			bookListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
			bookListSizeQuery.setParameter("bookCollectionId", bookCollectionId);
			bookListSizeQuery.setParameter("userId", user.getId());
			bookListSizeQuery.setParameter("updateDate", updateDate);
			
			Long bookListSize = (Long) bookListSizeQuery.getSingleResult();
			
			TypedQuery<Book> bookListQuery = entityManager.createQuery("select bmr.book from BookMarkReference bmr" + bookListQueryString + " order by bmr.book.number asc", Book.class);
			bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
			bookListQuery.setParameter("bookCollectionId", bookCollectionId);
			bookListQuery.setParameter("userId", user.getId());
			bookListQuery.setParameter("updateDate", updateDate);
			bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
			bookListQuery.setFirstResult((page - 1) * pageSize);
			bookListQuery.setMaxResults(pageSize);
			
			List<Book> bookList = bookListQuery.getResultList();
			
			if(graph != null) {
				if(graph.containsKey("bookMark")) {
					Graph bookMarkGraph = graph.get("bookMark");
					
					getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
				}
			}
	        
	        bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
		} catch(NoResultException e) {
			Long bookListSize = 0L;
			
			List<Book> bookList = new ArrayList<Book>();
			
			bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
		}
		
        return bookPageableList;
	}
	
	public PageableList<Book> getReadBooksByUserAndBookCollection(User user, Long bookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		String bookListQueryString = " where 1 = 1";
		
		bookListQueryString = bookListQueryString + " and bmr.rootBookCollection.id = :rootBookCollectionId";
		
		bookListQueryString = bookListQueryString + " and bmr.bookCollection.id = :bookCollectionId";
		
		bookListQueryString = bookListQueryString + " and bmr.user.id = :userId";
		
		bookListQueryString = bookListQueryString + " and bmr.book.numberOfPages = bmr.bookMark.page";
		
		Query bookListSizeQuery = entityManager.createQuery("select count(bmr.book.id) from BookMarkReference bmr" + bookListQueryString);
		bookListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListSizeQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListSizeQuery.setParameter("userId", user.getId());
		
		Long bookListSize = (Long) bookListSizeQuery.getSingleResult();
		
		TypedQuery<Book> bookListQuery = entityManager.createQuery("select bmr.book from BookMarkReference bmr" + bookListQueryString + " order by bmr.book.number asc", Book.class);
		bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListQuery.setParameter("userId", user.getId());
		bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookListQuery.setFirstResult((page - 1) * pageSize);
		bookListQuery.setMaxResults(pageSize);
		
		List<Book> bookList = bookListQuery.getResultList();
		
		if(graph != null) {
			if(graph.containsKey("bookMark")) {
				Graph bookMarkGraph = graph.get("bookMark");
				
				getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
			}
		}
        
        PageableList<Book> bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
        
        return bookPageableList;
	}
	
	public PageableList<Book> getReadingBooksByUserAndBookCollection(User user, Long bookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		String bookListQueryString = " where 1 = 1";
		
		bookListQueryString = bookListQueryString + " and bmr.rootBookCollection.id = :rootBookCollectionId";
		
		bookListQueryString = bookListQueryString + " and bmr.bookCollection.id = :bookCollectionId";
		
		bookListQueryString = bookListQueryString + " and bmr.user.id = :userId";
		
		bookListQueryString = bookListQueryString + " and bmr.book.numberOfPages <> bmr.bookMark.page";
		
		Query bookListSizeQuery = entityManager.createQuery("select count(bmr.book.id) from BookMarkReference bmr" + bookListQueryString);
		bookListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListSizeQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListSizeQuery.setParameter("userId", user.getId());
		
		Long bookListSize = (Long) bookListSizeQuery.getSingleResult();
		
		TypedQuery<Book> bookListQuery = entityManager.createQuery("select bmr.book from BookMarkReference bmr" + bookListQueryString + " order by bmr.book.number asc", Book.class);
		bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListQuery.setParameter("userId", user.getId());
		bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookListQuery.setFirstResult((page - 1) * pageSize);
		bookListQuery.setMaxResults(pageSize);
		
		List<Book> bookList = bookListQuery.getResultList();
		
		if(graph != null) {
			if(graph.containsKey("bookMark")) {
				Graph bookMarkGraph = graph.get("bookMark");
				
				getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
			}
		}
        
        PageableList<Book> bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
        
        return bookPageableList;
	}
	
	public PageableList<Book> getUnreadBooksByUserAndBookCollection(User user, Long bookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		String bookListQueryString = " where 1 = 1";
		
		bookListQueryString = bookListQueryString + " and b.rootBookCollection.id = :rootBookCollectionId";
		
		bookListQueryString = bookListQueryString + " and b.bookCollection.id = :bookCollectionId";
		
		bookListQueryString = bookListQueryString + " and b.id not in (select bmr.book.id from BookMarkReference bmr where bmr.rootBookCollection.id = :rootBookCollectionId and bmr.bookCollection.id = :bookCollectionId and bmr.user.id = :userId)";
		
		Query bookListSizeQuery = entityManager.createQuery("select count(b.id) from Book b" + bookListQueryString);
		bookListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListSizeQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListSizeQuery.setParameter("userId", user.getId());
		
		Long bookListSize = (Long) bookListSizeQuery.getSingleResult();
		
		TypedQuery<Book> bookListQuery = entityManager.createQuery("select b from Book b" + bookListQueryString + " order by b.number asc", Book.class);
		bookListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookListQuery.setParameter("bookCollectionId", bookCollectionId);
		bookListQuery.setParameter("userId", user.getId());
		bookListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookListQuery.setFirstResult((page - 1) * pageSize);
		bookListQuery.setMaxResults(pageSize);
		
		List<Book> bookList = bookListQuery.getResultList();
		
		if(graph != null) {
			if(graph.containsKey("bookMark")) {
				Graph bookMarkGraph = graph.get("bookMark");
				
				getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
			}
		}
        
        PageableList<Book> bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
        
        return bookPageableList;
	}
	
	@Transactional
	public void deleteBooks() throws ProblemException {
		entityManager.createQuery("delete from Book")
			.executeUpdate();
	}
	
	@Transactional
	public void deleteBooks(Date updateDate) throws ProblemException {
		entityManager.createQuery("delete from Book b where b.updateDate != :updateDate")
			.setParameter("updateDate", updateDate)
			.executeUpdate();
	}
	
	public PageableList<Book> getBooksByUserAndBookMark(User user, BookMark bookMark, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<Book> entityGraph = entityManager.createEntityGraph(Book.class);
		if(graph != null) {
			if(graph.containsKey("bookCollection")) {
				entityGraph.addSubgraph("bookCollection", BookCollection.class);
			}
		}
		
		String bookListQueryString = " where 1 = 1";
		
		bookListQueryString = " and bmr.rootBookCollection.id = :rootBookCollectionId and bmr.user.id = :userId and bmr.bookMark.id = :bookMarkId";
		
		Long bookListSize = (Long) entityManager.createQuery("select count(bmr.book.id) from BookMarkReference bmr" + bookListQueryString)
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.setParameter("userId", user.getId())
				.setParameter("bookMarkId", bookMark.getId())
				.getSingleResult();
		
		List<Book> bookList = entityManager.createQuery("select bmr.book from BookMarkReference bmr" + bookListQueryString + " order by bmr.book.number asc", Book.class)
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.setParameter("userId", user.getId())
				.setParameter("bookMarkId", bookMark.getId())
				.setHint("javax.persistence.loadgraph", entityGraph)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
        
		if(graph != null) {
			if(graph.containsKey("bookMark")) {
				Graph bookMarkGraph = graph.get("bookMark");
				
				getBookMarkService().loadBookMarkGraph(user, bookList, bookMarkGraph);
			}
		}
		
        PageableList<Book> bookPageableList = new PageableList<Book>(bookList, bookListSize, page, pageSize);
        
        return bookPageableList;
	}
}
