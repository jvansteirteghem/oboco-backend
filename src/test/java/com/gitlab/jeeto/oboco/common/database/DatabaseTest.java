package com.gitlab.jeeto.oboco.common.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.gitlab.jeeto.oboco.api.v1.book.Book;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMark;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkReference;
import com.gitlab.jeeto.oboco.api.v1.user.User;

import junit.framework.TestCase;

public class DatabaseTest extends TestCase {
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		entityManagerFactory = Persistence.createEntityManagerFactory("default");
		
		entityManager = entityManagerFactory.createEntityManager();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		entityManager.close();
		
		entityManagerFactory.close();
	}
	
	private void scan(Date updateDate) throws Exception {
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			User user = new User();
			user.setCreateDate(updateDate);
			user.setUpdateDate(updateDate);
			user.setName("name");
			user.setPasswordHash("passwordHash");
			
			List<String> roleList = new ArrayList<String>();
			roleList.add("role");
			
			user.setRoles(roleList);
			
			entityManager.persist(user);
			
			System.out.println("created user " + user.getId());
			
			BookCollection bookCollection = new BookCollection();
			bookCollection.setDirectoryPath("/bookCollection");
			bookCollection.setCreateDate(updateDate);
			bookCollection.setUpdateDate(updateDate);
			bookCollection.setName("root");
			bookCollection.setNormalizedName("root");
			bookCollection.setParentBookCollection(null);
			bookCollection.setBookCollections(null);
			bookCollection.setNumberOfBookCollections(0);
			bookCollection.setBooks(null);
			bookCollection.setNumberOfBooks(0);
			bookCollection.setNumber(1);
			
			entityManager.persist(bookCollection);
			
			System.out.println("created bookCollection " + bookCollection.getId());
			
			Book book = new Book();
			book.setFileId("0000000000");
			book.setFilePath("/bookCollection/0000000000.cbz");
			book.setCreateDate(updateDate);
			book.setUpdateDate(updateDate);
			book.setName("0000000000");
			book.setNormalizedName("0000000000");
			book.setNumberOfPages(50);
			book.setRootBookCollection(bookCollection);
			book.setBookCollection(bookCollection);
			book.setNumber(1);
			
			entityManager.persist(book);
			
			System.out.println("created book " + book.getId());
			
			BookMark bookMark = new BookMark();
			bookMark.setUser(user);
			bookMark.setFileId("0000000000");
			bookMark.setCreateDate(updateDate);
			bookMark.setUpdateDate(updateDate);
			bookMark.setPage(1);
			
			entityManager.persist(bookMark);
			
			System.out.println("created bookMark " + bookMark.getId());
			
			BookMarkReference bookMarkReference = new BookMarkReference();
			bookMarkReference.setUser(user);
			bookMarkReference.setCreateDate(updateDate);
			bookMarkReference.setUpdateDate(updateDate);
			bookMarkReference.setBook(book);
			bookMarkReference.setBookMark(bookMark);
			bookMarkReference.setBookCollection(bookCollection);
			bookMarkReference.setRootBookCollection(bookCollection);
			
			entityManager.persist(bookMarkReference);
			
			System.out.println("created bookMarkReference " + bookMarkReference.getId());
		entityTransaction.commit();
		} catch(Exception e) {
			entityTransaction.rollback();
			
			throw e;
		}
	}

	public void testDeleteBook() throws Exception {
		Date updateDate = new Date();
		
		scan(updateDate);
		
		EntityTransaction entityTransaction;
		
		entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			entityManager.createQuery("delete from BookMarkReference bmr where bmr.id = :id")
				.setParameter("id", 1L)
				.executeUpdate();
			entityTransaction.commit();
		} catch(Exception e) {
			entityTransaction.rollback();
			
			throw e;
		}
		
		entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			entityManager.createQuery("delete from Book b where b.id = :id")
				.setParameter("id", 1L)
				.executeUpdate();
			entityTransaction.commit();
		} catch(Exception e) {
			entityTransaction.rollback();
			
			throw e;
		}
	}
	
	public void testDeleteBookCollection() throws Exception {
		Date updateDate = new Date();
		
		scan(updateDate);
		
		EntityTransaction entityTransaction;
		
		entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			entityManager.createQuery("delete from BookMarkReference bmr where bmr.id = :id")
				.setParameter("id", 1L)
				.executeUpdate();
			entityTransaction.commit();
		} catch(Exception e) {
			entityTransaction.rollback();
			
			throw e;
		}
		
		entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			entityManager.createQuery("delete from Book b where b.id = :id")
				.setParameter("id", 1L)
				.executeUpdate();
			entityTransaction.commit();
		} catch(Exception e) {
			entityTransaction.rollback();
			
			throw e;
		}
		
		entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		try {
			entityManager.createQuery("delete from BookCollection bc where bc.id = :id")
				.setParameter("id", 1L)
				.executeUpdate();
			entityTransaction.commit();
		} catch(Exception e) {
			entityTransaction.rollback();
			
			throw e;
		}
	}
}
