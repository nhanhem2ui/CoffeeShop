package com.example.coffeeshop;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String PREFS_NAME = "CoffeeShopPrefs";
    private static final String KEY_LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load saved language before calling super.onCreate()
        loadSavedLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if language is already set
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedLanguage = prefs.getString(KEY_LANGUAGE, null);

        if (savedLanguage == null) {
            // First time - show language dialog
            showLanguageDialog();
        } else {
            // Language already set - proceed
            proceedToNextActivity();
        }
    }

    private void loadSavedLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "en");
        setLocale(language, false);
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Tiếng Việt"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");
        builder.setCancelable(false); // Prevent dismissing without selection
        builder.setSingleChoiceItems(languages, -1, (dialog, which) -> {
            if (which == 0) {
                setLocale("en", true);
            } else {
                setLocale("vi", true);
            }
            dialog.dismiss();

            // Recreate activity to apply language change
            recreate();
            proceedToNextActivity();
        });
        builder.create().show();
    }

    private void setLocale(String lang, boolean savePreference) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        // Update configuration
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Also update application context configuration
        getApplicationContext().getResources().updateConfiguration(config,
                getApplicationContext().getResources().getDisplayMetrics());

        // Save language preference
        if (savePreference) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LANGUAGE, lang);
            editor.apply();
        }
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