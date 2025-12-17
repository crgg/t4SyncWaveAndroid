package com.t4app.t4syncwave;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static SessionManager instance;
    private static Context context;
    private static final String SHARED_PREFS_NAME = "user_session_prefs";
    private static final String KEY_USER_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_TOKEN_USER = "token";
    private static final String KEY_REMEMBER_ME = "remember";


    private SharedPreferences.Editor editor;

    private SessionManager(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserSessionManager not initialized. Call initialize(context) first.");
        }
        return instance;
    }

    public void setUserEmail(String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public static synchronized void initialize(Context ctx) {
        if (instance == null) {
            instance = new SessionManager(ctx);
        }
    }

    public void saveUserDetails(String id,
                                String name,
                                String userEmail,
                                String tokenKey,
                                boolean rememberMe){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, id);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_TOKEN_USER, tokenKey);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    public String getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public String getUserEmail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getTokenKey() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN_USER, null);
    }

    public boolean getRememberMe() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void clearSession() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
