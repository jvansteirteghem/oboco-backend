package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@RequestScoped
public class BookMarkService {
	@Inject
	EntityManager entityManager;
	
	public BookMarkService() {
		super();
	}
	
	// bookMarkReference
	
	@Transactional
	public BookMarkReference createBookMarkReference(BookMarkReference bookMarkReference) throws ProblemException {
		entityManager.persist(bookMarkReference);
		
        return bookMarkReference;
	}
	
	@Transactional
	public BookMarkReference updateBookMarkReference(BookMarkReference bookMarkReference) throws ProblemException {
		bookMarkReference = entityManager.merge(bookMarkReference);
		
        return bookMarkReference;
	}
	
	public BookMarkReference getLastBookMarkReferenceByUserName(String userName) throws ProblemException {
		BookMarkReference bookMarkReference = null;
		
		try {
			bookMarkReference = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.userName = :userName order by bmr.bookMark.updateDate desc", BookMarkReference.class)
				.setParameter("userName", userName)
				.setMaxResults(1)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMarkReference;
	}
	
	public BookMarkReference getBookMarkReferenceByUserNameAndId(String userName, Long id) throws ProblemException {
		BookMarkReference bookMarkReference = null;
		
		try {
			bookMarkReference = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.userName = :userName and bmr.id = :id", BookMarkReference.class)
				.setParameter("userName", userName)
				.setParameter("id", id)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMarkReference;
	}
	
	public BookMarkReference getBookMarkReferenceByUserNameAndBookId(String userName, Long bookId) throws ProblemException {
		BookMarkReference bookMarkReference = null;
		
		try {
			bookMarkReference = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.userName = :userName and bmr.book.id = :bookId", BookMarkReference.class)
				.setParameter("userName", userName)
				.setParameter("bookId", bookId)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMarkReference;
	}
	
	public PageableList<BookMarkReference> getBookMarkReferencesByUserName(String userName, Integer page, Integer pageSize) throws ProblemException {
		Long bookMarkListSize = (Long) entityManager.createQuery("select count(bmr.id) from BookMarkReference bmr where bmr.userName = :userName")
				.setParameter("userName", userName)
				.getSingleResult();
		
		List<BookMarkReference> bookMarkList = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.userName = :userName order by bmr.bookMark.updateDate desc", BookMarkReference.class)
				.setParameter("userName", userName)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
        
        PageableList<BookMarkReference> bookMarkPageableList = new PageableList<BookMarkReference>(bookMarkList, bookMarkListSize, page, pageSize);
        
        return bookMarkPageableList;
	}
	
	public List<BookMarkReference> getBookMarkReferencesByFileId(String fileId) throws ProblemException {
		List<BookMarkReference> bookMarkReferenceList = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.fileId = :fileId", BookMarkReference.class)
				.setParameter("fileId", fileId)
				.getResultList();
		
        return bookMarkReferenceList;
	}
	
	@Transactional
	public void deleteBookMarkReferenceByUpdateDate(Date updateDate) throws ProblemException {
		entityManager.createQuery("delete from BookMarkReference bmr where bmr.updateDate != :updateDate")
			.setParameter("updateDate", updateDate)
			.executeUpdate();
	}
	
	// bookMark
	
	@Transactional
	public BookMark createBookMark(BookMark bookMark) throws ProblemException {
		entityManager.persist(bookMark);
		
        return bookMark;
	}
	
	@Transactional
	public BookMark updateBookMark(BookMark bookMark) throws ProblemException {
		bookMark = entityManager.merge(bookMark);
		
        return bookMark;
	}
	
	@Transactional
	public void deleteBookMark(BookMark bookMark) throws ProblemException {
		entityManager.remove(bookMark);
	}
	
	@Transactional
	public void deleteBookMarkByUserName(String userName) throws ProblemException {
		entityManager.createQuery("delete from BookMarkReference bmr where bmr.userName = :userName")
			.setParameter("userName", userName)
			.executeUpdate();
		
		entityManager.createQuery("delete from BookMark bm where bm.userName = :userName")
		.setParameter("userName", userName)
		.executeUpdate();
	}
	
	public List<BookMark> getBookMarksByFileId(String fileId) throws ProblemException {
		List<BookMark> bookMarkList = entityManager.createQuery("select bm from BookMark bm where bm.fileId = :fileId", BookMark.class)
				.setParameter("fileId", fileId)
				.getResultList();
		
        return bookMarkList;
	}
}
