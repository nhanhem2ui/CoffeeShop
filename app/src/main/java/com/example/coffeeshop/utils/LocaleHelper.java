package com.example.coffeeshop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {
    private static final String PREFS_NAME = "CoffeeShopPrefs";
    private static final String KEY_LANGUAGE = "language";

    /**
     * Apply saved language to the context
     */
    public static void applyLanguage(Context context) {
        String language = getSavedLanguage(context);
        setLocale(context, language);
    }

    /**
     * Get saved language from preferences
     */
    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    /**
     * Save language preference
     */
    public static void saveLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    /**
     * Set locale for the context
     */
    public static void setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}