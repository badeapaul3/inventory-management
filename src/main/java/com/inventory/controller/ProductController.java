package com.inventory.controller;

import com.inventory.discount.DiscountManager;
import com.inventory.exception.ExpiredProductException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import com.inventory.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Paul Badea
 **/

@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final DiscountManager discountManager;

    public ProductController(ProductService productService, DiscountManager discountManager) {
        this.productService = productService;
        this.discountManager = discountManager;
    }


    @GetMapping
    public List<Product> getAllProducts(){
        logger.info("GET /products - Fetching all products");
        return productService.getAllProducts();
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        logger.info("POST /products - Adding product: {}", product);
        productService.addProduct(product);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product product){
        logger.info("PUT /products/{} - Updating product", id);
        if(product.id() != id){
            throw new IllegalArgumentException("Product ID in path must match the body");
        }
        productService.updateProduct(product);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        logger.info("DELETE /products/{} - Deleting product", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        logger.info("GET /products/search?name={} - Searching products", name);
        return productService.findProductsByName(name);
    }

    @PostMapping("/{id}/discount")
    public ResponseEntity<Product> applyDiscount(@PathVariable int id) {
        logger.info("POST /products/{}/discount - Applying discount", id);
        Product discounted = discountManager.applyDynamicDiscount(id);
        return ResponseEntity.ok(discounted);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Void> adjustStock(@PathVariable int id, @RequestParam int amount) {
        logger.info("PUT /products/{}/stock?amount={} - Adjusting stock", id, amount);
        productService.adjustStock(id, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auto-discount")
    public ResponseEntity<Void> adjustStockForExpired() {
        logger.info("POST /products/auto-discount - Adjusting expired stock");
        productService.adjustStockForExpired();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/expiring-before")
    public List<Product> findProductsExpiringBefore(@RequestParam String date) {
        logger.info("GET /products/expiring-before?date={} - Finding expiring products", date);
        LocalDate localDate = LocalDate.parse(date);
        return productService.findProductsExpiringBefore(localDate);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("Bad request: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ProductNotFoundException e) {
        logger.warn("Not found: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExpiredProductException.class)
    public ResponseEntity<String> handleExpired(ExpiredProductException e) {
        logger.warn("Expired product: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        logger.error("Internal server error: {}", e.getMessage(), e);
        return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
