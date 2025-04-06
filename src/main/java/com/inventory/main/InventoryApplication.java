package com.inventory.main;

import com.inventory.config.ConfigManager;
import com.inventory.dao.CategoryDAO;
import com.inventory.dao.HistoryDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.database.DatabaseInitializer;
import com.inventory.database.DatabaseManager;
import com.inventory.discount.DiscountManager;
import com.inventory.service.ProductService;
import com.inventory.service.ProductServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Paul Badea
 **/

@SpringBootApplication
@ComponentScan(basePackages = "com.inventory")
public class InventoryApplication {
    public static final Logger logger = LoggerFactory.getLogger(InventoryApplication.class);
    private static Connection sharedConnection; // Singleton connection

    public static void main(String[] args){
        try{
            DatabaseInitializer.initializeDatabase();
            logger.info("Database initialized successfully.");
        } catch (SQLException e) {
            logger.error("Failed to initialize database: {}", e.getMessage(), e);
            System.exit(1);
        }
        SpringApplication.run(InventoryApplication.class, args);
        logger.info("Inventory REST API started on http://localhost:8080");
    }

    @Bean
    @Scope("singleton") // Explicitly ensure singleton scope
    public Connection connection() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed()) {
            sharedConnection = DatabaseManager.getInstance().getConnection();
            sharedConnection.setAutoCommit(true); // Ensure each operation commits immediately
            logger.info("Created singleton database connection.");
        }
        return sharedConnection;
    }

    @Bean(destroyMethod = "close")
    public Connection connectionCleanup() throws SQLException {
        return connection(); // Returns the singleton connection for Spring to manage
    }

    @Bean
    public ProductDAO productDAO(Connection connection){
        return new ProductDAO(connection, historyDAO(connection));
    }

    @Bean
    public HistoryDAO historyDAO(Connection connection){
        return new HistoryDAO(connection);
    }

    @Bean
    public CategoryDAO categoryDAO(Connection connection) {
        return new CategoryDAO(connection);
    }

    @Bean
    public SupplierDAO supplierDAO(Connection connection) {
        return new SupplierDAO(connection);
    }

    @Bean
    public ProductService productService(ProductDAO productDAO, CategoryDAO categoryDAO, SupplierDAO supplierDAO, HistoryDAO historyDAO) {
        return new ProductServiceImpl(productDAO, categoryDAO, supplierDAO, historyDAO);
    }

    @Bean
    public DiscountManager discountManager(ProductDAO productDAO) {
        return new DiscountManager(productDAO);
    }

    @Bean
    public ConfigManager configManager() {
        return ConfigManager.getInstance();
    }


}
//add these too
//8. POST /products/{id}/discount - Apply Discount to a Product
//Method: POST
//URL: http://localhost:8080/products/1/discount (use a valid product ID)
//Headers: None required
//Body: None
//Expected Response:
//Status: 200 OK
//Body: Updated product JSON (e.g., price reduced by 20% to 3.6 if original was 4.5)
//json
//
//Collapse
//
//Wrap
//
//Copy
//{
//  "id": 1,
//  "name": "Milk",
//  "price": 3.6,
//  "stock": 90,
//  "expirationDate": "2025-06-30",
//  "discounted": true,
//  "categoryId": 1,
//  "supplierId": 1
//}
//Notes: Discount applies if within 30 days of expiry (adjust expirationDate to test).
//9. PUT /products/{id}/stock - Adjust Stock
//Method: PUT
//URL: http://localhost:8080/products/1/stock?amount=10 (use a valid product ID)
//Headers: None required
//Body: None
//Expected Response:
//Status: 200 OK
//Body: Empty
//Notes: Stock increases to 100. Test with negative amount (e.g., -20) to decrease.
//10. POST /products/auto-discount - Clear Expired Stock
//Method: POST
//URL: http://localhost:8080/products/auto-discount
//Headers: None required
//Body: None
//Expected Response:
//Status: 200 OK
//Body: Empty
//Setup: Add a product with an expired date:
//json
//
//Collapse
//
//Wrap
//
//Copy
//{
//  "id": 0,
//  "name": "ExpiredMilk",
//  "price": 5.0,
//  "stock": 50,
//  "expirationDate": "2024-01-01",
//  "discounted": false,
//  "categoryId": 1,
//  "supplierId": 1
//}
//Notes: Check with GET /products afterward; stock should be 0.
//11. GET /products/expiring-before - Find Products Expiring Before a Date
//Method: GET
//URL: http://localhost:8080/products/expiring-before?date=2025-07-01
//Headers: None required
//Body: None
//Expected Response:
//Status: 200 OK
//Body: List of products expiring before July 1, 2025 (e.g., "Milk" from above).
//Notes: Test with different dates (e.g., 2025-06-01 to exclude "Milk").