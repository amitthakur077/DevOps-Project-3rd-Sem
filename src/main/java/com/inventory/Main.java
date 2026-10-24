package com.inventory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry point and interactive console user interface for the Inventory Management System.
 * Provides a robust, menu-driven interface with input validation and tabular formatting.
 */
public class Main {

    private final InventoryService inventoryService;
    private final Scanner scanner;

    public Main() {
        this.inventoryService = new InventoryService();
        this.scanner = new Scanner(System.in);
        seedSampleData();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    /**
     * Pre-seeds initial realistic inventory data so the application is immediately testable.
     */
    private void seedSampleData() {
        inventoryService.addProduct(new Product("P101", "Dell XPS 15 Laptop", 1499.99, 12, 5));
        inventoryService.addProduct(new Product("P102", "Logitech MX Master 3S Mouse", 99.50, 4, 10)); // Triggers low-stock alert!
        inventoryService.addProduct(new Product("P103", "Mechanical RGB Keyboard", 79.99, 25, 8));
        inventoryService.addProduct(new Product("P104", "USB-C Multiport Hub", 34.50, 6, 15)); // Triggers low-stock alert!
    }

    /**
     * Main application loop.
     */
    public void run() {
        printBanner();
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Enter your choice (1-8): ", 1, 8);
            System.out.println();

            switch (choice) {
                case 1 -> handleAddProduct();
                case 2 -> handleViewAllProducts();
                case 3 -> handleSearchProduct();
                case 4 -> handleUpdateStock();
                case 5 -> handleDeleteProduct();
                case 6 -> handleLowStockAlerts();
                case 7 -> handleInventorySummary();
                case 8 -> {
                    running = false;
                    handleExit();
                }
                default -> System.out.println("Invalid option. Please select between 1 and 8.");
            }

            if (running) {
                System.out.println();
                pause();
            }
        }
    }

    private void printBanner() {
        System.out.println("==========================================================================");
        System.out.println("                   INVENTORY MANAGEMENT SYSTEM (IMS v1.0)                 ");
        System.out.println("          Automated CI Pipeline with Git, Maven, JUnit 5 & Jenkins        ");
        System.out.println("==========================================================================");
        System.out.println("Note: 4 initial sample products pre-loaded for immediate demonstration.");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("--------------------------- MAIN MENU ------------------------------------");
        System.out.println("  [1] Add Product");
        System.out.println("  [2] View All Products");
        System.out.println("  [3] Search Product (by ID or Name)");
        System.out.println("  [4] Update Stock Quantity (Increase / Decrease)");
        System.out.println("  [5] Delete Product (with Confirmation)");
        System.out.println("  [6] View Low-Stock Alerts");
        System.out.println("  [7] View Inventory Summary & Statistics");
        System.out.println("  [8] Exit Application");
        System.out.println("--------------------------------------------------------------------------");
    }

    // =========================================================================
    // FEATURE HANDLERS
    // =========================================================================

    private void handleAddProduct() {
        System.out.println(">>> ADD NEW PRODUCT");
        String id;
        while (true) {
            id = readNonEmptyString("Enter Product ID (e.g., P105): ");
            if (inventoryService.getProductById(id).isPresent()) {
                System.out.println("Error: A product with ID '" + id + "' already exists! Please use a unique ID.");
            } else {
                break;
            }
        }

        String name = readNonEmptyString("Enter Product Name: ");
        double price = readDouble("Enter Price ($): ", 0.0);
        int quantity = readInt("Enter Initial Quantity: ", 0, Integer.MAX_VALUE);
        int threshold = readInt("Enter Low-Stock Alert Threshold: ", 0, Integer.MAX_VALUE);

        try {
            Product product = new Product(id, name, price, quantity, threshold);
            inventoryService.addProduct(product);
            System.out.println("\nSUCCESS: Product '" + product.getName() + "' (ID: " + product.getId() + ") added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("\nERROR: Failed to add product: " + e.getMessage());
        }
    }

    private void handleViewAllProducts() {
        System.out.println(">>> CURRENT PRODUCT INVENTORY");
        List<Product> products = inventoryService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Inventory is currently empty. Use Option 1 to add products.");
            return;
        }
        renderTable(products);
    }

    private void handleSearchProduct() {
        System.out.println(">>> SEARCH INVENTORY");
        String query = readNonEmptyString("Enter Product ID or Name to search: ");
        List<Product> results = inventoryService.searchProducts(query);

        if (results.isEmpty()) {
            System.out.println("\nNo products found matching: '" + query + "'");
        } else {
            System.out.println("\nFound " + results.size() + " matching product(s):");
            renderTable(results);
        }
    }

