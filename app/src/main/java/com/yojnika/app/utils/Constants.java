package com.yojnika.app.utils;

public class Constants {
    // Database Constants
    public static final String DATABASE_NAME = "yojnika_schemes_v2.db";
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
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_REG_EMAIL = "reg_email";
    public static final String KEY_REG_PHONE = "reg_phone";
    public static final String KEY_REG_PASSWORD = "reg_password";
    public static final String KEY_PROFILE_IMAGE_PATH = "profile_image_path";
    public static final String KEY_PROFILE_COMPLETED = "profile_completed";
    public static final String KEY_THEME_MODE = "theme_mode";

    // Intent Extras
    public static final String EXTRA_SCHEME_ID = "extra_scheme_id";
    public static final String EXTRA_FROM_SPLASH = "extra_from_splash";
    public static final String EXTRA_IS_SETUP_MODE = "extra_is_setup_mode";

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
