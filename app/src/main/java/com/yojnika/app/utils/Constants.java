package com.yojnika.app.utils;

public class Constants {
    // Database Constants
    public static final String DATABASE_NAME = "yojnika_schemes.db";
    public static final int DATABASE_VERSION = 1;

    // Shared Preferences Keys
    public static final String PREF_NAME = "yojnika_user_profile";
    public static final String KEY_USER_EXISTS = "user_exists";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_AGE = "age";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_ANNUAL_INCOME = "annual_income";
    public static final String KEY_OCCUPATION = "occupation";
    public static final String KEY_EDUCATION = "education";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_STATE = "state";
    public static final String KEY_DISTRICT = "district";
    public static final String KEY_MARITAL_STATUS = "marital_status";

    // Intent Extras
    public static final String EXTRA_SCHEME_ID = "extra_scheme_id";
    public static final String EXTRA_FROM_SPLASH = "extra_from_splash";

    // ML Model Configuration
    public static final String MODEL_FILE_NAME = "scheme_model.onnx";
    public static final int FEATURE_VECTOR_SIZE = 64;

    // Standard Categories
    public static final String[] CATEGORIES = {"General", "OBC", "SC", "ST", "EWS"};
    public static final String[] OCCUPATIONS = {"Student", "Employee", "Unemployed", "Farmer", "Retired", "Business"};
    public static final String[] GENDERS = {"Male", "Female", "Other"};
    public static final String[] MARITAL_STATUSES = {"Unmarried", "Married"};
    public static final String[] EDUCATION_LEVELS = {"Below 10th", "10th Pass", "12th Pass", "Graduate", "Post Graduate", "Diploma", "PhD"};
}
