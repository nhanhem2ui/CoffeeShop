package com.example.coffeeshop;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coffeeshop.adapters.CartAdapter;
import com.example.coffeeshop.database.DatabaseHelper;
import com.example.coffeeshop.models.CartItem;
import com.example.coffeeshop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private DatabaseHelper databaseHelper;
    private TextView tvTotal, tvEmptyCart;
    private Button btnCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        databaseHelper = new DatabaseHelper(this);

        setupToolbar();
        initViews();
        loadCartItems();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Shopping Cart");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_cart);
        tvTotal = findViewById(R.id.tv_total_amount);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        btnCheckout = findViewById(R.id.btn_checkout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItems = new ArrayList<>();
        adapter = new CartAdapter(this, cartItems, this);
        //caches item views by ID
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        btnCheckout.setOnClickListener(v -> checkout());
    }

    private void loadCartItems() {
        int userId = SessionManager.getInstance().getUserId();
        cartItems.clear();
        cartItems.addAll(databaseHelper.getCartItems(userId));
        adapter.notifyDataSetChanged();
        updateTotal();
        updateEmptyState();
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

    private void updateEmptyState() {
        if (cartItems.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnCheckout.setEnabled(false);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(true);
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        // Update database first
        boolean success = databaseHelper.updateCartItemQuantity(item.getCartId(), newQuantity);
        if (success) {
            // Reload the entire cart to ensure data consistency
            loadCartItems();
        } else {
            Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRemoveClick(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item")
                .setMessage("Remove " + item.getProduct().getName() + " from cart?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    boolean success = databaseHelper.removeFromCart(item.getCartId());
                    if (success) {
                        // Reload the entire cart to ensure data consistency
                        loadCartItems();
                        Toast.makeText(CartActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CartActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkout() {
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getSubtotal();
        }
        final double total = totalAmount; // Make it final for lambda

        new AlertDialog.Builder(this)
                .setTitle("Checkout")
                .setMessage(String.format(Locale.getDefault(), "Complete order for $%.2f?", total))
                .setPositiveButton("Confirm", (dialog, which) -> {
                    int userId = SessionManager.getInstance().getUserId();
                    boolean orderCreated = databaseHelper.createOrder(userId, total);
                    boolean cartCleared = databaseHelper.clearCart(userId);

                    if (orderCreated && cartCleared) {
                        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                        loadCartItems(); // Reload to show empty cart
                    } else {
                        Toast.makeText(this, "Failed to complete order", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart items when returning to this activity
        loadCartItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}