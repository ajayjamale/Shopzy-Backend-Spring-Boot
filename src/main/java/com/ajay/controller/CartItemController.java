package com.ajay.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.ajay.service.CartItemService;
import com.ajay.service.UserService;

@RestController
@RequestMapping("/api/cart_items")
@RequiredArgsConstructor
public class CartItemController {

	private final CartItemService cartItemService;
	private final UserService userService;
	

}

