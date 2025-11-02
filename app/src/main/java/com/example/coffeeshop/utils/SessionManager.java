package com.example.coffeeshop.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static volatile SessionManager instance;
    private static final String PREF_NAME = "CoffeeShopSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private int userId;
    private String userName;
    private String userRole;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        loadSession(); // Load existing session on initialization
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    //returns instance if already initialized
    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager must be initialized with Context first");
        }
        return instance;
    }

    private void loadSession() {
        userId = prefs.getInt(KEY_USER_ID, -1);
        userName = prefs.getString(KEY_USER_NAME, null);
        userRole = prefs.getString(KEY_USER_ROLE, null);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        editor.putInt(KEY_USER_ID, userId);
        //asynchronously
        editor.apply();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
        editor.putString(KEY_USER_ROLE, userRole);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return userId != -1;
    }

    public void logout() {
        userId = -1;
        userName = null;
        userRole = null;
        editor.clear();
        editor.apply();
    }

    public boolean isAdmin() {
        return "admin".equals(userRole);
    }
}