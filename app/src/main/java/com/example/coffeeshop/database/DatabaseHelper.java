package com.example.coffeeshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.coffeeshop.models.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CoffeeShop.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_NAME = "full_name";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";

    // Products Table
    private static final String TABLE_PRODUCTS = "products";
    private static final String COL_PRODUCT_ID = "id";
    private static final String COL_PRODUCT_NAME = "name";
    private static final String COL_PRODUCT_DESC = "description";
    private static final String COL_PRODUCT_PRICE = "price";
    private static final String COL_PRODUCT_IMAGE = "image_url";

    // Cart Table
    private static final String TABLE_CART = "cart";
    private static final String COL_CART_ID = "id";
    private static final String COL_CART_USER_ID = "user_id";
    private static final String COL_CART_PRODUCT_ID = "product_id";
    private static final String COL_CART_QUANTITY = "quantity";

    // Orders Table
    private static final String TABLE_ORDERS = "orders";
    private static final String COL_ORDER_ID = "id";
    private static final String COL_ORDER_USER_ID = "user_id";
    private static final String COL_ORDER_TOTAL = "total_amount";
    private static final String COL_ORDER_DATE = "order_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT NOT NULL, " +
                COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_USER_PASSWORD + " TEXT NOT NULL)";

        // Create Products Table
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUCT_NAME + " TEXT NOT NULL, " +
                COL_PRODUCT_DESC + " TEXT, " +
                COL_PRODUCT_PRICE + " REAL NOT NULL, " +
                COL_PRODUCT_IMAGE + " TEXT)";

        // Create Cart Table
        String createCartTable = "CREATE TABLE " + TABLE_CART + " (" +
                COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CART_USER_ID + " INTEGER NOT NULL, " +
                COL_CART_PRODUCT_ID + " INTEGER NOT NULL, " +
                COL_CART_QUANTITY + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_CART_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "), " +
                "FOREIGN KEY(" + COL_CART_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_PRODUCT_ID + "))";

        // Create Orders Table
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_USER_ID + " INTEGER NOT NULL, " +
                COL_ORDER_TOTAL + " REAL NOT NULL, " +
                COL_ORDER_DATE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_ORDER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";

        db.execSQL(createUsersTable);
        db.execSQL(createProductsTable);
        db.execSQL(createCartTable);
        db.execSQL(createOrdersTable);

        insertSampleProducts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void insertSampleProducts(SQLiteDatabase db) {
        String[][] products = {
                {"Espresso", "Strong, rich coffee shot", "2.99", "espresso"},
                {"Cappuccino", "Espresso with steamed milk foam", "4.49", "cappuccino"},
                {"Latte", "Smooth espresso with steamed milk", "4.99", "latte"},
                {"Americano", "Espresso diluted with hot water", "3.49", "americano"},
                {"Mocha", "Chocolate-flavored coffee drink", "5.49", "mocha"},
                {"Macchiato", "Espresso with a dollop of foam", "3.99", "macchiato"},
                {"Cold Brew", "Smooth, cold-steeped coffee", "4.99", "coldbrew"},
                {"Flat White", "Espresso with microfoam milk", "4.79", "flatwhite"}
        };

        for (String[] product : products) {
            ContentValues values = new ContentValues();
            values.put(COL_PRODUCT_NAME, product[0]);
            values.put(COL_PRODUCT_DESC, product[1]);
            values.put(COL_PRODUCT_PRICE, Double.parseDouble(product[2]));
            values.put(COL_PRODUCT_IMAGE, product[3]);
            db.insert(TABLE_PRODUCTS, null, values);
        }
    }

    // ========== USER OPERATIONS ==========

    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + " = ? AND " + COL_USER_PASSWORD + " = ?",
                new String[]{email, password}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD))
            );
        }
        cursor.close();
        return user;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USER_EMAIL + " = ?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ========== PRODUCT OPERATIONS ==========

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            products.add(new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE))
            ));
        }
        cursor.close();
        return products;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);

        Product product = null;
        if (cursor.moveToFirst()) {
            product = new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE))
            );
        }
        cursor.close();
        return product;
    }

    public boolean addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME, product.getName());
        values.put(COL_PRODUCT_DESC, product.getDescription());
        values.put(COL_PRODUCT_PRICE, product.getPrice());
        values.put(COL_PRODUCT_IMAGE, product.getImageUrl());

        long result = db.insert(TABLE_PRODUCTS, null, values);
        return result != -1;
    }

    public boolean updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME, product.getName());
        values.put(COL_PRODUCT_DESC, product.getDescription());
        values.put(COL_PRODUCT_PRICE, product.getPrice());
        values.put(COL_PRODUCT_IMAGE, product.getImageUrl());

        int result = db.update(TABLE_PRODUCTS, values,
                COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(product.getId())});
        return result > 0;
    }

    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PRODUCTS,
                COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
        return result > 0;
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                COL_PRODUCT_NAME + " LIKE ?", new String[]{"%" + query + "%"},
                null, null, null);

        while (cursor.moveToNext()) {
            products.add(new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE))
            ));
        }
        cursor.close();
        return products;
    }

    // ========== CART OPERATIONS ==========

    public boolean addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_CART,
                new String[]{COL_CART_ID, COL_CART_QUANTITY},
                COL_CART_USER_ID + " = ? AND " + COL_CART_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null);

        long result;
        if (cursor.moveToFirst()) {
            int cartId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_ID));
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_QUANTITY));

            ContentValues values = new ContentValues();
            values.put(COL_CART_QUANTITY, currentQty + quantity);
            result = db.update(TABLE_CART, values,
                    COL_CART_ID + " = ?", new String[]{String.valueOf(cartId)});
        } else {
            ContentValues values = new ContentValues();
            values.put(COL_CART_USER_ID, userId);
            values.put(COL_CART_PRODUCT_ID, productId);
            values.put(COL_CART_QUANTITY, quantity);
            result = db.insert(TABLE_CART, null, values);
        }
        cursor.close();
        return result != -1;
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c." + COL_CART_ID + ", c." + COL_CART_QUANTITY + ", " +
                "p." + COL_PRODUCT_ID + ", p." + COL_PRODUCT_NAME + ", " +
                "p." + COL_PRODUCT_DESC + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_IMAGE +
                " FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_PRODUCTS + " p ON c." + COL_CART_PRODUCT_ID + " = p." + COL_PRODUCT_ID +
                " WHERE c." + COL_CART_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            Product product = new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE))
            );
            cartItems.add(new CartItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_ID)),
                    product,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_QUANTITY))
            ));
        }
        cursor.close();
        return cartItems;
    }

    public boolean updateCartItemQuantity(int cartId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CART_QUANTITY, quantity);

        int result = db.update(TABLE_CART, values,
                COL_CART_ID + " = ?", new String[]{String.valueOf(cartId)});
        return result > 0;
    }

    public boolean removeFromCart(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_CART,
                COL_CART_ID + " = ?", new String[]{String.valueOf(cartId)});
        return result > 0;
    }

    public boolean clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_CART,
                COL_CART_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return result >= 0;
    }

    // ========== ORDER OPERATIONS ==========

    public boolean createOrder(int userId, double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        ContentValues values = new ContentValues();
        values.put(COL_ORDER_USER_ID, userId);
        values.put(COL_ORDER_TOTAL, totalAmount);
        values.put(COL_ORDER_DATE, currentDate);

        long result = db.insert(TABLE_ORDERS, null, values);
        return result != -1;
    }

    public double getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_ORDER_TOTAL + ") FROM " + TABLE_ORDERS, null);
        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getRevenueByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_ORDER_TOTAL + ") FROM " + TABLE_ORDERS +
                        " WHERE DATE(" + COL_ORDER_DATE + ") = ?", new String[]{date});
        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getRevenueByMonth(int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format(Locale.getDefault(), "%04d-%02d", year, month);
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_ORDER_TOTAL + ") FROM " + TABLE_ORDERS +
                        " WHERE strftime('%Y-%m', " + COL_ORDER_DATE + ") = ?", new String[]{monthStr});
        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getRevenueByYear(int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_ORDER_TOTAL + ") FROM " + TABLE_ORDERS +
                        " WHERE strftime('%Y', " + COL_ORDER_DATE + ") = ?",
                new String[]{String.valueOf(year)});
        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
}