    private void handleUpdateStock() {
        System.out.println(">>> UPDATE PRODUCT STOCK");
        String id = readNonEmptyString("Enter Product ID: ");
        Optional<Product> optionalProduct = inventoryService.getProductById(id);

        if (optionalProduct.isEmpty()) {
            System.out.println("Error: Product with ID '" + id + "' does not exist.");
            return;
        }

        Product product = optionalProduct.get();
        System.out.printf("Current Product: %s | Current Stock: %d units%n", product.getName(), product.getQuantity());
        System.out.println("Choose action:");
        System.out.println("  [1] Increase Stock (Add received units)");
        System.out.println("  [2] Decrease Stock (Deduct sold/removed units)");
        int action = readInt("Select (1 or 2): ", 1, 2);

        int delta;
        if (action == 1) {
            int amount = readInt("Enter quantity to ADD: ", 1, Integer.MAX_VALUE);
            delta = amount;
        } else {
            int amount = readInt("Enter quantity to DEDUCT: ", 1, Integer.MAX_VALUE);
            delta = -amount;
        }

        try {
            inventoryService.updateStock(id, delta);
            System.out.printf("%nSUCCESS: Stock updated for '%s'. New Quantity: %d%n", product.getName(), product.getQuantity());
            if (product.isLowStock()) {
                System.out.println("WARNING: Product stock is now at or below threshold (" + product.getLowStockThreshold() + ")!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\nERROR: " + e.getMessage());
        }
    }

    private void handleDeleteProduct() {
        System.out.println(">>> DELETE PRODUCT");
        String id = readNonEmptyString("Enter Product ID to delete: ");
        Optional<Product> optionalProduct = inventoryService.getProductById(id);

        if (optionalProduct.isEmpty()) {
            System.out.println("Error: Product with ID '" + id + "' does not exist.");
            return;
        }

        Product product = optionalProduct.get();
        System.out.printf("Product Found: %s (ID: %s, Current Stock: %d)%n", product.getName(), product.getId(), product.getQuantity());
        boolean confirmed = readConfirmation("Are you sure you want to delete this product? (yes/no): ");

        if (confirmed) {
            boolean deleted = inventoryService.deleteProduct(id);
            if (deleted) {
                System.out.println("\nSUCCESS: Product '" + product.getName() + "' (ID: " + id + ") has been deleted.");
            } else {
                System.out.println("\nERROR: Could not delete product.");
            }
        } else {
            System.out.println("\nDeletion cancelled by user.");
        }
    }

    private void handleLowStockAlerts() {
        System.out.println(">>> LOW-STOCK ALERTS");
        List<Product> lowStock = inventoryService.getLowStockProducts();

        if (lowStock.isEmpty()) {
            System.out.println("All products have sufficient stock levels! No low-stock alerts at this time.");
        } else {
            System.out.println("ALERT: " + lowStock.size() + " product(s) have reached or fallen below their low-stock threshold!");
            renderTable(lowStock);
        }
    }

    private void handleInventorySummary() {
        System.out.println(">>> INVENTORY SUMMARY & STATISTICS");
        int totalProducts = inventoryService.getTotalProductCount();
        double totalVal = inventoryService.getTotalInventoryValue();
        List<Product> lowStock = inventoryService.getLowStockProducts();

        System.out.println("+---------------------------------------------------------------+");
        System.out.printf("| %-35s | %23s |%n", "Metric", "Value");
        System.out.println("+---------------------------------------------------------------+");
        System.out.printf("| %-35s | %23d |%n", "Total Product Types Registered", totalProducts);
        System.out.printf("| %-35s | %23.2f |%n", "Total Valuation ($ USD)", totalVal);
        System.out.printf("| %-35s | %23d |%n", "Products in Low-Stock State", lowStock.size());
        System.out.println("+---------------------------------------------------------------+");
    }

    private void handleExit() {
        System.out.println("==========================================================================");
        System.out.println("       Thank you for using the Inventory Management System. Goodbye!      ");
        System.out.println("==========================================================================");
    }

    // =========================================================================
    // UI & FORMATTING HELPERS
    // =========================================================================

    private void renderTable(List<Product> products) {
        String border = "+--------+--------------------------------+------------+----------+-----------+--------------+";
        System.out.println(border);
        System.out.printf("| %-6s | %-30s | %-10s | %-8s | %-9s | %-12s |%n",
                "ID", "Name", "Price ($)", "Quantity", "Threshold", "Stock Status");
        System.out.println(border);

        for (Product p : products) {
            String status = p.isLowStock() ? "LOW STOCK (!)" : "OK";
            System.out.printf("| %-6s | %-30s | %10.2f | %8d | %9d | %-12s |%n",
                    p.getId(),
                    truncate(p.getName(), 30),
                    p.getPrice(),
                    p.getQuantity(),
                    p.getLowStockThreshold(),
                    status);
        }
        System.out.println(border);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }

    // =========================================================================
    // INPUT VALIDATION HELPERS
    // =========================================================================

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("Input cannot be empty. Please enter a valid value.");
        }
    }

    private double readDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val < min) {
                    System.out.printf("Value cannot be less than %.2f. Try again.%n", min);
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter a valid numeric value.");
            }
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val < min || val > max) {
                    System.out.printf("Please enter a number between %d and %d.%n", min, max);
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer format. Please enter a valid whole number.");
            }
        }
    }

    private boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) {
                return true;
            }
            if (input.equals("no") || input.equals("n")) {
                return false;
            }
            System.out.println("Please type 'yes' (or 'y') or 'no' (or 'n').");
        }
    }

    private void pause() {
        System.out.print("Press [ENTER] to return to the menu...");
        scanner.nextLine();
    }
}
