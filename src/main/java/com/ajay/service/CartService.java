package com.ajay.service;

import com.ajay.exception.ProductException;
import com.ajay.model.Cart;
import com.ajay.model.CartItem;
import com.ajay.model.Product;
import com.ajay.model.User;
import com.ajay.request.AddItemRequest;

public interface CartService {
	
	public CartItem addCartItem(User user,
								Product product,
								String size,
								int quantity) throws ProductException;
	
	public Cart findUserCart(User user);

}
