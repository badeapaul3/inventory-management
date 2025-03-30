package com.inventory.discount;

import com.inventory.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author hatzp
 **/
public class DiscountService {

    public Product applyDiscount(Product product, DiscountStrategy strategy){
        if(product == null || strategy == null){
            throw new IllegalArgumentException("Product and DiscountStrategy cannot be null.");
        }

        BigDecimal originalPrice = BigDecimal.valueOf(product.price());
        BigDecimal discountedPrice = strategy.applyDiscount(originalPrice).setScale(2, RoundingMode.HALF_UP);

        if(discountedPrice.compareTo(BigDecimal.ZERO) < 0) discountedPrice = BigDecimal.ZERO;

        return new Product(
                product.id(),
                product.name(),
                discountedPrice.doubleValue(),
                product.stock(),
                product.expirationDate()
        );
    }
}
