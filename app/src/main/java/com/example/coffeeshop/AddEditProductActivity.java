package com.example.coffeeshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.coffeeshop.database.DatabaseHelper;
import com.example.coffeeshop.models.Product;
import com.example.coffeeshop.utils.SessionManager;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText etName, etDescription, etPrice, etImageUrl;
    private Button btnSave;
    private DatabaseHelper databaseHelper;
    private Product productToEdit;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        if (!SessionManager.getInstance().isAdmin()) {
            Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProductListActivity.class));
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("product")) {
            productToEdit = (Product) getIntent().getSerializableExtra("product");
            isEditMode = true;
        }

        setupToolbar();
        initViews();

        if (isEditMode) {
            populateFields();
        }

        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Product" : "Add Product");
        }
    }

    private void initViews() {
        etName = findViewById(R.id.et_product_name);
        etDescription = findViewById(R.id.et_product_description);
        etPrice = findViewById(R.id.et_product_price);
        etImageUrl = findViewById(R.id.et_product_image_url);
        btnSave = findViewById(R.id.btn_save_product);
    }

    private void populateFields() {
        if (productToEdit != null) {
            etName.setText(productToEdit.getName());
            etDescription.setText(productToEdit.getDescription());
            etPrice.setText(String.valueOf(productToEdit.getPrice()));
            etImageUrl.setText(productToEdit.getImageUrl());
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Product name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0");
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(imageUrl)) {
            imageUrl = "coffee_default"; // Default image if none provided
        }

        boolean success;
        if (isEditMode) {
            productToEdit.setName(name);
            productToEdit.setDescription(description);
            productToEdit.setPrice(price);
            productToEdit.setImageUrl(imageUrl);
            success = databaseHelper.updateProduct(productToEdit);
        } else {
            Product newProduct = new Product(name, description, price, imageUrl);
            success = databaseHelper.addProduct(newProduct);
        }

        if (success) {
            Toast.makeText(this, isEditMode ? "Product updated" : "Product added", Toast.LENGTH_SHORT).show();
            finish(); // Go back to product list
        } else {
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
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