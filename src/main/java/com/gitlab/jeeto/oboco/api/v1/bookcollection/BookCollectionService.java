package com.gitlab.jeeto.oboco.api.v1.bookcollection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.gitlab.jeeto.oboco.api.v1.user.User;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.NameHelper;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookCollectionService {
	@Inject
	EntityManager entityManager;
	
	public BookCollectionService() {
		super();
	}
	
	@Transactional
	public BookCollection createBookCollection(BookCollection bookCollection) throws ProblemException {
		entityManager.persist(bookCollection);
		
        return bookCollection;
	}
	
	@Transactional
	public BookCollection updateBookCollection(BookCollection bookCollection) throws ProblemException {
		bookCollection = entityManager.merge(bookCollection);
		
        return bookCollection;
	}
	
	public BookCollection getRootBookCollection(Long id, Graph graph) throws ProblemException {
		BookCollection rootBookCollection = null;
		
		try {
			EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
			
			if(graph != null) {
				if(graph.containsKey("parentBookCollection")) {
					entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
				}
			}
			
			rootBookCollection = entityManager.createQuery("select bc from BookCollection bc where bc.parentBookCollection.id is null and bc.id = :id", BookCollection.class)
					.setParameter("id", id)
					.setHint("javax.persistence.loadgraph", entityGraph)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return rootBookCollection;
	}
	
	public List<BookCollection> getRootBookCollections(Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		List<BookCollection> rootBookCollectionList = entityManager.createQuery("select bc from BookCollection bc where bc.parentBookCollection.id is null order by bc.number asc", BookCollection.class)
				.setHint("javax.persistence.loadgraph", entityGraph)
				.getResultList();
		
        return rootBookCollectionList;
	}
	
	public BookCollection getBookCollectionByUser(User user, Long id, Graph graph) throws ProblemException {
		BookCollection bookCollection = null;
		
		try {
			EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
			
			if(graph != null) {
				if(graph.containsKey("parentBookCollection")) {
					entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
				}
			}
			
			bookCollection = entityManager.createQuery("select bc from BookCollection bc where (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId) and bc.id = :id", BookCollection.class)
					.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
					.setParameter("id", id)
					.setHint("javax.persistence.loadgraph", entityGraph)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookCollection;
	}
	
	public BookCollection getRootBookCollection(String name) throws ProblemException {
		BookCollection bookCollection = null;
		
		try {
			bookCollection = entityManager.createQuery("select bc from BookCollection bc where bc.parentBookCollection.id is null and bc.name = :name", BookCollection.class)
					.setParameter("name", name)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookCollection;
	}
	
	public BookCollection getBookCollectionByDirectory(String directoryPath, Date updateDate) throws ProblemException {
		BookCollection bookCollection = null;
		
		try {
			bookCollection = entityManager.createQuery("select bc from BookCollection bc where bc.updateDate = :updateDate and bc.directoryPath = :directoryPath", BookCollection.class)
					.setParameter("updateDate", updateDate)
					.setParameter("directoryPath", directoryPath)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookCollection;
	}
	
	public BookCollection getBookCollectionByRootBookCollectionAndDirectory(Long rootBookCollectionId, String directoryPath) throws ProblemException {
		BookCollection bookCollection = null;
		
		try {
			bookCollection = entityManager.createQuery("select bc from BookCollection bc where (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId) and bc.directoryPath = :directoryPath", BookCollection.class)
					.setParameter("rootBookCollectionId", rootBookCollectionId)
					.setParameter("directoryPath", directoryPath)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookCollection;
	}
	
	public PageableList<BookCollection> getBookCollectionsByUserAndParentBookCollection(User user, Long parentBookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
        return getBookCollectionsByUserAndParentBookCollection(user, parentBookCollectionId, null, null, page, pageSize, graph);
	}
	
	public PageableList<BookCollection> getBookCollectionsByUserAndParentBookCollection(User user, Long parentBookCollectionId, BookCollectionSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		if(parentBookCollectionId == null) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.parentBookCollection.id is null";
		} else {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.parentBookCollection.id = :parentBookCollectionId";
		}
		
		String normalizedName = null;
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			normalizedName = NameHelper.getNormalizedName(search);
			
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
			}
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(parentBookCollectionId != null) {
			bookCollectionListSizeQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(parentBookCollectionId != null) {
			bookCollectionListQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
		
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getBookCollectionsByUser(User user, Integer page, Integer pageSize, Graph graph) throws ProblemException {
        return getBookCollectionsByUser(user, null, null, page, pageSize, graph);
	}
	
	public PageableList<BookCollection> getBookCollectionsByUser(User user, BookCollectionSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		String normalizedName = null;
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			normalizedName = NameHelper.getNormalizedName(search);
			
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
			}
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getNewBookCollectionsByUser(User user, BookCollectionSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		PageableList<BookCollection> bookCollectionPageableList;
		
		try {
			Query createDateQuery = entityManager.createQuery("select max(b.createDate) from Book b");
			
			Date createDate = (Date) createDateQuery.getSingleResult();
			
			String bookCollectionListQueryString = " where 1 = 1";
			
			bookCollectionListQueryString = bookCollectionListQueryString + " and b.rootBookCollection.id = :rootBookCollectionId and b.createDate = :createDate";
			
			String normalizedName = null;
			if(BookCollectionSearchType.NAME.equals(searchType)) {
				normalizedName = NameHelper.getNormalizedName(search);
				
				if(normalizedName != null && "".equals(normalizedName) == false) {
					bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
				}
			}
			
			Query bookCollectionListSizeQuery = entityManager.createQuery("select count(distinct bc.id) from BookCollection bc join bc.books b" + bookCollectionListQueryString);
			bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
			bookCollectionListSizeQuery.setParameter("createDate", createDate);
			
			if(BookCollectionSearchType.NAME.equals(searchType)) {
				if(normalizedName != null && "".equals(normalizedName) == false) {
					bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
				}
			}
			
			Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
			
			TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select distinct bc from BookCollection bc join bc.books b" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
			bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
			bookCollectionListQuery.setParameter("createDate", createDate);
			
			if(BookCollectionSearchType.NAME.equals(searchType)) {
				if(normalizedName != null && "".equals(normalizedName) == false) {
					bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
				}
			}
			
			bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
			bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
			bookCollectionListQuery.setMaxResults(pageSize);
			
			List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
	        
	        bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
		} catch(NoResultException e) {
			Long bookCollectionListSize = 0L;
			
			List<BookCollection> bookCollectionList = new ArrayList<BookCollection>();
			
			bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
		}
        
        return bookCollectionPageableList;
	}
	
	@SuppressWarnings("unchecked")
	public PageableList<BookCollection> getLatestReadBookCollectionsByUser(User user, BookCollectionSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and bc.rootBookCollection.id = :rootBookCollectionId and bcm.user.id = :userId";
		
		String normalizedName = null;
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			normalizedName = NameHelper.getNormalizedName(search);
			
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
			}
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(distinct bc.id) from BookCollection bc join bc.bookCollectionMarks bcm" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListSizeQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		Query bookCollectionListQuery = entityManager.createQuery("select distinct bc, bcm.updateDate from BookCollection bc join bc.bookCollectionMarks bcm" + bookCollectionListQueryString + " order by bcm.updateDate desc, bc.number asc");
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<Object[]> bookCollectionObjectList = (List<Object[]>) bookCollectionListQuery.getResultList();
		
		List<BookCollection> bookCollectionList = new ArrayList<BookCollection>();
		
		for(Object[] bookCollectionObject: bookCollectionObjectList) {
			BookCollection bookCollection = (BookCollection) bookCollectionObject[0];
			
			bookCollectionList.add(bookCollection);
		}

		
		PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
		
		return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getReadBookCollectionsByUser(User user, BookCollectionSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and bc.rootBookCollection.id = :rootBookCollectionId and bcm.user.id = :userId and bcm.page = bcm.numberOfPages";
		
		String normalizedName = null;
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			normalizedName = NameHelper.getNormalizedName(search);
			
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
			}
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(distinct bc.id) from BookCollection bc join bc.bookCollectionMarks bcm" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListSizeQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select distinct bc from BookCollection bc join bc.bookCollectionMarks bcm" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getReadingBookCollectionsByUser(User user, BookCollectionSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and bc.rootBookCollection.id = :rootBookCollectionId and bcm.user.id = :userId and bcm.page <> bcm.numberOfPages";
		
		String normalizedName = null;
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			normalizedName = NameHelper.getNormalizedName(search);
			
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
			}
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(distinct bc.id) from BookCollection bc join bc.bookCollectionMarks bcm" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListSizeQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select distinct bc from BookCollection bc join bc.bookCollectionMarks bcm" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getUnreadBookCollectionsByUser(User user, BookCollectionSearchType searchType, String search, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and bc.rootBookCollection.id = :rootBookCollectionId and bc.numberOfBooks > 0 and bc.id not in (select distinct bc.id from BookCollection bc join bc.bookCollectionMarks bcm where bc.rootBookCollection.id = :rootBookCollectionId and bcm.user.id = :userId)";
		
		String normalizedName = null;
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			normalizedName = NameHelper.getNormalizedName(search);
			
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
			}
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListSizeQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListQuery.setParameter("userId", user.getId());
		
		if(BookCollectionSearchType.NAME.equals(searchType)) {
			if(normalizedName != null && "".equals(normalizedName) == false) {
				bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
			}
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	@Transactional
	public void deleteBookCollections() throws ProblemException {
		entityManager.createQuery("delete from BookCollection")
			.executeUpdate();
	}
	
	@Transactional
	public void deleteBookCollections(Date updateDate) throws ProblemException {
		entityManager.createQuery("delete from BookCollection bc where bc.updateDate != :updateDate")
			.setParameter("updateDate", updateDate)
			.executeUpdate();
	}
}
