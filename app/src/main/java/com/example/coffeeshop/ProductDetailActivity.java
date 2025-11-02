package com.example.coffeeshop;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.coffeeshop.database.DatabaseHelper;
import com.example.coffeeshop.models.Product;
import com.example.coffeeshop.utils.SessionManager;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvName, tvDescription, tvPrice;
    private Button btnAddToCart;
    private Product product;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("product")) {
            product = (Product) getIntent().getSerializableExtra("product");
        }

        setupToolbar();
        initViews();
        displayProductDetails();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Details");
        }
    }

    private void initViews() {
        ivProduct = findViewById(R.id.iv_product_detail);
        tvName = findViewById(R.id.tv_product_name_detail);
        tvDescription = findViewById(R.id.tv_product_description_detail);
        tvPrice = findViewById(R.id.tv_product_price_detail);
        btnAddToCart = findViewById(R.id.btn_add_to_cart_detail);

        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });
    }

    private void displayProductDetails() {
        if (product != null) {
            tvName.setText(product.getName());
            tvDescription.setText(product.getDescription());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

            // Load image - check if it's a URL or drawable resource
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                // Load from URL (Cloudinary)
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.coffee_default)
                        .error(R.drawable.coffee_default)
                        .centerCrop()
                        .into(ivProduct);
            } else {
                // Load from drawable resources
                int imageResource = getImageResource(imageUrl);
                ivProduct.setImageResource(imageResource);
            }
        }
    }

    private void addToCart() {
        int userId = SessionManager.getInstance().getUserId();
        boolean success = databaseHelper.addToCart(userId, product.getId(), 1);
        if (success) {
            Toast.makeText(this, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private int getImageResource(String imageName) {
        switch (imageName != null ? imageName.toLowerCase() : "") {
            case "espresso":
                return R.drawable.espresso;
            case "cappuccino":
                return R.drawable.cappuccino;
            case "latte":
                return R.drawable.latte;
            case "americano":
                return R.drawable.americano;
            case "mocha":
                return R.drawable.mocha;
            case "macchiato":
                return R.drawable.macchiato;
            case "coldbrew":
                return R.drawable.coldbrew;
            case "flatwhite":
                return R.drawable.flatwhite;
            default:
                return R.drawable.coffee_default;
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