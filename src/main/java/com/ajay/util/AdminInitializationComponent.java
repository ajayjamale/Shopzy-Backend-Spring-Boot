package com.ajay.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ajay.domains.USER_ROLE;
import com.ajay.model.User;
import com.ajay.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminInitializationComponent implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:devwithajay@gmail.com}")
    private String adminUsername;

    @Value("${app.bootstrap.admin.password:12345678}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        if (userRepository.findByEmail(adminUsername) == null) {
            User adminUser = new User();
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setFullName("Ajay Jamale");
            adminUser.setEmail(adminUsername);
            adminUser.setRole(USER_ROLE.ROLE_ADMIN);

            userRepository.save(adminUser);
            System.out.println("Admin user created: " + adminUsername);
        } else {
            System.out.println("Admin user already exists");
        }
    }
}

