package com.example.coffeeshop.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHandler {

    public static final int PERMISSION_REQUEST_CODE = 100;

    /**
     * Check if we have the necessary image permissions based on Android version
     */
    public static boolean hasImagePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            // Check for partial access (selected photos)
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) { // Android 13
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
        else { // Android 12 and below
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Request image permissions based on Android version
     */
    public static void requestImagePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+

            // Request both full and partial access
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    },
                    PERMISSION_REQUEST_CODE);
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) { // Android 13
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_CODE);
        }
        else { // Android 12 and below
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Check if permission request should show rationale
     */
    public static boolean shouldShowRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_MEDIA_IMAGES) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_MEDIA_IMAGES);
        }
        else {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Get user-friendly permission explanation
     */
    public static String getPermissionExplanation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return "Please allow access to photos so you can upload product images. " +
                    "You can choose to allow access to all photos or select specific photos.";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            return "Please allow access to photos so you can upload product images.";
        }
        else {
            return "Please allow storage access so you can upload product images.";
        }
    }
}