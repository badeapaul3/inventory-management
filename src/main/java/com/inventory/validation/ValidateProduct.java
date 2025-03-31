package com.inventory.validation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateProduct {
    boolean notNull() default true; // Whether the field can be null
    boolean notEmpty() default false; // String if it can be empty
    double minValue() default Double.MIN_VALUE; // Minimum value for numeric fields
    boolean allowPastDate() default  true; // For LocalDate: allow past dates

}
