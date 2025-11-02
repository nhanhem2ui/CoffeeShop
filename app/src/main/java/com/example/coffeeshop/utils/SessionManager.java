package com.example.coffeeshop.utils;

public class SessionManager {

    //multiple threads access
    private static volatile SessionManager instance;
    private int userId;
    private String userName;
    private String userRole;

    private SessionManager() {
        userId = -1;
    }

    public static SessionManager getInstance() {
        //already exists → no need to lock.
        if (instance == null) {
            //locks
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
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

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public boolean isLoggedIn() {
        return userId != -1;
    }

    public void logout() {
        userId = -1;
        userName = null;
        userRole = null;
    }

    public boolean isAdmin() {
        return "admin".equals(userRole);
    }
}