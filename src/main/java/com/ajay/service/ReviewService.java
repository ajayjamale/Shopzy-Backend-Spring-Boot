package com.ajay.service;

import javax.naming.AuthenticationException;

import com.ajay.exception.ReviewNotFoundException;
import com.ajay.model.Product;
import com.ajay.model.Review;
import com.ajay.model.User;
import com.ajay.request.CreateReviewRequest;

import java.util.List;

public interface ReviewService {

    Review createReview(CreateReviewRequest req,
                        User user,
                        Product product);

    List<Review> getReviewsByProductId(Long productId);

    Review updateReview(Long reviewId,
                        String reviewText,
                        double rating,
                        Long userId) throws ReviewNotFoundException, AuthenticationException;


    void deleteReview(Long reviewId, Long userId) throws ReviewNotFoundException, AuthenticationException;

}
