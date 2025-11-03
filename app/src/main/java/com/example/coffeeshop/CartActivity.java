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
import com.example.coffeeshop.utils.LocaleHelper;
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
    private Button btnPlaceOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLanguage(this);
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
            getSupportActionBar().setTitle(R.string.shopping_cart);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_cart);
        tvTotal = findViewById(R.id.tv_total_amount);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItems = new ArrayList<>();
        adapter = new CartAdapter(this, cartItems, this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadCartItems() {
        int userId = SessionManager.getInstance().getUserId();
        List<CartItem> newItems = databaseHelper.getCartItems(userId);

        cartItems.clear();
        cartItems.addAll(newItems);

        adapter.notifyDataSetChanged();

        updateTotal();
        updateEmptyState();
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.getDefault(), getString(R.string.total), total));
    }

    private void updateEmptyState() {
        if (cartItems.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(false);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(true);
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        boolean success = databaseHelper.updateCartItemQuantity(item.getCartId(), newQuantity);
        if (success) {
            loadCartItems();
        } else {
            Toast.makeText(this, R.string.failed_to_update, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRemoveClick(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_item_title)
                .setMessage(String.format(getString(R.string.remove_item_message), item.getProduct().getName()))
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    boolean success = databaseHelper.removeFromCart(item.getCartId());
                    if (success) {
                        loadCartItems();
                        Toast.makeText(CartActivity.this, R.string.item_removed, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CartActivity.this, R.string.failed_to_remove, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void placeOrder() {
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getSubtotal();
        }
        final double total = totalAmount;

        new AlertDialog.Builder(this)
                .setTitle(R.string.place_order_title)
                .setMessage(String.format(Locale.getDefault(), getString(R.string.place_order_message), total))
                .setPositiveButton(R.string.place_order, (dialog, which) -> {
                    int userId = SessionManager.getInstance().getUserId();

                    boolean orderCreated = databaseHelper.createOrder(userId, total, "pending");
                    boolean cartCleared = databaseHelper.clearCart(userId);

                    if (orderCreated && cartCleared) {
                        Toast.makeText(this, R.string.order_placed, Toast.LENGTH_LONG).show();
                        loadCartItems();
                    } else {
                        Toast.makeText(this, R.string.failed_to_place_order, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
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