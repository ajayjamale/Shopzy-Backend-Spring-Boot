package com.ajay.mapper;

import com.ajay.payload.response.ProductResponse;
import com.ajay.model.Product;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setMrpPrice(product.getMrpPrice());
        response.setSellingPrice(product.getSellingPrice());
        response.setDiscountPercent(product.getDiscountPercent());
        response.setQuantity(product.getQuantity());
        response.setColor(product.getColor());
        response.setImages(product.getImages());
        response.setNumRatings(product.getNumRatings());
        response.setCreatedAt(product.getCreatedAt());
        response.setSizes(product.getSizes());
        return response;
    }

    public static Product toEntity(ProductResponse response) {
        if (response == null) {
            return null;
        }
        Product product = new Product();
        product.setId(response.getId());
        product.setTitle(response.getTitle());
        product.setDescription(response.getDescription());
        product.setMrpPrice(response.getMrpPrice());
        product.setSellingPrice(response.getSellingPrice());
        product.setDiscountPercent(response.getDiscountPercent());
        product.setQuantity(response.getQuantity());
        product.setColor(response.getColor());
        product.setImages(response.getImages());
        product.setNumRatings(response.getNumRatings());
        product.setCreatedAt(response.getCreatedAt());
        product.setSizes(response.getSizes());
        return product;
    }

    public static Product updateEntity(Product product, Product sourceProduct) {
        if (product == null || sourceProduct == null) {
            return product;
        }
        product.setTitle(sourceProduct.getTitle());
        product.setDescription(sourceProduct.getDescription());
        product.setMrpPrice(sourceProduct.getMrpPrice());
        product.setSellingPrice(sourceProduct.getSellingPrice());
        product.setDiscountPercent(sourceProduct.getDiscountPercent());
        product.setQuantity(sourceProduct.getQuantity());
        product.setColor(sourceProduct.getColor());
        product.setImages(sourceProduct.getImages());
        product.setSizes(sourceProduct.getSizes());
        product.setIn_stock(sourceProduct.isIn_stock());
        return product;
    }

    public static ProductResponse toProductResponse(Product product) {
        return toResponse(product);
    }
}
