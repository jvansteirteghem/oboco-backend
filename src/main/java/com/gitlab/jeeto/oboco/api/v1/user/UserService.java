package com.gitlab.jeeto.oboco.api.v1.user;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.common.Graph;
import com.gitlab.jeeto.oboco.common.PageableList;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;

@RequestScoped
public class UserService {
	@Inject
	EntityManager entityManager;
	
	public UserService() {
		super();
	}
	
	private String getPasswordHash(String password) {
		String passwordHash = BCrypt.with(BCrypt.Version.VERSION_2A, LongPasswordStrategies.truncate(BCrypt.Version.VERSION_2A))
				.hashToString(12, password.toCharArray());
		return passwordHash;
	}
	
	private boolean isPassword(String password, String passwordHash) {
		BCrypt.Result result = BCrypt.verifyer(BCrypt.Version.VERSION_2A, LongPasswordStrategies.truncate(BCrypt.Version.VERSION_2A))
				.verify(password.toCharArray(), passwordHash);
		if(result.verified) {
			return true;
		} else {
			return false;
		}
	}
	
	@Transactional
	public User createUser(User user, Graph graph) throws ProblemException {
		if(user.getPassword() != null) {
			String passwordHash = getPasswordHash(user.getPassword());
			
			user.setPasswordHash(passwordHash);
		}
		
		entityManager.persist(user);
		
		user = getUserById(user.getId(), graph);
        
        return user;
	}
	
	@Transactional
	public User updateUser(User user, Graph graph) throws ProblemException {
		if(user.getPassword() != null) {
			String passwordHash = getPasswordHash(user.getPassword());
			
			user.setPasswordHash(passwordHash);
		}
		
		user = entityManager.merge(user);
		
		user = getUserById(user.getId(), graph);
		
		return user;
	}
	
	public User getUserById(Long id, Graph graph) throws ProblemException {
		User user = null;
		
		try {
			EntityGraph<User> entityGraph = entityManager.createEntityGraph(User.class);
			entityGraph.addAttributeNodes("roles");
			
			if(graph != null) {
				if(graph.containsKey("rootBookCollection")) {
					entityGraph.addSubgraph("rootBookCollection", BookCollection.class);
				}
			}
			
			user = entityManager.createQuery("select u from User u where u.id = :id", User.class)
					.setParameter("id", id)
					.setHint("javax.persistence.loadgraph", entityGraph)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return user;
	}
	
	public User getUserByName(String name, Graph graph) throws ProblemException {
		User user = null;
		
		try {
			EntityGraph<User> entityGraph = entityManager.createEntityGraph(User.class);
			entityGraph.addAttributeNodes("roles");
			
			if(graph != null) {
				if(graph.containsKey("rootBookCollection")) {
					entityGraph.addSubgraph("rootBookCollection", BookCollection.class);
				}
			}
			
			user = entityManager.createQuery("select u from User u where u.name = :name", User.class)
					.setParameter("name", name)
					.setHint("javax.persistence.loadgraph", entityGraph)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
		
        return user;
	}
	
	public User getUserByNameAndPassword(String name, String password) throws ProblemException {
		User user = null;
		
		try {
			EntityGraph<User> entityGraph = entityManager.createEntityGraph(User.class);
			entityGraph.addAttributeNodes("roles");
			
			user = entityManager.createQuery("select u from User u where u.name = :name", User.class)
					.setParameter("name", name)
					.setHint("javax.persistence.loadgraph", entityGraph)
					.getSingleResult();
		} catch(NoResultException e) {
			
		}
        
        return getUserByNameAndPassword(user, password);
	}
	
	public User getUserByNameAndPassword(User user, String password) throws ProblemException {
		if(user != null) {
			String passwordHash = user.getPasswordHash();
			
			if(isPassword(password, passwordHash) == false) {
				user = null;
			}
		}
        
        return user;
	}
	
	public PageableList<User> getUsers(Integer page, Integer pageSize, Graph graph) throws ProblemException {
		EntityGraph<User> entityGraph = entityManager.createEntityGraph(User.class);
		entityGraph.addAttributeNodes("roles");
		
		if(graph != null) {
			if(graph.containsKey("rootBookCollection")) {
				entityGraph.addSubgraph("rootBookCollection", BookCollection.class);
			}
		}
		
		Long userListSize = (Long) entityManager.createQuery("select count(u.id) from User u")
				.getSingleResult();
		
		List<Long> userIdList = entityManager.createQuery("select u.id from User u", Long.class)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
		
		List<User> userList = entityManager.createQuery("select u from User u where u.id in :userIdList", User.class)
				.setParameter("userIdList", userIdList)
				.setHint("javax.persistence.loadgraph", entityGraph)
				.getResultList();
		
        PageableList<User> userPageableList = new PageableList<User>(userList, userListSize, page, pageSize);
        
        return userPageableList;
	}
	
	@Transactional
	public void deleteUser(User user) throws ProblemException {
		user = entityManager.merge(user);
		
		entityManager.remove(user);
	}
	
	@Transactional
	public void delete() throws ProblemException {
		entityManager.createQuery("delete from User")
			.executeUpdate();
	}
}
