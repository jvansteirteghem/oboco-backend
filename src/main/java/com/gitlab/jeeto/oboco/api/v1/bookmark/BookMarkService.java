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
	
	public BookMarkReference getLastBookMarkReferenceByBookCollectionIdAndUserId(Long rootBookCollectionId, Long userId) throws ProblemException {
		BookMarkReference bookMarkReference = null;
		
		try {
			bookMarkReference = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.rootBookCollection.id = :rootBookCollectionId and bmr.user.id = :userId order by bmr.bookMark.updateDate desc", BookMarkReference.class)
				.setParameter("rootBookCollectionId", rootBookCollectionId)
				.setParameter("userId", userId)
				.setMaxResults(1)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMarkReference;
	}
	
	public BookMarkReference getBookMarkReferenceByBookCollectionIdAndUserIdAndId(Long rootBookCollectionId, Long userId, Long id) throws ProblemException {
		BookMarkReference bookMarkReference = null;
		
		try {
			bookMarkReference = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.rootBookCollection.id = :rootBookCollectionId and bmr.user.id = :userId and bmr.id = :id", BookMarkReference.class)
				.setParameter("rootBookCollectionId", rootBookCollectionId)
				.setParameter("userId", userId)
				.setParameter("id", id)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMarkReference;
	}
	
	public BookMarkReference getBookMarkReferenceByBookCollectionIdAndUserIdAndBookId(Long rootBookCollectionId, Long userId, Long bookId) throws ProblemException {
		BookMarkReference bookMarkReference = null;
		
		try {
			bookMarkReference = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.rootBookCollection.id = :rootBookCollectionId and bmr.user.id = :userId and bmr.book.id = :bookId", BookMarkReference.class)
				.setParameter("rootBookCollectionId", rootBookCollectionId)
				.setParameter("userId", userId)
				.setParameter("bookId", bookId)
				.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return bookMarkReference;
	}
	
	public PageableList<BookMarkReference> getBookMarkReferencesByBookCollectionIdAndUserId(Long rootBookCollectionId, Long userId, Integer page, Integer pageSize) throws ProblemException {
		Long bookMarkListSize = (Long) entityManager.createQuery("select count(bmr.id) from BookMarkReference bmr where bmr.rootBookCollection.id = :rootBookCollectionId and bmr.user.id = :userId")
				.setParameter("rootBookCollectionId", rootBookCollectionId)
				.setParameter("userId", userId)
				.getSingleResult();
		
		List<BookMarkReference> bookMarkList = entityManager.createQuery("select bmr from BookMarkReference bmr where bmr.rootBookCollection.id = :rootBookCollectionId and bmr.user.id = :userId order by bmr.bookMark.updateDate desc", BookMarkReference.class)
				.setParameter("rootBookCollectionId", rootBookCollectionId)
				.setParameter("userId", userId)
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
		bookMark = entityManager.merge(bookMark);
		
		entityManager.remove(bookMark);
	}
	
	@Transactional
	public void deleteBookMarkByUserId(Long userId) throws ProblemException {
		entityManager.createQuery("delete from BookMarkReference bmr where bmr.user.id = :userId")
		.setParameter("userId", userId)
		.executeUpdate();
		
		entityManager.createQuery("delete from BookMark bm where bm.user.id = :userId")
		.setParameter("userId", userId)
		.executeUpdate();
	}
	
	public List<BookMark> getBookMarksByFileId(String fileId) throws ProblemException {
		List<BookMark> bookMarkList = entityManager.createQuery("select bm from BookMark bm where bm.fileId = :fileId", BookMark.class)
				.setParameter("fileId", fileId)
				.getResultList();
		
        return bookMarkList;
	}
}
