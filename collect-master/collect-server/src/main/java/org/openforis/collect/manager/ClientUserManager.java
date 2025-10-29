package org.openforis.collect.manager;

import static org.openforis.collect.config.CollectConfiguration.getUsersRestfulApiUrl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.client.AbstractClient;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;

public class ClientUserManager extends AbstractClient implements UserManager  {

	@Override
	public User loadById(Integer userId) {
		User user = getOne(getUsersRestfulApiUrl() + "/user/" + userId, User.class);
		setGenericRoles(user);
		return user;
	}

	@Override
	public User loadByUserName(String userName) {
		List<User> list = getList(String.format(getUsersRestfulApiUrl() + "/user?username=%s", userName), User.class);
		adaptRoles(list);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public User loadEnabledUser(String userName) {
		List<User> list = getList(String.format(getUsersRestfulApiUrl() + "/user?username=%s&enabled=true", userName), User.class);
		adaptRoles(list);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public User loadAdminUser() {
		User user = loadByUserName(ADMIN_USER_NAME);
		setGenericRoles(user);
		return user;
	}

	@Override
	public List<User> loadAll() {
		List<User> list = getList(String.format(getUsersRestfulApiUrl() + "/user"), User.class);
		adaptRoles(list);
		return list;
	}

	@Override
	public List<User> loadAllAvailableUsers(User availableTo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public User save(User user, User modifiedByUser) throws UserPersistenceException {
		User result = post(getUsersRestfulApiUrl() + "/user", user, User.class);
		user.setId(result.getId()); //if user was new
		return user;
	}

	@Override
	public User insertUser(String name, String rawPassword, UserRole role, User createdByUser) throws UserPersistenceException {
		User user = new User(name);
		user.setRawPassword(rawPassword);
		user.addRole(role);
		save(user, createdByUser);
		return user;
	}

	@Override
	public boolean verifyPassword(String username, String password) {
		@SuppressWarnings({ "serial", "unchecked" })
		Map<String, Object> result = post(getUsersRestfulApiUrl() + "/login", new HashMap<String, Object>(){{
			put("username", username);
			put("rawPassword", password);
		}}, Map.class);
		return (Boolean) result.get("success");
	}
	
	@Override
	@SuppressWarnings("serial")
	public OperationResult changePassword(final String username, final String oldPassword, final String newPassword) throws UserPersistenceException {
		OperationResult result = post(getUsersRestfulApiUrl() + "/change-password", new HashMap<String, Object>(){{
			put("username", username);
			put("oldPassword", oldPassword);
			put("newPassword", newPassword);
		}}, OperationResult.class);
		return result;
	}

	@Override
	public Boolean isDefaultAdminPasswordSet() {
		return verifyPassword(ADMIN_USER_NAME, ADMIN_DEFAULT_PASSWORD);
	}

	@Override
	public void deleteById(Integer id) throws CannotDeleteUserException {
		delete(getUsersRestfulApiUrl() + "/user/" + id);
	}

	@Override
	public void delete(User user) {
		deleteById(user.getId());
	}
	
	private void adaptRoles(List<User> list) {
		for (User user : list) {
			setGenericRoles(user);
		}
	}

	private void setGenericRoles(User user) {
		if (user != null) {
			UserRole role = UserRole.ADMIN;
			user.setRoles(Arrays.asList(role));
		}
	}
	
}
