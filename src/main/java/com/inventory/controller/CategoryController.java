package com.inventory.controller;

import com.inventory.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private final ProductService productService;

    // Constructor injection
    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Integer> addCategory(@RequestBody CategoryRequest request) {
        logger.info("POST /categories - Adding category: {}", request.name);
        int id = productService.addCategory(request.name);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    record CategoryRequest(String name) {}
}