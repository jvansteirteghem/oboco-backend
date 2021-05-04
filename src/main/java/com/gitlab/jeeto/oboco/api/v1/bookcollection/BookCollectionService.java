package com.gitlab.jeeto.oboco.api.v1.bookcollection;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

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
	
	public BookCollection getRootBookCollectionById(Long id) throws ProblemException {
		BookCollection rootBookCollection = null;
		
		try {
			rootBookCollection = entityManager.createQuery("select bc from BookCollection bc where bc.parentBookCollection.id is null and bc.id = :id", BookCollection.class)
					.setParameter("id", id)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return rootBookCollection;
	}
	
	public List<BookCollection> getRootBookCollections() throws ProblemException {
		List<BookCollection> rootBookCollectionList = entityManager.createQuery("select bc from BookCollection bc where bc.parentBookCollection.id is null order by bc.number asc", BookCollection.class)
				.getResultList();
		
        return rootBookCollectionList;
	}
	
	public BookCollection getBookCollectionByBookCollectionIdAndId(Long rootBookCollectionId, Long id) throws ProblemException {
		BookCollection bookCollection = null;
		
		try {
			bookCollection = entityManager.createQuery("select bc from BookCollection bc where (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId) and bc.id = :id", BookCollection.class)
					.setParameter("rootBookCollectionId", rootBookCollectionId)
					.setParameter("id", id)
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
	
	public PageableList<BookCollection> getBookCollectionsByBookCollectionId(Long rootBookCollectionId, Integer page, Integer pageSize) throws ProblemException {
		Long bookCollectionListSize = (Long) entityManager.createQuery("select count(bc.id) from BookCollection bc where (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)")
				.setParameter("rootBookCollectionId", rootBookCollectionId)
				.getSingleResult();
	
		List<BookCollection> bookCollectionList = entityManager.createQuery("select bc from BookCollection bc where (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId) order by bc.number asc", BookCollection.class)
				.setParameter("rootBookCollectionId", rootBookCollectionId)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getBookCollectionsByBookCollectionIdAndName(Long rootBookCollectionId, String name, Integer page, Integer pageSize) throws ProblemException {
		String normalizedName = NameHelper.getNormalizedName(name);
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		if("".equals(normalizedName) == false) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", rootBookCollectionId);
		
		if("".equals(normalizedName) == false) {
			bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", rootBookCollectionId);
		
		if("".equals(normalizedName) == false) {
			bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getBookCollectionsByBookCollectionId(Long rootBookCollectionId, Long parentBookCollectionId, Integer page, Integer pageSize) throws ProblemException {
		String bookCollectionListQueryString = " where 1 = 1 ";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		if(parentBookCollectionId == null) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.parentBookCollection.id is null";
		} else {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.parentBookCollection.id = :parentBookCollectionId";
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", rootBookCollectionId);
		
		if(parentBookCollectionId != null) {
			bookCollectionListSizeQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", rootBookCollectionId);
		
		if(parentBookCollectionId != null) {
			bookCollectionListQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		bookCollectionListQuery.setFirstResult((page - 1) * pageSize);
		bookCollectionListQuery.setMaxResults(pageSize);
		
		List<BookCollection> bookCollectionList = bookCollectionListQuery.getResultList();
        
        PageableList<BookCollection> bookCollectionPageableList = new PageableList<BookCollection>(bookCollectionList, bookCollectionListSize, page, pageSize);
        
        return bookCollectionPageableList;
	}
	
	public PageableList<BookCollection> getBookCollectionsByBookCollectionIdAndName(Long rootBookCollectionId, Long parentBookCollectionId, String name, Integer page, Integer pageSize) throws ProblemException {
		String normalizedName = NameHelper.getNormalizedName(name);
		
		String bookCollectionListQueryString = " where 1 = 1";
		
		bookCollectionListQueryString = bookCollectionListQueryString + " and (bc.id = :rootBookCollectionId or bc.rootBookCollection.id = :rootBookCollectionId)";
		
		if(parentBookCollectionId != null) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.id in (select cbc.id from BookCollection pbc join pbc.childBookCollections cbc where pbc.id = :parentBookCollectionId)";
		}
		
		if("".equals(normalizedName) == false) {
			bookCollectionListQueryString = bookCollectionListQueryString + " and bc.normalizedName like :normalizedName";
		}
		
		Query bookCollectionListSizeQuery = entityManager.createQuery("select count(bc.id) from BookCollection bc" + bookCollectionListQueryString);
		bookCollectionListSizeQuery.setParameter("rootBookCollectionId", rootBookCollectionId);
		
		if(parentBookCollectionId != null) {
			bookCollectionListSizeQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		if("".equals(normalizedName) == false) {
			bookCollectionListSizeQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
		Long bookCollectionListSize = (Long) bookCollectionListSizeQuery.getSingleResult();
		
		TypedQuery<BookCollection> bookCollectionListQuery = entityManager.createQuery("select bc from BookCollection bc" + bookCollectionListQueryString + " order by bc.number asc", BookCollection.class);
		bookCollectionListQuery.setParameter("rootBookCollectionId", rootBookCollectionId);
		
		if(parentBookCollectionId != null) {
			bookCollectionListQuery.setParameter("parentBookCollectionId", parentBookCollectionId);
		}
		
		if("".equals(normalizedName) == false) {
			bookCollectionListQuery.setParameter("normalizedName", "%" + normalizedName + "%");
		}
		
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
}
