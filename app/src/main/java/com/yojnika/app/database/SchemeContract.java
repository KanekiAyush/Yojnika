package com.yojnika.app.database;

import android.provider.BaseColumns;

public final class SchemeContract {
    private SchemeContract() {}

    public static class SchemeEntry implements BaseColumns {
        public static final String TABLE_NAME = "schemes";
        public static final String COLUMN_SCHEME_ID = "scheme_id";
        public static final String COLUMN_SCHEME_NAME = "scheme_name";
        public static final String COLUMN_SCHEME_DESCRIPTION = "scheme_description";
        public static final String COLUMN_MIN_AGE = "min_age";
        public static final String COLUMN_MAX_AGE = "max_age";
        public static final String COLUMN_GENDER_ELIGIBLE = "gender_eligible";
        public static final String COLUMN_INCOME_LIMIT = "income_limit";
        public static final String COLUMN_ELIGIBLE_OCCUPATIONS = "eligible_occupations";
        public static final String COLUMN_MIN_EDUCATION_LEVEL = "min_education_level";
        public static final String COLUMN_ELIGIBLE_CATEGORY = "eligible_category";
        public static final String COLUMN_ELIGIBLE_STATES = "eligible_states";
        public static final String COLUMN_MARITAL_STATUS = "marital_status_requirement";
        public static final String COLUMN_BENEFITS = "benefits";
        public static final String COLUMN_APPLICATION_PROCESS = "application_process";
        public static final String COLUMN_OFFICIAL_WEBSITE = "official_website";
        public static final String COLUMN_SCHEME_TYPE = "scheme_type";
        public static final String COLUMN_SLUG = "slug";
        public static final String COLUMN_DOCUMENTS = "documents";
        public static final String COLUMN_SCHEME_CATEGORY = "scheme_category";
        public static final String COLUMN_TAGS = "tags";
        public static final String COLUMN_ELIGIBILITY_TEXT = "eligibility_text";
        public static final String COLUMN_CREATED_DATE = "created_date";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_IS_BOOKMARKED = "is_bookmarked";

        // Hindi Translations
        public static final String COLUMN_SCHEME_NAME_HI = "scheme_name_hi";
        public static final String COLUMN_SCHEME_DESCRIPTION_HI = "scheme_description_hi";
        public static final String COLUMN_BENEFITS_HI = "benefits_hi";
        public static final String COLUMN_APPLICATION_PROCESS_HI = "application_process_hi";
        public static final String COLUMN_DOCUMENTS_HI = "documents_hi";
        public static final String COLUMN_ELIGIBILITY_TEXT_HI = "eligibility_text_hi";

        // Marathi Translations
        public static final String COLUMN_SCHEME_NAME_MR = "scheme_name_mr";
        public static final String COLUMN_SCHEME_DESCRIPTION_MR = "scheme_description_mr";
        public static final String COLUMN_BENEFITS_MR = "benefits_mr";
        public static final String COLUMN_APPLICATION_PROCESS_MR = "application_process_mr";
        public static final String COLUMN_DOCUMENTS_MR = "documents_mr";
        public static final String COLUMN_ELIGIBILITY_TEXT_MR = "eligibility_text_mr";
    }

    public static class BookmarkEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookmarks";
        public static final String COLUMN_SCHEME_ID = "scheme_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_SAVED_TIMESTAMP = "saved_timestamp";
    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_ANNUAL_INCOME = "annual_income";
        public static final String COLUMN_OCCUPATION = "occupation";
        public static final String COLUMN_EDUCATION = "education";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_DISTRICT = "district";
        public static final String COLUMN_MARITAL_STATUS = "marital_status";
        public static final String COLUMN_CREATED_AT = "created_at";
    }
}
