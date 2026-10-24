package com.inventory;

import java.util.Objects;

/**
 * Model class representing a product in the Inventory Management System.
 * Encapsulates product details and business validation logic.
 */
public class Product {

    private final String id;
    private String name;
    private double price;
    private int quantity;
    private int lowStockThreshold;

    /**
     * Constructs a new Product with full validation of all attributes.
     *
     * @param id                Unique product identifier (non-empty)
     * @param name              Product name (non-empty)
     * @param price             Price per unit (must be >= 0.0)
     * @param quantity          Available stock quantity (must be >= 0)
     * @param lowStockThreshold Minimum stock threshold before alert (must be >= 0)
     * @throws IllegalArgumentException if any validation condition fails
     */
    public Product(String id, String name, double price, int quantity, int lowStockThreshold) {
        validateId(id);
        validateName(name);
        validatePrice(price);
        validateQuantity(quantity);
        validateLowStockThreshold(lowStockThreshold);

        this.id = id.trim();
        this.name = name.trim();
        this.price = price;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    private static void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty.");
        }
    }

    private static void validatePrice(double price) {
        if (Double.isNaN(price) || Double.isInfinite(price) || price < 0.0) {
            throw new IllegalArgumentException(
                String.format("Product price cannot be negative or invalid. Provided: %.2f", price)
            );
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative. Provided: " + quantity);
        }
    }

    private static void validateLowStockThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Low-stock threshold cannot be negative. Provided: " + threshold);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        validatePrice(price);
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        validateLowStockThreshold(lowStockThreshold);
        this.lowStockThreshold = lowStockThreshold;
    }

    /**
     * Checks if product quantity has reached or fallen below low-stock threshold.
     *
     * @return true if quantity <= lowStockThreshold, false otherwise
     */
    public boolean isLowStock() {
        return this.quantity <= this.lowStockThreshold;
    }

    /**
     * Calculates the total inventory value of this product (quantity * price).
     *
     * @return total monetary value of current stock
     */
    public double getTotalValue() {
        return this.price * this.quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equalsIgnoreCase(product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format(
            "Product[id='%s', name='%s', price=%.2f, qty=%d, threshold=%d, lowStock=%s]",
            id, name, price, quantity, lowStockThreshold, isLowStock() ? "YES" : "NO"
        );
    }
}
