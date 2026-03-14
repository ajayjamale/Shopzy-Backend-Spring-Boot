package com.ajay.service;

import com.ajay.exception.CartItemException;
import com.ajay.exception.UserException;
import com.ajay.model.Cart;
import com.ajay.model.CartItem;
import com.ajay.model.Product;


public interface CartItemService {
	
	public CartItem updateCartItem(Long userId, Long id,CartItem cartItem) throws CartItemException, UserException;
	
	public void removeCartItem(Long userId,Long cartItemId) throws CartItemException, UserException;
	
	public CartItem findCartItemById(Long cartItemId) throws CartItemException;
	
}
