package com.example.coffeeshop.utils;

public class SessionManager {
    private static SessionManager instance;
    private int userId;
    private String userName;

    private SessionManager() {
        userId = -1;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isLoggedIn() {
        return userId != -1;
    }

    public void logout() {
        userId = -1;
        userName = null;
    }
}