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
	
	public BookCollection getRootBookCollectionById(Long id, Graph graph) throws ProblemException {
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
	
	public BookCollection getBookCollectionByUserAndId(User user, Long id, Graph graph) throws ProblemException {
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
	
	public BookCollection getRootBookCollectionByName(String name) throws ProblemException {
		BookCollection bookCollection = null;
		
		try {
			bookCollection = entityManager.createQuery("select bc from BookCollection bc where bc.parentBookCollection.id is null and bc.name = :name", BookCollection.class)
					.setParameter("name", name)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookCollection;
	}
	
	public BookCollection getBookCollectionByUpdateDateAndDirectoryPath(Date updateDate, String directoryPath) throws ProblemException {
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
	
	public BookCollection getBookCollectionByBookCollectionIdAndDirectoryPath(Long rootBookCollectionId, String directoryPath) throws ProblemException {
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
	
	public PageableList<BookCollection> getBookCollectionsByUser(User user, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		Long bookCollectionListSize = (Long) entityManager.createQuery("select count(bc.id) from BookCollection bc where (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)")
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.getSingleResult();
	
		List<BookCollection> bookCollectionList = entityManager.createQuery("select bc from BookCollection bc where (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId) order by bc.number asc", BookCollection.class)
				.setParameter("rootBookCollectionId", user.getRootBookCollection().getId())
				.setHint("javax.persistence.loadgraph", entityGraph)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getBookCollectionsByUserAndName(User user, String name, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String normalizedName = NameHelper.getNormalizedName(name);
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getBookCollectionsByUser(User user, Long parentBookCollectionId, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String bookCollectionListQueryString = " where 1 = 1 ";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		if(parentBookCollectionId == null) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.parentBookCollection.id is null";
		} else {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.parentBookCollection.id = :parentBookCollectionId";
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(parentBookCollectionId != null) {
			bookCollectionListSizeQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(parentBookCollectionId != null) {
			bookCollectionListQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getBookCollectionsByUserAndName(User user, Long parentBookCollectionId, String name, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String normalizedName = NameHelper.getNormalizedName(name);
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		if(parentBookCollectionId != null) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.id in (select cbc.id from BookCollection pbc join pbc.childBookCollections cbc where pbc.id = :parentBookCollectionId)";
		}
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(parentBookCollectionId != null) {
			bookCollectionListSizeQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		
		if(parentBookCollectionId != null) {
			bookCollectionListQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		bookCollectionListQuery.setHint("javax.persistence.loadgraph", entityGraph);
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
		
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	@Transactional
	public void deleteBookCollection() throws ProblemException {
		entityManager.createQuery("delete from BookCollection")
			.executeUpdate();
	}
	
	@Transactional
	public void deleteBookCollectionByUpdateDate(Date updateDate) throws ProblemException {
		entityManager.createQuery("delete from BookCollection bc where bc.updateDate != :updateDate")
			.setParameter("updateDate", updateDate)
			.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	public PageableList<BookCollection> getLatestBookCollectionsByUserAndName(User user, String name, Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<BookCollection> entityGraph = entityManager.createEntityGraph(BookCollection.class);
		
		if(graph != null) {
			if(graph.containsKey("parentBookCollection")) {
				entityGraph.addSubgraph("parentBookCollection", BookCollection.class);
			}
		}
		
		String normalizedName = NameHelper.getNormalizedName(name);
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and bmr.rootBookCollection.id = :rootBookCollectionId and bmr.user.id = :userId";
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bmr.bookCollection.normalizedName like :normalizedName";
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(distinct bmr.bookCollection.id) from BookMarkReference bmr" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListSizeQuery.setParameter("userId", user.getId());
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		Query bookCollectionListQuery = entityManager.createQuery("select bc, max(bmr.bookMark.updateDate) from BookMarkReference bmr inner join bmr.bookCollection bc" + bookCollectionListQueryString + " group by bc order by max(bmr.bookMark.updateDate) desc");
		bookCollectionListQuery.setParameter("rootBookCollectionId", user.getRootBookCollection().getId());
		bookCollectionListQuery.setParameter("userId", user.getId());
		
		if(normalizedName != null && "".equals(normalizedName) == false) {
			bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
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
}
