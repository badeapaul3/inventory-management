package com.inventory.discount;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DiscountManager {
    private final ProductDAO productDAO;
    private static final int EXPIRATION_THRESHOLD_DAYS = 30;
    private static final BigDecimal NEAR_EXPIRY_DISCOUNT_PERCENT = BigDecimal.valueOf(20);

    public DiscountManager(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public int applyDynamicDiscounts() {
        int productsDiscounted = 0;
        List<Product> products = productDAO.getAllProducts(false);
        DiscountService discountService = new DiscountService(productDAO);

        for (Product product : products) {
            if (!product.discounted()) { // Skip if already discounted
                DiscountStrategy strategy = determineDiscountStrategy(product);
                if (strategy != null) {
                    discountService.applyAndSaveDiscount(product, strategy);
                    productsDiscounted++;
                }
            }
        }
        return productsDiscounted;
    }

    private DiscountStrategy determineDiscountStrategy(Product product) {
        LocalDate today = LocalDate.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, product.expirationDate());

        if (daysUntilExpiry <= EXPIRATION_THRESHOLD_DAYS && daysUntilExpiry > 0) {
            return new PercentageDiscountStrategy(NEAR_EXPIRY_DISCOUNT_PERCENT);
        }
        return null;
    }

    public Product applyDynamicDiscount(int id) {
        Product product = getProductById(id);
        if (!product.discounted()) { // Skip if already discounted
            DiscountService discountService = new DiscountService(productDAO);
            DiscountStrategy strategy = determineDiscountStrategy(product);
            if (strategy != null) {
                discountService.applyAndSaveDiscount(product, strategy);
                return getProductById(id); // Return updated product
            }
        }
        return product; // Return unchanged if no discount applied
    }

    private Product getProductById(int id) {
        List<Product> products = productDAO.getAllProducts(false);
        return products.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No product found with ID: " + id));
    }
}