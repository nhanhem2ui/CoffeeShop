package com.example.coffeeshop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.coffeeshop.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 1s

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user is already logged in
        new Handler().postDelayed(() -> {
            // Check session
            if (SessionManager.getInstance(MainActivity.this).isLoggedIn()) {
                // User is logged in, go to product list
                startActivity(new Intent(MainActivity.this, ProductListActivity.class));
            } else {
                // User is not logged in, go to login
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}