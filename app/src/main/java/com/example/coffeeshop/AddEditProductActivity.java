package com.example.coffeeshop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.coffeeshop.database.DatabaseHelper;
import com.example.coffeeshop.models.Product;
import com.example.coffeeshop.utils.CloudinaryHelper;
import com.example.coffeeshop.utils.PermissionHandler;
import com.example.coffeeshop.utils.SessionManager;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText etName, etDescription, etPrice;
    private ImageView ivProductPreview;
    private Button btnSave, btnSelectImage;
    private ProgressBar progressBar;
    private DatabaseHelper databaseHelper;
    private Product productToEdit;
    private boolean isEditMode = false;
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

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

        // Initialize Cloudinary
        CloudinaryHelper.getInstance().init(this);

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("product")) {
            productToEdit = (Product) getIntent().getSerializableExtra("product");
            isEditMode = true;
        }

        setupToolbar();
        initViews();
        setupImagePicker();

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
        ivProductPreview = findViewById(R.id.iv_product_preview);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSave = findViewById(R.id.btn_save_product);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Show preview
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .centerCrop()
                                    .into(ivProductPreview);
                            ivProductPreview.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    private void populateFields() {
        if (productToEdit != null) {
            etName.setText(productToEdit.getName());
            etDescription.setText(productToEdit.getDescription());
            etPrice.setText(String.valueOf(productToEdit.getPrice()));

            // Load existing image
            if (productToEdit.getImageUrl() != null && !productToEdit.getImageUrl().isEmpty()) {
                uploadedImageUrl = productToEdit.getImageUrl();
                if (uploadedImageUrl.startsWith("http")) {
                    // It's a URL (from Cloudinary)
                    Glide.with(this)
                            .load(uploadedImageUrl)
                            .centerCrop()
                            .into(ivProductPreview);
                    ivProductPreview.setVisibility(View.VISIBLE);
                } else {
                    // It's a drawable resource name
                    int imageResource = getImageResource(productToEdit.getImageUrl());
                    ivProductPreview.setImageResource(imageResource);
                    ivProductPreview.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> checkPermissionAndOpenGallery());
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void checkPermissionAndOpenGallery() {
        if (PermissionHandler.hasImagePermission(this)) {
            openImagePicker();
        } else {
            if (PermissionHandler.shouldShowRationale(this)) {
                showPermissionRationale();
            } else {
                PermissionHandler.requestImagePermission(this);
            }
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(PermissionHandler.getPermissionExplanation())
                .setPositiveButton("Grant Permission", (dialog, which) -> {
                    PermissionHandler.requestImagePermission(this);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHandler.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                // Check if user selected "Don't ask again"
                if (!PermissionHandler.shouldShowRationale(this)) {
                    showSettingsDialog();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot select images.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Photo access permission is required to upload product images. " +
                        "Please enable it in app settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

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

        // If new image is selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSave(name, description, price);
        } else {
            // No new image, save with existing image URL or default
            String imageUrl = isEditMode && uploadedImageUrl != null ? uploadedImageUrl : "coffee_default";
            saveToDatabase(name, description, price, imageUrl);
        }
    }

    private void uploadImageAndSave(String name, String description, double price) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        CloudinaryHelper.getInstance().uploadImage(
                selectedImageUri,
                "coffee_shop/products",
                new CloudinaryHelper.UploadListener() {
                    @Override
                    public void onUploadSuccess(String imageUrl) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            saveToDatabase(name, description, price, imageUrl);
                        });
                    }

                    @Override
                    public void onUploadFailure(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(AddEditProductActivity.this,
                                    "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private void saveToDatabase(String name, String description, double price, String imageUrl) {
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
            Toast.makeText(this, isEditMode ? "Product updated" : "Product added",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
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