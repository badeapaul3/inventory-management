package com.inventory.discount;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiscountService {
    private final ProductDAO productDAO;

    public DiscountService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Product applyDiscount(Product product, DiscountStrategy strategy) {
        if (product == null || strategy == null) {
            throw new IllegalArgumentException("Product and DiscountStrategy cannot be null.");
        }

        BigDecimal originalPrice = BigDecimal.valueOf(product.price());
        BigDecimal discountedPrice = strategy.applyDiscount(originalPrice)
                .setScale(2, RoundingMode.HALF_UP);

        if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
            discountedPrice = BigDecimal.ZERO;
        }

        return new Product(
                product.id(),
                product.name(),
                discountedPrice.doubleValue(),
                product.stock(),
                product.expirationDate(),
                true // Mark as discounted
        );
    }

    public void applyAndSaveDiscount(Product product, DiscountStrategy strategy) {
        if (productDAO == null) {
            throw new IllegalStateException("ProductDAO not initialized. Use constructor with ProductDAO.");
        }

        Product discountedProduct = applyDiscount(product, strategy);
        productDAO.updateProduct(discountedProduct);
    }
}