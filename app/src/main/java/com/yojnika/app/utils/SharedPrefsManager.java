package com.yojnika.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

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

    public void clearProfile() {
        sharedPreferences.edit().clear().apply();
    }
}
