package com.ajay.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ajay.exception.OrderException;
import com.ajay.model.OrderItem;
import com.ajay.repository.OrderItemRepository;
import com.ajay.service.OrderItemService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {


    private final OrderItemRepository orderItemRepository;


    @Override
    public OrderItem getOrderItemById(Long id) throws Exception {

        System.out.println("------- "+id);
        Optional<OrderItem> optionalOrderItem = orderItemRepository.findById(id);
        if(optionalOrderItem.isPresent()){
            return optionalOrderItem.get();
        }
        throw new OrderException("Order item not found");
    }
}

