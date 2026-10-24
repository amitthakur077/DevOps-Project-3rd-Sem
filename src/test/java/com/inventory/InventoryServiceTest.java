package com.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InventoryService and Product business rules.
 * Uses JUnit 5 Jupiter to validate all CRUD, search, stock validation,
 * and edge cases independently of the console UI.
 */
class InventoryServiceTest {

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService();
        service.clear();
    }

    // =========================================================================
    // 1. ADD PRODUCT TESTS
    // =========================================================================

    @Nested
    @DisplayName("Product Creation & Addition Tests")
    class AddProductTests {

        @Test
        @DisplayName("Should successfully add a valid product to inventory")
        void testAddProductSuccess() {
            Product product = new Product("P001", "Wireless Mouse", 25.50, 50, 10);
            Product added = service.addProduct(product);

            assertNotNull(added);
            assertEquals("P001", added.getId());
            assertEquals(1, service.getTotalProductCount());

            Optional<Product> retrieved = service.getProductById("P001");
            assertTrue(retrieved.isPresent());
            assertEquals("Wireless Mouse", retrieved.get().getName());
        }

        @Test
        @DisplayName("Should reject product with duplicate ID (case-insensitive)")
        void testAddProductDuplicateIdThrowsException() {
            Product p1 = new Product("P001", "Keyboard", 45.00, 20, 5);
            service.addProduct(p1);

            Product duplicate = new Product("p001", "Duplicate Keyboard", 50.00, 10, 5);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.addProduct(duplicate);
            });

            assertTrue(ex.getMessage().contains("Duplicate Product ID"));
            assertEquals(1, service.getTotalProductCount());
        }

        @Test
        @DisplayName("Should reject adding a null product")
        void testAddProductNullThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                service.addProduct(null);
            });
        }
    }

    // =========================================================================
    // 2. PRODUCT MODEL VALIDATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Product Model Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should reject empty or null product ID")
        void testInvalidIdThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Product(null, "Item", 10.0, 5, 2));
            assertThrows(IllegalArgumentException.class, () -> new Product("   ", "Item", 10.0, 5, 2));
        }

        @Test
        @DisplayName("Should reject empty or null product name")
        void testInvalidNameThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Product("P01", null, 10.0, 5, 2));
            assertThrows(IllegalArgumentException.class, () -> new Product("P01", "  ", 10.0, 5, 2));
        }

        @Test
        @DisplayName("Should reject negative price")
        void testNegativePriceThrowsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new Product("P01", "USB Cable", -5.99, 10, 2);
            });
            assertTrue(ex.getMessage().contains("cannot be negative"));
        }

        @Test
        @DisplayName("Should reject negative initial quantity")
        void testNegativeQuantityThrowsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new Product("P01", "Monitor", 199.99, -1, 5);
            });
            assertTrue(ex.getMessage().contains("cannot be negative"));
        }

        @Test
        @DisplayName("Should reject negative low-stock threshold")
        void testNegativeThresholdThrowsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new Product("P01", "Speaker", 49.99, 10, -2);
            });
            assertTrue(ex.getMessage().contains("cannot be negative"));
        }
    }

    // =========================================================================
    // 3. SEARCH & RETRIEVAL TESTS
    // =========================================================================

    @Nested
    @DisplayName("Product Search and Retrieval Tests")
    class SearchTests {

        @BeforeEach
        void seedProducts() {
            service.addProduct(new Product("P101", "Dell Monitor 27", 299.99, 15, 5));
            service.addProduct(new Product("P102", "Dell Wireless Mouse", 29.99, 30, 10));
            service.addProduct(new Product("P103", "Sony Headphones WH-1000", 349.00, 8, 3));
        }

        @Test
        @DisplayName("Should retrieve all added products in order")
        void testGetAllProducts() {
            List<Product> all = service.getAllProducts();
            assertEquals(3, all.size());
            assertEquals("P101", all.get(0).getId());
            assertEquals("P102", all.get(1).getId());
            assertEquals("P103", all.get(2).getId());
        }

        @Test
        @DisplayName("Should find product by ID (exact match)")
        void testGetProductByIdFound() {
            Optional<Product> product = service.getProductById("P102");
            assertTrue(product.isPresent());
            assertEquals("Dell Wireless Mouse", product.get().getName());
        }

        @Test
        @DisplayName("Should return empty Optional when ID does not exist")
        void testGetProductByIdNotFound() {
            Optional<Product> product = service.getProductById("NON_EXISTENT");
            assertTrue(product.isEmpty());
        }

        @Test
        @DisplayName("Should handle null or empty ID lookup safely")
        void testGetProductByIdNullOrEmpty() {
            assertTrue(service.getProductById(null).isEmpty());
            assertTrue(service.getProductById("   ").isEmpty());
        }

        @Test
        @DisplayName("Should search products by name (case-insensitive substring)")
        void testSearchByNameCaseInsensitive() {
            List<Product> dellProducts = service.searchProductsByName("dell");
            assertEquals(2, dellProducts.size());

            List<Product> headphone = service.searchProductsByName("HEADPHONES");
            assertEquals(1, headphone.size());
            assertEquals("P103", headphone.get(0).getId());
        }

        @Test
        @DisplayName("Should return empty list when searching with empty or non-matching query")
        void testSearchNonExistentReturnsEmpty() {
            List<Product> emptyQuery = service.searchProductsByName("   ");
            assertTrue(emptyQuery.isEmpty());

            List<Product> notFound = service.searchProductsByName("Printer");
            assertTrue(notFound.isEmpty());
        }

        @Test
        @DisplayName("Unified search should find product by ID or name without duplicates")
        void testUnifiedSearch() {
            List<Product> byId = service.searchProducts("P101");
            assertEquals(1, byId.size());
            assertEquals("Dell Monitor 27", byId.get(0).getName());

            List<Product> byName = service.searchProducts("Monitor");
            assertEquals(1, byName.size());
            assertEquals("P101", byName.get(0).getId());
        }
    }

    // =========================================================================
    // 4. STOCK UPDATE TESTS
    // =========================================================================

    @Nested
    @DisplayName("Stock Update & Inventory Adjustment Tests")
    class StockUpdateTests {

        @BeforeEach
        void setupProduct() {
            service.addProduct(new Product("P201", "Webcam 1080p", 49.99, 20, 5));
        }

        @Test
        @DisplayName("Should increase stock quantity successfully")
        void testUpdateStockIncrease() {
            Product updated = service.updateStock("P201", 15);
            assertEquals(35, updated.getQuantity());

            Product inInventory = service.getProductById("P201").orElseThrow();
            assertEquals(35, inInventory.getQuantity());
        }

        @Test
        @DisplayName("Should decrease stock quantity successfully")
        void testUpdateStockDecrease() {
            Product updated = service.updateStock("P201", -8);
            assertEquals(12, updated.getQuantity());
        }

        @Test
        @DisplayName("Should allow reducing stock exactly to zero")
        void testUpdateStockReduceToZero() {
            Product updated = service.updateStock("P201", -20);
            assertEquals(0, updated.getQuantity());
            assertTrue(updated.isLowStock());
        }

        @Test
        @DisplayName("Should reject reduction that would result in negative stock")
        void testUpdateStockNegativeResultThrowsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                service.updateStock("P201", -25); // Current is 20, cannot reduce by 25
            });

            assertTrue(ex.getMessage().contains("negative stock"));

            // Verify stock was not changed
            Product unchanged = service.getProductById("P201").orElseThrow();
            assertEquals(20, unchanged.getQuantity());
        }

        @Test
        @DisplayName("Should throw exception when updating stock of non-existent product")
        void testUpdateStockNonExistentProductThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                service.updateStock("UNKNOWN_ID", 10);
            });
        }
    }

    // =========================================================================
    // 5. LOW-STOCK ALERT TESTS
    // =========================================================================

    @Nested
    @DisplayName("Low-Stock Alerts & Notification Tests")
    class LowStockTests {

        @Test
        @DisplayName("Should flag product when quantity is below or equal to threshold")
        void testLowStockAlerts() {
            // Product 1: Quantity (5) == Threshold (5) -> SHOULD trigger alert
            service.addProduct(new Product("P301", "Item Equal", 10.0, 5, 5));

            // Product 2: Quantity (2) < Threshold (5) -> SHOULD trigger alert
            service.addProduct(new Product("P302", "Item Below", 10.0, 2, 5));

            // Product 3: Quantity (10) > Threshold (5) -> Should NOT trigger alert
            service.addProduct(new Product("P303", "Item Above", 10.0, 10, 5));

            List<Product> lowStock = service.getLowStockProducts();
            assertEquals(2, lowStock.size());

            List<String> lowStockIds = lowStock.stream().map(Product::getId).toList();
            assertTrue(lowStockIds.contains("P301"));
            assertTrue(lowStockIds.contains("P302"));
            assertFalse(lowStockIds.contains("P303"));
        }
    }

    // =========================================================================
    // 6. DELETE PRODUCT TESTS
    // =========================================================================

    @Nested
    @DisplayName("Product Deletion Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should successfully delete existing product")
        void testDeleteProductSuccess() {
            service.addProduct(new Product("P401", "To Be Deleted", 15.0, 10, 2));
            assertEquals(1, service.getTotalProductCount());

            boolean deleted = service.deleteProduct("P401");
            assertTrue(deleted);
            assertEquals(0, service.getTotalProductCount());
            assertTrue(service.getProductById("P401").isEmpty());
        }

        @Test
        @DisplayName("Should return false when attempting to delete non-existent product")
        void testDeleteProductNonExistent() {
            boolean deleted = service.deleteProduct("NON_EXISTENT");
            assertFalse(deleted);
        }

        @Test
        @DisplayName("Should return false when passed null or empty ID")
        void testDeleteProductNullOrEmpty() {
            assertFalse(service.deleteProduct(null));
            assertFalse(service.deleteProduct("   "));
        }
    }

    // =========================================================================
    // 7. INVENTORY AGGREGATION & METRICS
    // =========================================================================

    @Nested
    @DisplayName("Inventory Metrics & Valuation Tests")
    class ValuationTests {

        @Test
        @DisplayName("Should correctly compute total inventory monetary value")
        void testCalculateTotalInventoryValue() {
            service.addProduct(new Product("P501", "Item A", 100.0, 2, 1)); // 200.0
            service.addProduct(new Product("P502", "Item B", 25.50, 4, 1)); // 102.0

            assertEquals(302.0, service.getTotalInventoryValue(), 0.001);
        }
    }
}
