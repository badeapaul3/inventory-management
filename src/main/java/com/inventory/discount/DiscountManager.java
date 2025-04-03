package com.inventory.discount;

import com.inventory.config.ConfigManager;
import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DiscountManager {
    private static final Logger logger = LoggerFactory.getLogger(DiscountManager.class);
    private final ProductDAO productDAO;
    private final int EXPIRATION_THRESHOLD_DAYS;
    private final BigDecimal NEAR_EXPIRY_DISCOUNT_PERCENT;

    public DiscountManager(ProductDAO productDAO) {
        this.productDAO = productDAO;
        ConfigManager config = ConfigManager.getInstance();
        this.EXPIRATION_THRESHOLD_DAYS = config.getIntProperty("discount.expiration.threshold.days", 30);
        this.NEAR_EXPIRY_DISCOUNT_PERCENT = BigDecimal.valueOf(
                config.getDoubleProperty("discount.near.expiry.percent", 20)
        );
        logger.debug("Initialized with threshold: {} days, discount: {}%",
                EXPIRATION_THRESHOLD_DAYS, NEAR_EXPIRY_DISCOUNT_PERCENT);
    }

    public int applyDynamicDiscounts() {
        logger.debug("Applying dynamic discounts to all products.");
        int productsDiscounted = 0;
        List<Product> products = productDAO.getAllProducts(false);
        DiscountService discountService = new DiscountService(productDAO);

        for (Product product : products) {
            if (!product.discounted()) {
                DiscountStrategy strategy = determineDiscountStrategy(product);
                if (strategy != null) {
                    discountService.applyAndSaveDiscount(product, strategy);
                    productsDiscounted++;
                    logger.info("Discount applied to product ID: {}", product.id());
                }
            } else {
                logger.debug("Skipping already discounted product ID: {}", product.id());
            }
        }
        logger.info("Applied discounts to {} products.", productsDiscounted);
        return productsDiscounted;
    }

    private DiscountStrategy determineDiscountStrategy(Product product) {
        LocalDate today = LocalDate.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, product.expirationDate());

        if (daysUntilExpiry <= EXPIRATION_THRESHOLD_DAYS && daysUntilExpiry > 0) {
            logger.debug("Product ID: {} qualifies for near-expiry discount ({} days).", product.id(), daysUntilExpiry);
            return new PercentageDiscountStrategy(NEAR_EXPIRY_DISCOUNT_PERCENT);
        }
        logger.debug("No discount strategy for product ID: {}, days until expiry: {}", product.id(), daysUntilExpiry);
        return null;
    }

    public Product applyDynamicDiscount(int id) {
        logger.debug("Applying dynamic discount to product ID: {}", id);
        Product product = getProductById(id);
        if (!product.discounted()) {
            DiscountService discountService = new DiscountService(productDAO);
            DiscountStrategy strategy = determineDiscountStrategy(product);
            if (strategy != null) {
                discountService.applyAndSaveDiscount(product, strategy);
                Product updated = getProductById(id);
                logger.info("Discount applied to product ID: {}, new price: {}", id, updated.price());
                return updated;
            }
        }
        logger.debug("No discount applied to product ID: {}", id);
        return product;
    }

    private Product getProductById(int id) {
        Product product = productDAO.getAllProducts(false).stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Product not found for discount, ID: {}", id);
                    return new IllegalArgumentException("No product found with ID: " + id);
                });
        logger.debug("Retrieved product ID: {} for discount check.", id);
        return product;
    }
}