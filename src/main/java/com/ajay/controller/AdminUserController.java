package com.ajay.controller;

import com.ajay.domains.USER_ROLE;
import com.ajay.model.Seller;
import com.ajay.model.User;
import com.ajay.service.SellerService;
import com.ajay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final SellerService sellerService;

    @GetMapping
    public ResponseEntity<List<User>> listUsers(@RequestParam(required = false) USER_ROLE role) {
        if (role == USER_ROLE.ROLE_SELLER) {
            // This path returns sellers as users is not appropriate; use /admin/users/sellers instead
            return ResponseEntity.ok(List.of());
        }
        List<User> users = role == null
                ? userService.getAllUsers()
                : userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<Seller>> listSellers() {
        return ResponseEntity.ok(sellerService.getAllSellers(null));
    }
}

