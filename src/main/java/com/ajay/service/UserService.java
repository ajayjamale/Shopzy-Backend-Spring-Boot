package com.ajay.service;

import java.util.List;

import com.ajay.exception.UserException;
import com.ajay.model.User;

public interface UserService {

	public User findUserProfileByJwt(String jwt) throws UserException;
	
	public User findUserByEmail(String email) throws UserException;

	List<User> getAllUsers();

	List<User> getUsersByRole(com.ajay.domains.USER_ROLE role);


}

