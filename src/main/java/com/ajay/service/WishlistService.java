package com.ajay.service;


import java.util.Optional;

import com.ajay.exception.WishlistNotFoundException;
import com.ajay.model.Product;
import com.ajay.model.User;
import com.ajay.model.Wishlist;

public interface WishlistService {

    Wishlist createWishlist(User user);

    Wishlist getWishlistByUserId(User user);

    Wishlist addProductToWishlist(User user, Product product) throws WishlistNotFoundException;

}

