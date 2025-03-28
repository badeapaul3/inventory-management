package com.inventory.discount;

import java.math.BigDecimal;

public class PercentageDiscountStrategy implements DiscountStrategy {
    private final BigDecimal percentage;

    public PercentageDiscountStrategy(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal price) {
        return price.subtract(price.multiply(percentage).divide(BigDecimal.valueOf(100)));
    }
}