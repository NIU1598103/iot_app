package com.example.iotapp.data;

public class UserData {
    private static UserData sInstance;
    private String userId;

    // private constructor to limit new instance creation
    private UserData() {}

    public static UserData getInstance() {
        if (sInstance == null) {
            sInstance = new UserData();
        }

        return sInstance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
