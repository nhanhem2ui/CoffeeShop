package com.example.coffeeshop;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.coffeeshop.utils.SessionManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 1s

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showLanguageDialog();
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Tiếng Việt"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");
        builder.setSingleChoiceItems(languages, -1, (dialog, which) -> {
            if (which == 0) {
                setLocale("en");
            } else {
                setLocale("vi");
            }
            dialog.dismiss();
            proceedToNextActivity();
        });
        builder.create().show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void proceedToNextActivity() {
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