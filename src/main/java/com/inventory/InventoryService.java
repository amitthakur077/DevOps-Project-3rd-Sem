package com.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class handling all inventory operations in memory.
 * Provides validation, query capabilities, stock updates, and low-stock alerting.
 */
public class InventoryService {

    // Preserves product addition order using LinkedHashMap
    private final Map<String, Product> inventory = new LinkedHashMap<>();

    /**
     * Adds a new product to the inventory.
     *
     * @param product Product instance to add
     * @return the added Product
     * @throws IllegalArgumentException if product is null or if product ID already exists
     */
    public Product addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot add null product to inventory.");
        }

        String normalizedId = normalizeKey(product.getId());

        if (inventory.containsKey(normalizedId)) {
            throw new IllegalArgumentException(
                String.format("Duplicate Product ID: A product with ID '%s' already exists in inventory.", product.getId())
            );
        }

        inventory.put(normalizedId, product);
        return product;
    }

    /**
     * Retrieves all products currently stored in the inventory.
     *
     * @return an unmodifiable list of all products
     */
    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(new ArrayList<>(inventory.values()));
    }

    /**
     * Finds a product by its unique product ID.
     *
     * @param id Product ID to look up
     * @return Optional containing the product if found, or Optional.empty()
     */
    public Optional<Product> getProductById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(inventory.get(normalizeKey(id)));
    }

    /**
     * Searches for products whose names contain the given query (case-insensitive).
     *
     * @param nameQuery Substring to search within product names
     * @return List of matching products
     */
    public List<Product> searchProductsByName(String nameQuery) {
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String query = nameQuery.trim().toLowerCase();
        List<Product> matches = new ArrayList<>();

        for (Product product : inventory.values()) {
            if (product.getName().toLowerCase().contains(query)) {
                matches.add(product);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Unified search: searches by product ID first (exact match),
     * and also searches by name substring.
     *
     * @param query Search query for ID or name
     * @return List of matching products without duplicates
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Product> results = new ArrayList<>();

        // Check if query exactly matches an ID
        Optional<Product> byId = getProductById(query);
        byId.ifPresent(results::add);

        // Add any name matches that are not already in results
        List<Product> byName = searchProductsByName(query);
        for (Product p : byName) {
            if (!results.contains(p)) {
                results.add(p);
            }
        }

        return Collections.unmodifiableList(results);
    }

    /**
     * Updates the stock level of an existing product.
     * A positive change increases stock; a negative change decreases stock.
     *
     * @param id             Product ID
     * @param quantityChange Delta to apply to current quantity (can be positive or negative)
     * @return Updated Product instance
     * @throws IllegalArgumentException if product not found or if reduction results in negative stock
     */
    public Product updateStock(String id, int quantityChange) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        }

        String normalizedId = normalizeKey(id);
        Product product = inventory.get(normalizedId);

        if (product == null) {
            throw new IllegalArgumentException(
                String.format("Product with ID '%s' was not found in inventory.", id.trim())
            );
        }

        int currentQuantity = product.getQuantity();
        int newQuantity = currentQuantity + quantityChange;

        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid stock reduction: Cannot reduce stock by %d. Current quantity is %d, which would result in negative stock (%d).",
                    Math.abs(quantityChange), currentQuantity, newQuantity
                )
            );
        }

        product.setQuantity(newQuantity);
        return product;
    }

    /**
     * Deletes a product from the inventory by its ID.
     *
     * @param id Product ID to delete
     * @return true if product existed and was deleted, false otherwise
     */
    public boolean deleteProduct(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return inventory.remove(normalizeKey(id)) != null;
    }

    /**
     * Retrieves all products whose quantities are at or below their low-stock thresholds.
     *
     * @return List of products requiring restocking
     */
    public List<Product> getLowStockProducts() {
        List<Product> lowStock = new ArrayList<>();
        for (Product product : inventory.values()) {
            if (product.isLowStock()) {
                lowStock.add(product);
            }
        }
        return Collections.unmodifiableList(lowStock);
    }

    /**
     * Returns total number of distinct products currently stored.
     *
     * @return count of products
     */
    public int getTotalProductCount() {
        return inventory.size();
    }

    /**
     * Calculates the total inventory value across all stored products.
     *
     * @return total monetary value of entire inventory
     */
    public double getTotalInventoryValue() {
        double total = 0.0;
        for (Product product : inventory.values()) {
            total += product.getTotalValue();
        }
        return total;
    }

    /**
     * Clears all products from inventory (used for resets/testing).
     */
    public void clear() {
        inventory.clear();
    }

    /**
     * Normalizes a product ID key for case-insensitive and whitespace-tolerant lookups.
     */
    private String normalizeKey(String id) {
        return id.trim().toUpperCase();
    }
}
