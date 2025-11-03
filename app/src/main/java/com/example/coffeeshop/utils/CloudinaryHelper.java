package com.example.coffeeshop.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static volatile CloudinaryHelper instance;
    private boolean isInitialized = false;
    private static final String CLOUD_NAME = "ddo9kqdnd";
    private static final String API_KEY = "317549328636269";
    private static final String API_SECRET = "7WZQu4ekIkPbK-XaRB4cg9gaN4w";

    private CloudinaryHelper() {}

    public static CloudinaryHelper getInstance() {
        if (instance == null) {
            synchronized (CloudinaryHelper.class) {
                if (instance == null) {
                    instance = new CloudinaryHelper();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);

            try {
                MediaManager.init(context, config);
                isInitialized = true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Cloudinary", e);
            }
        }
    }

    public interface UploadListener {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(String error);
    }

    public void uploadImage(Uri imageUri, String folder, UploadListener listener) {
        if (!isInitialized) {
            listener.onUploadFailure("Cloudinary not initialized");
            return;
        }

        try {
            MediaManager.get().upload(imageUri)
                    .option("folder", folder)
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String secureUrl = (String) resultData.get("secure_url");
                            listener.onUploadSuccess(secureUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            listener.onUploadFailure(error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();
        } catch (Exception e) {
            listener.onUploadFailure(e.getMessage());
        }
    }
}