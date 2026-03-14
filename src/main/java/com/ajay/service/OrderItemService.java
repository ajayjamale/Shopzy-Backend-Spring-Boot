package com.ajay.service;


import com.ajay.exception.OrderException;
import com.ajay.model.OrderItem;
import com.ajay.model.Product;

public interface OrderItemService {

	OrderItem getOrderItemById(Long id) throws Exception;
	


}
