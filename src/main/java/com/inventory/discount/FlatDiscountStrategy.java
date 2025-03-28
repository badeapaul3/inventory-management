package com.inventory.discount;

import java.math.BigDecimal;

/**
 * @author hatzp
 **/
public class FlatDiscountStrategy implements DiscountStrategy{
    private final BigDecimal discountAmount;

    public FlatDiscountStrategy(BigDecimal discountAmount){
        this.discountAmount = discountAmount;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal price){
        return price.subtract(discountAmount);
    }
}
