package com.inventory.service;

import com.inventory.discount.DiscountStrategy;
import com.inventory.model.Product;

import java.time.LocalDate;
import java.util.List;


public interface ProductService {

    /**
     * Adds a new product or updates stock if it already exists.
     * @param product the product to add
     * @throws IllegalArgumentException if product validation fails
     */
    void addProduct(Product product);

    /**
     * Retrieves all products.
     * @return list of all products
     */
    List<Product> getAllProducts();

    /**
     * Updates an existing product.
     * @param product the product with updated details
     * @throws IllegalArgumentException if product ID is invalid or not found
     */
    void updateProduct(Product product);

    /**
     * Deletes a product by ID.
     * @param id the ID of the product to delete
     * @throws IllegalArgumentException if ID is invalid or not found
     */
    void deleteProduct(int id);

    /**
     * Adjusts the stock of a product by a given amount.
     * @param id the ID of the product
     * @param amount the amount to adjust (positive to add, negative to subtract)
     * @throws IllegalArgumentException if ID is invalid or stock would go negative
     */
    void adjustStock(int id, int amount);

    /**
     * Applies a manual discount to a specific product.
     * @param id the ID of the product
     * @param strategy the discount strategy to apply
     * @return the updated product with discount applied
     * @throws IllegalArgumentException if ID is invalid or not found
     */
    Product applyManualDiscount(int id, DiscountStrategy strategy);

    /**
     * Applies dynamic discounts to all eligible products.
     * @return the number of products discounted
     */
    int applyDynamicDiscounts();

    /**
     * Applies a dynamic discount to a specific product.
     * @param id the ID of the product
     * @return the updated product (with or without discount)
     * @throws IllegalArgumentException if ID is invalid or not found
     */
    Product applyDynamicDiscount(int id);

    /**
     * Finds products by partial name match.
     * @param name the name to search for
     * @return list of matching products
     * @throws IllegalArgumentException if name is null or empty
     */
    List<Product> findProductsByName(String name);

    /**
     * Finds products expiring before a given date.
     * @param date the date to check against
     * @return list of products expiring before the date
     * @throws IllegalArgumentException if date is null
     */
    List<Product> findProductsExpiringBefore(LocalDate date);




}
