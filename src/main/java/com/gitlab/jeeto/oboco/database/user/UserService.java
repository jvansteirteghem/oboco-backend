package com.gitlab.jeeto.oboco.database.user;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

import com.gitlab.jeeto.oboco.database.Graph;
import com.gitlab.jeeto.oboco.database.PageableList;
import com.gitlab.jeeto.oboco.database.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.problem.ProblemException;

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
		
		user = getUser(user.getId(), graph);
        
        return user;
	}
	
	@Transactional
	public User updateUser(User user, Graph graph) throws ProblemException {
		if(user.getPassword() != null) {
			String passwordHash = getPasswordHash(user.getPassword());
			
			user.setPasswordHash(passwordHash);
		}
		
		user = entityManager.merge(user);
		
		user = getUser(user.getId(), graph);
		
		return user;
	}
	
	public User getUser(Long id, Graph graph) throws ProblemException {
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
	
	public User getUserByName(String name) throws ProblemException {
		return getUserByName(name, null);
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
		return getUserByNameAndPassword(name, password, null);
	}
	
	public User getUserByNameAndPassword(String name, String password, Graph graph) throws ProblemException {
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
		
		if(user != null) {
			if(isPassword(password, user.getPasswordHash()) == false) {
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
		
		// fix HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!
		List<Long> userIdList = entityManager.createQuery("select u.id from User u order by u.createDate asc", Long.class)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
		
		List<User> userList = entityManager.createQuery("select u from User u where u.id in :userIdList order by u.createDate asc", User.class)
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
	public void deleteUsers() throws ProblemException {
		entityManager.createQuery("delete from User")
			.executeUpdate();
	}
}
