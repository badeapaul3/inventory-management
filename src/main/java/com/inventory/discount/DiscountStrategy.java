package com.inventory.discount;

import java.math.BigDecimal;

/**
 * @author hatzp
 **/
public interface DiscountStrategy {
    BigDecimal applyDiscount(BigDecimal price);
}
