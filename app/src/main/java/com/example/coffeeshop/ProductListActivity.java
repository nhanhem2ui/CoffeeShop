package com.example.coffeeshop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coffeeshop.adapters.ProductAdapter;
import com.example.coffeeshop.database.DatabaseHelper;
import com.example.coffeeshop.models.Product;
import com.example.coffeeshop.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private List<Product> filteredList;
    private DatabaseHelper databaseHelper;
    private EditText etSearch;
    private FloatingActionButton fabAddProduct;
    private boolean isAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        databaseHelper = new DatabaseHelper(this);

        setupToolbar();
        initViews();
        loadProducts();
        setupSearch();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Coffee Shop");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_products);
        etSearch = findViewById(R.id.et_search);
        fabAddProduct = findViewById(R.id.fab_add_product);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ProductAdapter(this, filteredList, this);
        recyclerView.setAdapter(adapter);

        fabAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(ProductListActivity.this, AddEditProductActivity.class));
        });
    }

    private void loadProducts() {
        productList = databaseHelper.getAllProducts();
        filteredList.clear();
        filteredList.addAll(productList);
        adapter.updateList(filteredList);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        adapter.updateList(filteredList);
    }

    private void sortProducts() {
        Collections.sort(filteredList, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                if (isAscending) {
                    return Double.compare(p1.getPrice(), p2.getPrice());
                } else {
                    return Double.compare(p2.getPrice(), p1.getPrice());
                }
            }
        });
        isAscending = !isAscending;
        adapter.updateList(filteredList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            sortProducts();
            return true;
        } else if (id == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        } else if (id == R.id.action_revenue) {
            startActivity(new Intent(this, RevenueActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SessionManager.getInstance().logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = databaseHelper.deleteProduct(product.getId());
                        if (success) {
                            Toast.makeText(ProductListActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                            loadProducts();
                        } else {
                            Toast.makeText(ProductListActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAddToCartClick(Product product) {
        int userId = SessionManager.getInstance().getUserId();
        boolean success = databaseHelper.addToCart(userId, product.getId(), 1);
        if (success) {
            Toast.makeText(this, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}