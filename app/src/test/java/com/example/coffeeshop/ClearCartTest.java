
package com.example.coffeeshop;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.coffeeshop.database.DatabaseHelper;

@RunWith(RobolectricTestRunner.class)
public class ClearCartTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setup() {
        dbHelper = new DatabaseHelper(RuntimeEnvironment.application);
        // Add a user and some products to the database for testing
        dbHelper.registerUser("test user", "test@example.com", "password");
        dbHelper.addProduct(new com.example.coffeeshop.models.Product("Coffee", "description", 10.0, "image_url"));
    }

    @Test
    public void clearCart_returnsFalseIfCartIsEmpty() {
        // Given an empty cart
        int userId = 1;

        // When clearing the cart
        boolean result = dbHelper.clearCart(userId);

        // Then the result should be false
        assertFalse(result);
    }

    @Test
    public void clearCart_returnsTrueIfCartIsNotEmpty() {
        // Given a cart with items
        int userId = 1;
        int productId = 1;
        dbHelper.addToCart(userId, productId, 1);

        // When clearing the cart
        boolean result = dbHelper.clearCart(userId);

        // Then the result should be true
        assertTrue(result);
    }
}
