package com.ajay.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

	public User findByEmail(String username);

    List<User> findByRole(com.ajay.domains.USER_ROLE role);
}

