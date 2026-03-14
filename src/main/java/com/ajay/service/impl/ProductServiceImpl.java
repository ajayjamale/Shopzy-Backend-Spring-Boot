package com.ajay.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.ajay.exception.ProductException;
import com.ajay.model.Category;
import com.ajay.model.Product;
import com.ajay.model.Seller;
import com.ajay.request.CreateProductRequest;
import com.ajay.service.ProductService;
import com.ajay.repository.CategoryRepository;
import com.ajay.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // createProduct
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Product createProduct(CreateProductRequest req, Seller seller) throws ProductException {

        // FIX 3: Resolve category string IDs → Category entities
        // The frontend sends category/category2/category3 as string IDs like "men",
        // "mens_clothing", etc. We need to look them up and link them properly,
        // creating them if they don't already exist (for leaf categories especially).
        Category category1Obj = categoryRepository.findByCategoryId(req.getCategory());
        if (category1Obj == null) {
            category1Obj = new Category();
            category1Obj.setCategoryId(req.getCategory());
            category1Obj.setName(req.getCategory());
            category1Obj.setLevel(1);
            category1Obj = categoryRepository.save(category1Obj);
        }

        Category category2Obj = categoryRepository.findByCategoryId(req.getCategory2());
        if (category2Obj == null) {
            category2Obj = new Category();
            category2Obj.setCategoryId(req.getCategory2());
            category2Obj.setName(req.getCategory2());
            category2Obj.setLevel(2);
            category2Obj.setParentCategory(category1Obj);
            category2Obj = categoryRepository.save(category2Obj);
        }

        Category category3Obj = null;
        if (req.getCategory3() != null && !req.getCategory3().isEmpty()) {
            category3Obj = categoryRepository.findByCategoryId(req.getCategory3());
            if (category3Obj == null) {
                category3Obj = new Category();
                category3Obj.setCategoryId(req.getCategory3());
                category3Obj.setName(req.getCategory3());
                category3Obj.setLevel(3);
                category3Obj.setParentCategory(category2Obj);
                category3Obj = categoryRepository.save(category3Obj);
            }
        }

        Product product = new Product();
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setMrpPrice(req.getMrpPrice());
        product.setSellingPrice(req.getSellingPrice());
        product.setColor(req.getColor());
        product.setSizes(req.getSizes());
        product.setImages(req.getImages());
        product.setQuantity(req.getQuantity()); // FIX 2: was never set before
        product.setSeller(seller);
        product.setCreatedAt(LocalDateTime.now());
        product.setIn_stock(true);
        product.setNumRatings(0);

        // Link the deepest available category (level 3 → 2 → 1 fallback)
        product.setCategory(category3Obj != null ? category3Obj : category2Obj);

        // FIX 1: Calculate and persist discountPercent
        // Use the value from the request if the frontend sent it (> 0).
        // Always recompute server-side as a safety fallback so it's never 0
        // just because the frontend forgot to send it.
        product.setDiscountPercent(resolveDiscount(
                req.getMrpPrice(),
                req.getSellingPrice(),
                req.getDiscountPercent()
        ));

        return productRepository.save(product);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateProduct
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Product updateProduct(Long productId, Product updated) throws ProductException {
        // Load the existing managed entity from the DB.
        // We NEVER call productRepository.save(updated) directly because the
        // incoming Product from @RequestBody is a detached/transient object —
        // it has no Hibernate session, and its @ManyToOne fields (seller,
        // category) are plain deserialized POJOs, not managed proxies.
        // Saving it directly would null-out or corrupt those relationships.
        Product existing = findProductById(productId);

        // Only copy the fields the seller is allowed to change.
        // seller, createdAt, numRatings, reviews stay from the existing entity.
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setMrpPrice(updated.getMrpPrice());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setColor(updated.getColor());
        existing.setSizes(updated.getSizes());
        existing.setImages(updated.getImages());
        existing.setQuantity(updated.getQuantity());
        existing.setIn_stock(updated.isIn_stock());

        // Always recalculate discountPercent from prices.
        // This is the core fix — discountPercent sent from the frontend is used
        // if valid (> 0), otherwise it is computed server-side as a safety net.
        // Products saved with discountPercent = 0 before the fix are also
        // self-healed here on their next update.
        existing.setDiscountPercent(resolveDiscount(
                updated.getMrpPrice(),
                updated.getSellingPrice(),
                updated.getDiscountPercent()
        ));

        return productRepository.save(existing);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateProductStock (toggle)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Product updateProductStock(Long productId) throws ProductException {
        Product product = findProductById(productId);
        product.setIn_stock(!product.isIn_stock());
        return productRepository.save(product);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteProduct
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void deleteProduct(Long productId) throws ProductException {
        Product product = findProductById(productId);
        productRepository.delete(product);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findProductById
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Product findProductById(Long id) throws ProductException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found with id: " + id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // searchProduct
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<Product> searchProduct(String query) {
        return productRepository.searchProduct(query);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // recentlyAddedProduct
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<Product> recentlyAddedProduct() {
        return productRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getProductBySellerId
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<Product> getProductBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllProduct  (customer-facing filtered/paginated list)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public Page<Product> getAllProduct(
            String category,
            String brand,
            String colors,
            String sizes,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber) {

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isBlank()) {
                // Match against the categoryId of the linked Category entity
                predicates.add(cb.like(
                        cb.lower(root.join("category").get("categoryId")),
                        "%" + category.toLowerCase() + "%"
                ));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.toLowerCase()));
            }
            if (colors != null && !colors.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("color")), colors.toLowerCase()));
            }
            if (sizes != null && !sizes.isBlank()) {
                // FIX: field is now "sizes" (lowercase) matching Product.java fix
                predicates.add(cb.equal(cb.lower(root.get("sizes")), sizes.toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }
            if (minDiscount != null && minDiscount > 0) {
                // FIX: this only returns meaningful results now that discountPercent
                // is actually being stored correctly (see createProduct fix above)
                predicates.add(cb.greaterThanOrEqualTo(root.get("discountPercent"), minDiscount));
            }
            if ("in_stock".equalsIgnoreCase(stock)) {
                predicates.add(cb.isTrue(root.get("in_stock")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sortOrder = Sort.unsorted();
        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "price_low"  -> sortOrder = Sort.by("sellingPrice").ascending();
                case "price_high" -> sortOrder = Sort.by("sellingPrice").descending();
                case "newest"     -> sortOrder = Sort.by("createdAt").descending();
                case "discount"   -> sortOrder = Sort.by("discountPercent").descending();
            }
        }

        Pageable pageable = PageRequest.of(pageNumber != null ? pageNumber : 0, 10, sortOrder);
        return productRepository.findAll(spec, pageable);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helper: resolve discountPercent with fallback calculation
    // ─────────────────────────────────────────────────────────────────────────
    private int resolveDiscount(int mrpPrice, int sellingPrice, int providedDiscount) {
        // If frontend sent a valid discount, trust it
        if (providedDiscount > 0) {
            return providedDiscount;
        }
        // Otherwise compute it server-side — this is the safety net
        if (mrpPrice > 0 && sellingPrice >= 0 && mrpPrice > sellingPrice) {
            return (int) Math.round(((double)(mrpPrice - sellingPrice) / mrpPrice) * 100);
        }
        return 0;
    }
}