package com.inventory.controller;

import com.inventory.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private final ProductService productService;

    // Constructor injection
    public SupplierController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Integer> addSupplier(@RequestBody SupplierRequest request) {
        logger.info("POST /suppliers - Adding supplier: {} with contact: {}", request.name, request.contactInfo);
        int id = productService.addSupplier(request.name, request.contactInfo);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    record SupplierRequest(String name, String contactInfo) {}
}