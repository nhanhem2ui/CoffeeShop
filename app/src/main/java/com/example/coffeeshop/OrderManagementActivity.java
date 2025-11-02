package com.example.coffeeshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coffeeshop.adapters.OrderAdapter;
import com.example.coffeeshop.database.DatabaseHelper;
import com.example.coffeeshop.models.Order;
import com.example.coffeeshop.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class OrderManagementActivity extends AppCompatActivity implements OrderAdapter.OnOrderActionListener {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private DatabaseHelper databaseHelper;
    private TextView tvEmptyOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        if (!SessionManager.getInstance().isAdmin()) {
            Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProductListActivity.class));
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);

        setupToolbar();
        initViews();
        loadOrders();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pending Orders");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_orders);
        tvEmptyOrders = findViewById(R.id.tv_empty_orders);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(this, orderList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadOrders() {
        orderList.clear();
        orderList.addAll(databaseHelper.getPendingOrders());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAcceptClick(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Order")
                .setMessage("Accept order #" + order.getId() + " from " + order.getUserName() + "?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    boolean success = databaseHelper.updateOrderStatus(order.getId(), "accepted");
                    if (success) {
                        Toast.makeText(this, "Order accepted", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    } else {
                        Toast.makeText(this, "Failed to accept order", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRejectClick(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Reject Order")
                .setMessage("Reject order #" + order.getId() + " from " + order.getUserName() + "?")
                .setPositiveButton("Reject", (dialog, which) -> {
                    boolean success = databaseHelper.updateOrderStatus(order.getId(), "rejected");
                    if (success) {
                        Toast.makeText(this, "Order rejected", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    } else {
                        Toast.makeText(this, "Failed to reject order", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
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