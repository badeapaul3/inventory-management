package com.inventory.main;

import com.inventory.dao.ProductDAO;
import com.inventory.database.DatabaseInitializer;
import com.inventory.discount.DiscountManager;
import com.inventory.discount.DiscountService;
import com.inventory.discount.FlatDiscountStrategy;
import com.inventory.discount.PercentageDiscountStrategy;
import com.inventory.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        try (ProductDAO productDAO = new ProductDAO();
             Scanner scanner = new Scanner(System.in)) {
            DiscountService discountService = new DiscountService(productDAO);
            DiscountManager discountManager = new DiscountManager(productDAO);
            System.out.println("Inventory Management CLI - Enter 'help' for commands.");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String[] parts = input.split("\\s+");
                String command = parts[0].toLowerCase();

                try {
                    switch (command) {
                        case "help":
                            printHelp();
                            break;
                        case "exit":
                            System.out.println("Exiting...");
                            return;
                        case "add":
                            handleAdd(productDAO, parts);
                            break;
                        case "list":
                            handleList(productDAO);
                            break;
                        case "update":
                            handleUpdate(productDAO, parts);
                            break;
                        case "delete":
                            handleDelete(productDAO, parts);
                            break;
                        case "search":
                            handleSearch(productDAO, parts);
                            break;
                        case "discount":
                            handleDiscount(discountService, productDAO, parts);
                            break;
                        case "adjust":
                            handleAdjust(productDAO, parts);
                            break;
                        case "auto-discount":
                            handleAutoDiscount(discountManager, productDAO ,parts);
                            break;
                        default:
                            System.out.println("Unknown command: " + command + ". Type 'help' for options.");
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  add <name> <price> <stock> <expiration>  - Add a product (e.g., add Milk 5.0 100 2025-12-31)");
        System.out.println("  list                             - List all products");
        System.out.println("  update <id> <name> <price> <stock> <expiration> - Update a product");
        System.out.println("  delete <id>                      - Delete a product by ID");
        System.out.println("  search name <name>               - Search products by name");
        System.out.println("  search expiring <date>           - Search products expiring before date (yyyy-MM-dd)");
        System.out.println("  discount <id> flat <amount>      - Apply flat discount to product");
        System.out.println("  discount <id> percent <percent>  - Apply percentage discount to product");
        System.out.println("  adjust <id> <amount>             - Adjust stock (positive to add, negative to subtract)");
        System.out.println("  auto-discount [id]               - Apply dynamic discounts (all products or specific ID)");
        System.out.println("  help                             - Show this help");
        System.out.println("  exit                             - Exit the app");
    }

    private static void handleAdd(ProductDAO productDAO, String[] parts) {
        if (parts.length != 5) {
            throw new IllegalArgumentException("Usage: add <name> <price> <stock> <expiration>");
        }
        String name = parts[1];
        double price = Double.parseDouble(parts[2]);
        int stock = Integer.parseInt(parts[3]);
        LocalDate expiration = LocalDate.parse(parts[4], DATE_FORMATTER);
        Product product = new Product(0, name, price, stock, expiration, false); // New products not discounted
        productDAO.insertOrUpdateProduct(product);
        System.out.println("Product added: " + product);
    }

    private static void handleList(ProductDAO productDAO) {
        List<Product> products = productDAO.getAllProducts(false);
        if (products.isEmpty()) {
            System.out.println("No products found.");
        } else {
            products.forEach(System.out::println);
        }
    }

    private static void handleUpdate(ProductDAO productDAO, String[] parts) {
        if (parts.length != 6) {
            throw new IllegalArgumentException("Usage: update <id> <name> <price> <stock> <expiration>");
        }
        int id = Integer.parseInt(parts[1]);
        String name = parts[2];
        double price = Double.parseDouble(parts[3]);
        int stock = Integer.parseInt(parts[4]);
        LocalDate expiration = LocalDate.parse(parts[5], DATE_FORMATTER);
        // Preserve discounted status from existing product
        Product existing = findProductById(productDAO, id);
        Product product = new Product(id, name, price, stock, expiration, existing.discounted());
        productDAO.updateProduct(product);
        System.out.println("Product updated: " + product);
    }

    private static void handleDelete(ProductDAO productDAO, String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Usage: delete <id>");
        }
        int id = Integer.parseInt(parts[1]);
        productDAO.deleteProduct(id);
        System.out.println("Product ID " + id + " deleted.");
    }

    private static void handleSearch(ProductDAO productDAO, String[] parts) {
        if (parts.length < 3) {
            throw new IllegalArgumentException("Usage: search name <name> | search expiring <date>");
        }
        String type = parts[1].toLowerCase();
        if ("name".equals(type)) {
            String name = parts[2];
            List<Product> results = productDAO.findProductsByName(name);
            if (results.isEmpty()) {
                System.out.println("No products found with name containing: " + name);
            } else {
                results.forEach(System.out::println);
            }
        } else if ("expiring".equals(type)) {
            LocalDate date = LocalDate.parse(parts[2], DATE_FORMATTER);
            List<Product> results = productDAO.findProductsExpiringBefore(date);
            if (results.isEmpty()) {
                System.out.println("No products expiring before: " + date);
            } else {
                results.forEach(System.out::println);
            }
        } else {
            throw new IllegalArgumentException("Unknown search type: " + type);
        }
    }

    private static void handleDiscount(DiscountService discountService, ProductDAO productDAO, String[] parts) {
        if (parts.length != 4) {
            throw new IllegalArgumentException("Usage: discount <id> flat <amount> | discount <id> percent <percent>");
        }
        int id = Integer.parseInt(parts[1]);
        String type = parts[2].toLowerCase();
        Product product = findProductById(productDAO, id);
        if ("flat".equals(type)) {
            BigDecimal amount = new BigDecimal(parts[3]);
            discountService.applyAndSaveDiscount(product, new FlatDiscountStrategy(amount));
            System.out.println("Flat discount of " + amount + " applied to ID " + id);
        } else if ("percent".equals(type)) {
            BigDecimal percent = new BigDecimal(parts[3]);
            discountService.applyAndSaveDiscount(product, new PercentageDiscountStrategy(percent));
            System.out.println("Percentage discount of " + percent + "% applied to ID " + id);
        } else {
            throw new IllegalArgumentException("Unknown discount type: " + type);
        }
    }

    private static void handleAdjust(ProductDAO productDAO, String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException("Usage: adjust <id> <amount>");
        }
        int id = Integer.parseInt(parts[1]);
        int amount = Integer.parseInt(parts[2]);
        productDAO.adjustStock(id, amount);
        System.out.println("Stock adjusted for ID " + id + " by " + amount);
    }

    private static void handleAutoDiscount(DiscountManager discountManager, ProductDAO productDAO, String[] parts) {
        if (parts.length == 1) {
            int productsDiscounted = discountManager.applyDynamicDiscounts();
            if(productsDiscounted > 0) System.out.println("Dynamic discounts applied to all eligible products.");
                else System.out.println("Dynamic discounts were not applied to any product.");
        } else if (parts.length == 2) {

            int id = Integer.parseInt(parts[1]);
            Product before = findProductById(productDAO, id);
            Product updatedProduct = discountManager.applyDynamicDiscount(id);

            if(!before.discounted() && updatedProduct.discounted()){
                System.out.println("Dynamic discount applied to ID " + id + ": " + updatedProduct);
            } else {
                System.out.println("No dynamic discount applied to ID " + id + ": " + updatedProduct);
            }
        } else {
            throw new IllegalArgumentException("Usage: auto-discount [id]");
        }
    }

    private static Product findProductById(ProductDAO productDAO, int id) {
        List<Product> products = productDAO.getAllProducts(false);
        return products.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No product found with ID: " + id));
    }
}