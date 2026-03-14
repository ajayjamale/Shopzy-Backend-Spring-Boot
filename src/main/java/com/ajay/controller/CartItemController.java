package com.ajay.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ajay.exception.CartItemException;
import com.ajay.exception.UserException;
import com.ajay.model.CartItem;
import com.ajay.model.User;
import com.ajay.response.ApiResponse;
import com.ajay.service.CartItemService;
import com.ajay.service.UserService;

@RestController
@RequestMapping("/api/cart_items")
public class CartItemController {

	private CartItemService cartItemService;
	private UserService userService;
	
	public CartItemController(CartItemService cartItemService, UserService userService) {
		this.cartItemService=cartItemService;
		this.userService=userService;
	}
	

}
