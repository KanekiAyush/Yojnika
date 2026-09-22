package com.yojnika.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

import com.yojnika.app.models.UserProfile;

public class SharedPrefsManager {
    private static SharedPrefsManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }

    public void saveUserProfile(UserProfile profile) {
        if (profile == null) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_USER_EXISTS, true);
        editor.putBoolean(Constants.KEY_PROFILE_COMPLETED, true);
        editor.putString(Constants.KEY_FULL_NAME, profile.getFullName());
        editor.putInt(Constants.KEY_AGE, profile.getAge());
        editor.putString(Constants.KEY_GENDER, profile.getGender());
        editor.putLong(Constants.KEY_ANNUAL_INCOME, profile.getAnnualIncome());
        editor.putString(Constants.KEY_OCCUPATION, profile.getOccupation());
        editor.putString(Constants.KEY_EDUCATION, profile.getEducationLevel());
        editor.putString(Constants.KEY_CATEGORY, profile.getCategory());
        editor.putString(Constants.KEY_STATE, profile.getState());
        editor.putString(Constants.KEY_DISTRICT, profile.getDistrict());
        editor.putString(Constants.KEY_MARITAL_STATUS, profile.getMaritalStatus());
        editor.apply();
    }

    public UserProfile getUserProfile() {
        if (!hasUserProfile()) {
            return null;
        }
        UserProfile profile = new UserProfile();
        profile.setFullName(sharedPreferences.getString(Constants.KEY_FULL_NAME, ""));
        profile.setAge(sharedPreferences.getInt(Constants.KEY_AGE, 0));
        profile.setGender(sharedPreferences.getString(Constants.KEY_GENDER, "Male"));
        profile.setAnnualIncome(sharedPreferences.getLong(Constants.KEY_ANNUAL_INCOME, 0L));
        profile.setOccupation(sharedPreferences.getString(Constants.KEY_OCCUPATION, "Student"));
        profile.setEducationLevel(sharedPreferences.getString(Constants.KEY_EDUCATION, "12th Pass"));
        profile.setCategory(sharedPreferences.getString(Constants.KEY_CATEGORY, "General"));
        profile.setState(sharedPreferences.getString(Constants.KEY_STATE, "All India"));
        profile.setDistrict(sharedPreferences.getString(Constants.KEY_DISTRICT, ""));
        profile.setMaritalStatus(sharedPreferences.getString(Constants.KEY_MARITAL_STATUS, "Unmarried"));
        return profile;
    }

    public boolean hasUserProfile() {
        return sharedPreferences.getBoolean(Constants.KEY_USER_EXISTS, false)
                && !sharedPreferences.getString(Constants.KEY_FULL_NAME, "").isEmpty();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getInt(Constants.KEY_LOGGED_IN_USER_ID, -1) != -1;
    }

    public void setLoggedIn(int userId) {
        sharedPreferences.edit()
                .putInt(Constants.KEY_LOGGED_IN_USER_ID, userId)
                .putBoolean(Constants.KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public int getLoggedInUserId() {
        return sharedPreferences.getInt(Constants.KEY_LOGGED_IN_USER_ID, -1);
    }

    public void clearSession() {
        sharedPreferences.edit()
                .remove(Constants.KEY_LOGGED_IN_USER_ID)
                .putBoolean(Constants.KEY_IS_LOGGED_IN, false)
                .apply();
    }

    public void registerUser(String email, String phone, String password, String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // Clear old profile data to ensure clean slate for new user
        editor.remove(Constants.KEY_FULL_NAME);
        editor.remove(Constants.KEY_AGE);
        editor.remove(Constants.KEY_GENDER);
        editor.remove(Constants.KEY_ANNUAL_INCOME);
        editor.remove(Constants.KEY_OCCUPATION);
        editor.remove(Constants.KEY_EDUCATION);
        editor.remove(Constants.KEY_CATEGORY);
        editor.remove(Constants.KEY_STATE);
        editor.remove(Constants.KEY_DISTRICT);
        editor.remove(Constants.KEY_MARITAL_STATUS);
        editor.remove(Constants.KEY_PROFILE_IMAGE_PATH);
        
        editor.putBoolean(Constants.KEY_PROFILE_COMPLETED, false);
        editor.putString(Constants.KEY_REG_EMAIL, email);
        editor.putString(Constants.KEY_REG_PHONE, phone);
        editor.putString(Constants.KEY_REG_PASSWORD, password);
        editor.putBoolean(Constants.KEY_USER_EXISTS, true);
        editor.apply();
    }

    public boolean isProfileComplete() {
        return sharedPreferences.getBoolean(Constants.KEY_PROFILE_COMPLETED, false);
    }

    public String getRegisteredEmail() {
        return sharedPreferences.getString(Constants.KEY_REG_EMAIL, "");
    }

    public String getRegisteredPhone() {
        return sharedPreferences.getString(Constants.KEY_REG_PHONE, "");
    }

    public String getRegisteredPassword() {
        return sharedPreferences.getString(Constants.KEY_REG_PASSWORD, "");
    }

    public void saveProfileImagePath(int userId, String path) {
        sharedPreferences.edit().putString(Constants.KEY_PROFILE_IMAGE_PATH + "_" + userId, path).apply();
    }

    public String getProfileImagePath(int userId) {
        // Migration logic: if global key exists, move to user-specific key
        if (sharedPreferences.contains(Constants.KEY_PROFILE_IMAGE_PATH)) {
            String globalPath = sharedPreferences.getString(Constants.KEY_PROFILE_IMAGE_PATH, null);
            if (globalPath != null) {
                sharedPreferences.edit()
                        .putString(Constants.KEY_PROFILE_IMAGE_PATH + "_" + userId, globalPath)
                        .remove(Constants.KEY_PROFILE_IMAGE_PATH)
                        .apply();
                return globalPath;
            }
        }
        return sharedPreferences.getString(Constants.KEY_PROFILE_IMAGE_PATH + "_" + userId, null);
    }

    public void removeProfileImagePath(int userId) {
        sharedPreferences.edit().remove(Constants.KEY_PROFILE_IMAGE_PATH + "_" + userId).apply();
    }

    public void setThemeMode(int mode) {
        sharedPreferences.edit().putInt(Constants.KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(Constants.KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void applyTheme() {
        AppCompatDelegate.setDefaultNightMode(getThemeMode());
    }

    public void setContentLanguage(String langCode) {
        sharedPreferences.edit().putString(Constants.KEY_CONTENT_LANGUAGE, langCode).apply();
    }

    public String getContentLanguage() {
        return sharedPreferences.getString(Constants.KEY_CONTENT_LANGUAGE, "en");
    }

    public void clearProfile() {
        sharedPreferences.edit().clear().apply();
    }
}
