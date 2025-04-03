package com.inventory.main;

import com.inventory.dao.CategoryDAO;
import com.inventory.dao.HistoryDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.database.DatabaseInitializer;
import com.inventory.database.DatabaseManager;
import com.inventory.service.ProductService;
import com.inventory.service.ProductServiceImpl;
import com.inventory.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        logger.info("Starting with args: {}", String.join(" ", args));
        DatabaseInitializer.initializeDatabase();

        try (Connection connection = DatabaseManager.getInstance().getConnection();
             HistoryDAO historyDAO = new HistoryDAO(connection);
             ProductDAO productDAO = new ProductDAO(connection, historyDAO);
             CategoryDAO categoryDAO = new CategoryDAO(connection);
             SupplierDAO supplierDAO = new SupplierDAO(connection);
             Scanner scanner = new Scanner(System.in)) {

            ProductService productService = new ProductServiceImpl(productDAO, categoryDAO, supplierDAO, historyDAO);

            logger.info("Inventory application started.");
            while (true) {
                System.out.print("> "); // Keep console prompt
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    logger.info("Application exiting.");
                    break;
                }

                try {
                    String[] parts = input.split("\\s+");
                    String command = parts[0].toLowerCase();

                    switch (command) {
                        case "add":
                            handleAdd(productService, parts);
                            break;
                        case "list":
                            productService.getAllProducts().forEach(product -> {
                                System.out.println(product); // Keep console output
                                logger.debug("Listed product: {}", product);
                            });
                            break;
                        case "update":
                            handleUpdate(productService, parts);
                            break;
                        case "delete":
                            handleDelete(productService, parts);
                            break;
                        case "search":
                            handleSearch(productService, parts);
                            break;
                        case "discount":
                            handleDiscount(productService, parts);
                            break;
                        case "adjust":
                            handleAdjust(productService, parts);
                            break;
                        case "auto-discount":
                            productService.adjustStockForExpired();
                            logger.info("Expired stock adjusted.");
                            System.out.println("Expired stock adjusted."); // Keep console output
                            break;
                        case "add-category":
                            handleAddCategory(productService, parts);
                            break;
                        case "add-supplier":
                            handleAddSupplier(productService, parts);
                            break;
                        case "help":
                            handleHelp();
                            break;
                        default:
                            logger.warn("Unknown command: {}", command);
                            System.out.println("Unknown command: " + command + ". Type 'help' for available commands.");
                    }
                } catch (Exception e) {
                    logger.error("Command error: {}", e.getMessage(), e);
                    System.out.println("Error: " + e.getMessage()); // Keep console feedback
                }
            }
        } catch (SQLException e) {
            logger.error("Application startup error: {}", e.getMessage(), e);
            System.err.println("Application error: " + e.getMessage()); // Keep for now
        }
    }

    private static void handleAdd(ProductService productService, String[] parts) {
        if (parts.length != 8) {
            throw new IllegalArgumentException("Usage: add <name> <price> <stock> <expiration> <discounted> <categoryId> <supplierId>");
        }
        String name = parts[1].replace("\"", "");
        double price = Double.parseDouble(parts[2]);
        int stock = Integer.parseInt(parts[3]);
        LocalDate expiration = LocalDate.parse(parts[4], DATE_FORMATTER);
        boolean discounted = Boolean.parseBoolean(parts[5]);
        Integer categoryId = parts[6].equals("null") ? null : Integer.parseInt(parts[6]);
        Integer supplierId = parts[7].equals("null") ? null : Integer.parseInt(parts[7]);
        Product product = new Product(0, name, price, stock, expiration, discounted, categoryId, supplierId);
        productService.addProduct(product);
        logger.info("Product added: {}", product);
        System.out.println("Product added: " + product); // Keep console output
    }

    private static void handleUpdate(ProductService productService, String[] parts) {
        if (parts.length != 9) {
            throw new IllegalArgumentException("Usage: update <id> <name> <price> <stock> <expiration> <discounted> <categoryId> <supplierId>");
        }
        int id = Integer.parseInt(parts[1]);
        String name = parts[2].replace("\"", "");
        double price = Double.parseDouble(parts[3]);
        int stock = Integer.parseInt(parts[4]);
        LocalDate expiration = LocalDate.parse(parts[5], DATE_FORMATTER);
        boolean discounted = Boolean.parseBoolean(parts[6]);
        Integer categoryId = parts[7].equals("null") ? null : Integer.parseInt(parts[7]);
        Integer supplierId = parts[8].equals("null") ? null : Integer.parseInt(parts[8]);
        Product product = new Product(id, name, price, stock, expiration, discounted, categoryId, supplierId);
        productService.updateProduct(product);
        logger.info("Product updated: {}", product);
        System.out.println("Product updated: " + product); // Keep console output
    }

    private static void handleDelete(ProductService productService, String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Usage: delete <id>");
        }
        int id = Integer.parseInt(parts[1]);
        productService.deleteProduct(id);
        logger.info("Product deleted with ID: {}", id);
        System.out.println("Product deleted with ID: " + id); // Keep console output
    }

    private static void handleSearch(ProductService productService, String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Usage: search <name>");
        }
        String name = parts[1].replace("\"", "");
        logger.debug("Searching for products with name: {}", name);
        productService.findProductsByName(name).forEach(product -> {
            System.out.println(product); // Keep console output
            logger.debug("Search result: {}", product);
        });
    }

    private static void handleDiscount(ProductService productService, String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Usage: discount <id>");
        }
        int id = Integer.parseInt(parts[1]);
        productService.applyDiscount(id);
        logger.info("Discount applied to product ID: {}", id);
        System.out.println("Discount applied to product ID: " + id); // Keep console output
    }

    private static void handleAdjust(ProductService productService, String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException("Usage: adjust <id> <amount>");
        }
        int id = Integer.parseInt(parts[1]);
        int amount = Integer.parseInt(parts[2]);
        productService.adjustStock(id, amount);
        logger.info("Stock adjusted for product ID: {} by amount: {}", id, amount);
        System.out.println("Stock adjusted for product ID: " + id); // Keep console output
    }

    private static void handleAddCategory(ProductService productService, String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Usage: add-category <name>");
        }
        String name = parts[1].replace("\"", "");
        int id = productService.addCategory(name);
        logger.info("Category added with ID: {}, name: {}", id, name);
        System.out.println("Category added with ID: " + id); // Keep console output
    }

    private static void handleAddSupplier(ProductService productService, String[] parts) {
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Usage: add-supplier <name> [<contactInfo>]");
        }
        String name = parts[1].replace("\"", "");
        String contactInfo = parts.length == 3 ? parts[2].replace("\"", "") : null;
        int id = productService.addSupplier(name, contactInfo);
        logger.info("Supplier added with ID: {}, name: {}, contactInfo: {}", id, name, contactInfo);
        System.out.println("Supplier added with ID: " + id); // Keep console output
    }

    private static void handleHelp() {
        logger.debug("Displaying help message.");
        System.out.println("Available commands:");
        System.out.println("  add <name> <price> <stock> <expiration> <discounted> <categoryId> <supplierId>");
        System.out.println("    - Add a new product (e.g., add \"Milk\" 5.0 100 2025-04-28 false 1 1)");
        System.out.println("  list");
        System.out.println("    - List all products");
        System.out.println("  update <id> <name> <price> <stock> <expiration> <discounted> <categoryId> <supplierId>");
        System.out.println("    - Update a product (e.g., update 1 \"Milk\" 4.5 90 2025-04-28 true 1 1)");
        System.out.println("  delete <id>");
        System.out.println("    - Delete a product by ID");
        System.out.println("  search <name>");
        System.out.println("    - Search products by name (e.g., search \"Milk\")");
        System.out.println("  discount <id>");
        System.out.println("    - Apply a discount to a product by ID");
        System.out.println("  adjust <id> <amount>");
        System.out.println("    - Adjust stock for a product (e.g., adjust 1 -10)");
        System.out.println("  auto-discount");
        System.out.println("    - Clear stock for expired products");
        System.out.println("  add-category <name>");
        System.out.println("    - Add a new category (e.g., add-category \"Dairy\")");
        System.out.println("  add-supplier <name> [<contactInfo>]");
        System.out.println("    - Add a new supplier (e.g., add-supplier \"FarmFresh\" \"555-1234\")");
        System.out.println("  help");
        System.out.println("    - Show this help message");
        System.out.println("  exit");
        System.out.println("    - Exit the application");
    }
}