package com.yojnika.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.HashUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SchemeDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "SchemeDatabaseHelper";
    private static SchemeDatabaseHelper instance;
    private final Context mContext;

    public static final String[] FULL_PROJECTION = new String[]{
            SchemeContract.SchemeEntry.COLUMN_SCHEME_ID,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME,
            SchemeContract.SchemeEntry.COLUMN_SLUG,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION,
            SchemeContract.SchemeEntry.COLUMN_MIN_AGE,
            SchemeContract.SchemeEntry.COLUMN_MAX_AGE,
            SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE,
            SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS,
            SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES,
            SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS,
            SchemeContract.SchemeEntry.COLUMN_BENEFITS,
            SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS,
            SchemeContract.SchemeEntry.COLUMN_DOCUMENTS,
            SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY,
            SchemeContract.SchemeEntry.COLUMN_TAGS,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT,
            SchemeContract.SchemeEntry.COLUMN_CREATED_DATE,
            SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE,
            SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_HI,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_HI,
            SchemeContract.SchemeEntry.COLUMN_BENEFITS_HI,
            SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_HI,
            SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_HI,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_HI,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_MR,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_MR,
            SchemeContract.SchemeEntry.COLUMN_BENEFITS_MR,
            SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_MR,
            SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_MR,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_MR
    };

    public static final String[] SUMMARY_PROJECTION = new String[]{
            SchemeContract.SchemeEntry.COLUMN_SCHEME_ID,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME,
            SchemeContract.SchemeEntry.COLUMN_SLUG,
            SchemeContract.SchemeEntry.COLUMN_MIN_AGE,
            SchemeContract.SchemeEntry.COLUMN_MAX_AGE,
            SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE,
            SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS,
            SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY,
            SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES,
            SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS,
            SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE,
            SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY,
            SchemeContract.SchemeEntry.COLUMN_TAGS,
            SchemeContract.SchemeEntry.COLUMN_CREATED_DATE,
            SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE,
            SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED
    };

    public static synchronized SchemeDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SchemeDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public SchemeDatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        this.mContext = context.getApplicationContext();
        checkAndCopyDatabase();
    }

    private synchronized void checkAndCopyDatabase() {
        File dbFile = mContext.getDatabasePath(Constants.DATABASE_NAME);
        boolean shouldCopy = !dbFile.exists();

        if (!shouldCopy) {
            try (SQLiteDatabase checkDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
                Cursor c = checkDb.rawQuery("SELECT COUNT(*) FROM " + SchemeContract.SchemeEntry.TABLE_NAME, null);
                if (c != null && c.moveToFirst()) {
                    if (c.getInt(0) == 0) {
                        shouldCopy = true; // Recovery: database exists but is empty
                    }
                    c.close();
                }
            } catch (Exception e) {
                shouldCopy = true;
            }
        }

        if (shouldCopy || dbFile.length() < 1000000) {
            try {
                if (dbFile.getParentFile() != null) {
                    dbFile.getParentFile().mkdirs();
                }
                InputStream is = mContext.getAssets().open("yojnika_schemes.db");
                OutputStream os = new FileOutputStream(dbFile);
                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                os.close();
                is.close();
                Log.i(TAG, "Successfully copied prebuilt SQLite database from assets.");

                try (SQLiteDatabase checkDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE)) {
                    checkDb.execSQL("PRAGMA user_version = " + Constants.DATABASE_VERSION);
                    ensureSchema(checkDb);
                } catch (Exception e) {
                    Log.e(TAG, "Failed setting user_version PRAGMA or schema check", e);
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy prebuilt SQLite database from assets", e);
            }
        }

        Log.d(TAG, "DB path: " + dbFile.getAbsolutePath());
        Log.d(TAG, "DB size: " + dbFile.length());
        try (SQLiteDatabase checkDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
            Cursor c = checkDb.rawQuery("SELECT COUNT(*) FROM " + SchemeContract.SchemeEntry.TABLE_NAME, null);
            if (c != null && c.moveToFirst()) {
                Log.d(TAG, "Row count: " + c.getInt(0));
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging database row count", e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCHEMES_TABLE = "CREATE TABLE IF NOT EXISTS " + SchemeContract.SchemeEntry.TABLE_NAME + " ("
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " INTEGER PRIMARY KEY,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " TEXT NOT NULL,"
                + SchemeContract.SchemeEntry.COLUMN_SLUG + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_MIN_AGE + " INTEGER,"
                + SchemeContract.SchemeEntry.COLUMN_MAX_AGE + " INTEGER,"
                + SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT + " INTEGER,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_BENEFITS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_DOCUMENTS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_TAGS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_CREATED_DATE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED + " INTEGER DEFAULT 0,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_HI + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_HI + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_BENEFITS_HI + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_HI + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_HI + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_HI + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_MR + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_MR + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_BENEFITS_MR + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_MR + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_MR + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_MR + " TEXT"
                + ");";

        String CREATE_BOOKMARKS_TABLE = "CREATE TABLE IF NOT EXISTS " + SchemeContract.BookmarkEntry.TABLE_NAME + " ("
                + SchemeContract.BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SchemeContract.BookmarkEntry.COLUMN_USER_ID + " INTEGER,"
                + SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " INTEGER,"
                + SchemeContract.BookmarkEntry.COLUMN_SAVED_TIMESTAMP + " INTEGER,"
                + "UNIQUE(" + SchemeContract.BookmarkEntry.COLUMN_USER_ID + ", " + SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + ")"
                + ");";

        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + SchemeContract.UserEntry.TABLE_NAME + " ("
                + SchemeContract.UserEntry.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SchemeContract.UserEntry.COLUMN_EMAIL + " TEXT UNIQUE,"
                + SchemeContract.UserEntry.COLUMN_PHONE + " TEXT UNIQUE,"
                + SchemeContract.UserEntry.COLUMN_PASSWORD_HASH + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_FULL_NAME + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_AGE + " INTEGER,"
                + SchemeContract.UserEntry.COLUMN_GENDER + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_ANNUAL_INCOME + " INTEGER,"
                + SchemeContract.UserEntry.COLUMN_OCCUPATION + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_EDUCATION + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_CATEGORY + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_STATE + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_DISTRICT + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_MARITAL_STATUS + " TEXT,"
                + SchemeContract.UserEntry.COLUMN_CREATED_AT + " INTEGER"
                + ");";

        db.execSQL(CREATE_SCHEMES_TABLE);
        db.execSQL(CREATE_BOOKMARKS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do not drop tables! We want to keep the data from assets.
        // ensureSchema in onOpen will handle missing columns.
        if (oldVersion < 2) {
            ensureSchema(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureSchema(db);
    }

    private void ensureSchema(SQLiteDatabase db) {
        String tableName = SchemeContract.SchemeEntry.TABLE_NAME;
        String[] columns = {
                SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED,
                SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_HI,
                SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_HI,
                SchemeContract.SchemeEntry.COLUMN_BENEFITS_HI,
                SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_HI,
                SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_HI,
                SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_HI,
                SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_MR,
                SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_MR,
                SchemeContract.SchemeEntry.COLUMN_BENEFITS_MR,
                SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_MR,
                SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_MR,
                SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_MR
        };
        
        for (String col : columns) {
            try {
                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + col + (col.equals(SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED) ? " INTEGER DEFAULT 0;" : " TEXT;"));
            } catch (Exception ignored) {}
        }

        // Bookmark table updates for multi-user
        try {
            db.execSQL("ALTER TABLE " + SchemeContract.BookmarkEntry.TABLE_NAME + " ADD COLUMN " + SchemeContract.BookmarkEntry.COLUMN_USER_ID + " INTEGER DEFAULT 0;");
        } catch (Exception ignored) {}

        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + SchemeContract.UserEntry.TABLE_NAME + " ("
                    + SchemeContract.UserEntry.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SchemeContract.UserEntry.COLUMN_EMAIL + " TEXT UNIQUE,"
                    + SchemeContract.UserEntry.COLUMN_PHONE + " TEXT UNIQUE,"
                    + SchemeContract.UserEntry.COLUMN_PASSWORD_HASH + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_FULL_NAME + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_AGE + " INTEGER,"
                    + SchemeContract.UserEntry.COLUMN_GENDER + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_ANNUAL_INCOME + " INTEGER,"
                    + SchemeContract.UserEntry.COLUMN_OCCUPATION + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_EDUCATION + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_CATEGORY + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_STATE + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_DISTRICT + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_MARITAL_STATUS + " TEXT,"
                    + SchemeContract.UserEntry.COLUMN_CREATED_AT + " INTEGER"
                    + ");");
        } catch (Exception ignored) {}
        
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + SchemeContract.BookmarkEntry.TABLE_NAME + " ("
                    + SchemeContract.BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SchemeContract.BookmarkEntry.COLUMN_USER_ID + " INTEGER,"
                    + SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " INTEGER,"
                    + SchemeContract.BookmarkEntry.COLUMN_SAVED_TIMESTAMP + " INTEGER,"
                    + "UNIQUE(" + SchemeContract.BookmarkEntry.COLUMN_USER_ID + ", " + SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + ")"
                    + ");");
        } catch (Exception ignored) {}
    }

    public long registerUser(String name, String email, String phone, String password, UserProfile initialProfile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchemeContract.UserEntry.COLUMN_FULL_NAME, name);
        values.put(SchemeContract.UserEntry.COLUMN_EMAIL, email);
        values.put(SchemeContract.UserEntry.COLUMN_PHONE, phone);
        values.put(SchemeContract.UserEntry.COLUMN_PASSWORD_HASH, HashUtils.sha256(password));
        values.put(SchemeContract.UserEntry.COLUMN_CREATED_AT, System.currentTimeMillis());

        if (initialProfile != null) {
            values.put(SchemeContract.UserEntry.COLUMN_AGE, initialProfile.getAge());
            values.put(SchemeContract.UserEntry.COLUMN_GENDER, initialProfile.getGender());
            values.put(SchemeContract.UserEntry.COLUMN_ANNUAL_INCOME, initialProfile.getAnnualIncome());
            values.put(SchemeContract.UserEntry.COLUMN_OCCUPATION, initialProfile.getOccupation());
            values.put(SchemeContract.UserEntry.COLUMN_EDUCATION, initialProfile.getEducationLevel());
            values.put(SchemeContract.UserEntry.COLUMN_CATEGORY, initialProfile.getCategory());
            values.put(SchemeContract.UserEntry.COLUMN_STATE, initialProfile.getState());
            values.put(SchemeContract.UserEntry.COLUMN_DISTRICT, initialProfile.getDistrict());
            values.put(SchemeContract.UserEntry.COLUMN_MARITAL_STATUS, initialProfile.getMaritalStatus());
        }

        try {
            long id = db.insertWithOnConflict(SchemeContract.UserEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
            Log.d("AUTH", "User registered: " + email + ", ID=" + id);
            return id;
        } catch (Exception e) {
            Log.e("AUTH", "Registration failed for " + email, e);
            return -1;
        }
    }

    public int loginUser(String input, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = SchemeContract.UserEntry.COLUMN_EMAIL + " = ? OR " + SchemeContract.UserEntry.COLUMN_PHONE + " = ?";
        String[] args = {input, input};
        String hash = HashUtils.sha256(password);

        try (Cursor cursor = db.query(SchemeContract.UserEntry.TABLE_NAME, 
                new String[]{SchemeContract.UserEntry.COLUMN_USER_ID, SchemeContract.UserEntry.COLUMN_PASSWORD_HASH},
                selection, args, null, null, null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                String storedHash = cursor.getString(1);
                if (hash.equals(storedHash)) {
                    int userId = cursor.getInt(0);
                    Log.d("AUTH", "Login success for " + input + ", ID=" + userId);
                    return userId;
                }
            }
        } catch (Exception e) {
            Log.e("AUTH", "Login error", e);
        }
        return -1;
    }

    public UserProfile getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(SchemeContract.UserEntry.TABLE_NAME, null,
                SchemeContract.UserEntry.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                UserProfile profile = new UserProfile();
                profile.setFullName(getString(cursor, SchemeContract.UserEntry.COLUMN_FULL_NAME));
                profile.setAge(getInt(cursor, SchemeContract.UserEntry.COLUMN_AGE));
                profile.setGender(getString(cursor, SchemeContract.UserEntry.COLUMN_GENDER));
                profile.setAnnualIncome(getLong(cursor, SchemeContract.UserEntry.COLUMN_ANNUAL_INCOME));
                profile.setOccupation(getString(cursor, SchemeContract.UserEntry.COLUMN_OCCUPATION));
                profile.setEducationLevel(getString(cursor, SchemeContract.UserEntry.COLUMN_EDUCATION));
                profile.setCategory(getString(cursor, SchemeContract.UserEntry.COLUMN_CATEGORY));
                profile.setState(getString(cursor, SchemeContract.UserEntry.COLUMN_STATE));
                profile.setDistrict(getString(cursor, SchemeContract.UserEntry.COLUMN_DISTRICT));
                profile.setMaritalStatus(getString(cursor, SchemeContract.UserEntry.COLUMN_MARITAL_STATUS));
                return profile;
            }
        }
        return null;
    }

    private String getString(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        return idx != -1 ? c.getString(idx) : "";
    }

    private int getInt(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        return idx != -1 ? c.getInt(idx) : 0;
    }

    private long getLong(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        return idx != -1 ? c.getLong(idx) : 0L;
    }

    public void updateUserProfile(int userId, UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(SchemeContract.UserEntry.COLUMN_FULL_NAME, profile.getFullName());
        v.put(SchemeContract.UserEntry.COLUMN_AGE, profile.getAge());
        v.put(SchemeContract.UserEntry.COLUMN_GENDER, profile.getGender());
        v.put(SchemeContract.UserEntry.COLUMN_ANNUAL_INCOME, profile.getAnnualIncome());
        v.put(SchemeContract.UserEntry.COLUMN_OCCUPATION, profile.getOccupation());
        v.put(SchemeContract.UserEntry.COLUMN_EDUCATION, profile.getEducationLevel());
        v.put(SchemeContract.UserEntry.COLUMN_CATEGORY, profile.getCategory());
        v.put(SchemeContract.UserEntry.COLUMN_STATE, profile.getState());
        v.put(SchemeContract.UserEntry.COLUMN_DISTRICT, profile.getDistrict());
        v.put(SchemeContract.UserEntry.COLUMN_MARITAL_STATUS, profile.getMaritalStatus());
        
        db.update(SchemeContract.UserEntry.TABLE_NAME, v, SchemeContract.UserEntry.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public void migrateLegacyBookmarks(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(SchemeContract.BookmarkEntry.COLUMN_USER_ID, userId);
            int count = db.update(SchemeContract.BookmarkEntry.TABLE_NAME, values, 
                    SchemeContract.BookmarkEntry.COLUMN_USER_ID + " = 0 OR " + SchemeContract.BookmarkEntry.COLUMN_USER_ID + " IS NULL", null);
            Log.d("BOOKMARK", "Migrated " + count + " legacy bookmarks to user ID " + userId);
        } catch (Exception e) {
            Log.e("BOOKMARK", "Migration failed", e);
        }
    }

    public List<Scheme> getAllSchemes(int userId) {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    SUMMARY_PROJECTION,
                    SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " = 1",
                    null,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Scheme s = cursorToScheme(cursor);
                    s.setBookmarked(isSchemeBookmarked(s.getSchemeId(), userId));
                    schemes.add(s);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all schemes", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public Scheme getSchemeById(int schemeId, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    FULL_PROJECTION,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " = ?",
                    new String[]{String.valueOf(schemeId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                Scheme s = cursorToScheme(cursor);
                s.setBookmarked(isSchemeBookmarked(schemeId, userId));
                return s;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching scheme by id: " + schemeId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public List<Scheme> searchAndFilterSchemes(String query, String stateFilter, String typeFilter, String categoryFilter, int userId) {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            StringBuilder selection = new StringBuilder(SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " = 1");
            List<String> selectionArgs = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_BENEFITS).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_TAGS).append(" LIKE ?)");
                String pattern = "%" + query.trim() + "%";
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
            }

            if (typeFilter != null && !typeFilter.equals("All") && !typeFilter.equals("All Types")) {
                selection.append(" AND ").append(SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE).append(" LIKE ?");
                selectionArgs.add("%" + typeFilter + "%");
            }

            if (stateFilter != null && !stateFilter.equals("All") && !stateFilter.equals("All India")) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES).append(" LIKE '%All India%')");
                selectionArgs.add("%" + stateFilter + "%");
            }

            if (categoryFilter != null && !categoryFilter.equals("All") && !categoryFilter.equals("All Categories")) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_TAGS).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS).append(" LIKE ?)");
                String catPattern = "%" + categoryFilter + "%";
                selectionArgs.add(catPattern);
                selectionArgs.add(catPattern);
                selectionArgs.add(catPattern);
            }

            String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    SUMMARY_PROJECTION,
                    selection.toString(),
                    args,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Scheme s = cursorToScheme(cursor);
                    s.setBookmarked(isSchemeBookmarked(s.getSchemeId(), userId));
                    schemes.add(s);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering schemes", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public List<Scheme> searchAndFilterSchemesPaged(int page, int pageSize, String query, String stateFilter, String typeFilter, String categoryFilter, int userId) {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            StringBuilder selection = new StringBuilder(SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " = 1");
            List<String> selectionArgs = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_BENEFITS).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_TAGS).append(" LIKE ?)");
                String pattern = "%" + query.trim() + "%";
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
            }

            if (typeFilter != null && !typeFilter.equals("All") && !typeFilter.equals("All Types")) {
                selection.append(" AND ").append(SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE).append(" LIKE ?");
                selectionArgs.add("%" + typeFilter + "%");
            }

            if (stateFilter != null && !stateFilter.equals("All") && !stateFilter.equals("All India")) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES).append(" LIKE '%All India%')");
                selectionArgs.add("%" + stateFilter + "%");
            }

            if (categoryFilter != null && !categoryFilter.equals("All") && !categoryFilter.equals("All Categories")) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_TAGS).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS).append(" LIKE ?)");
                String catPattern = "%" + categoryFilter + "%";
                selectionArgs.add(catPattern);
                selectionArgs.add(catPattern);
                selectionArgs.add(catPattern);
            }

            String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);
            int offset = Math.max(0, (page - 1) * pageSize);
            String limitOffset = offset + ", " + pageSize;

            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    SUMMARY_PROJECTION,
                    selection.toString(),
                    args,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC",
                    limitOffset
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Scheme s = cursorToScheme(cursor);
                    s.setBookmarked(isSchemeBookmarked(s.getSchemeId(), userId));
                    schemes.add(s);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering schemes paged", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public List<Scheme> getBookmarkedSchemes(int userId) {
        Log.d("BOOKMARK", "=== getBookmarkedSchemes called for userId: " + userId);
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    SUMMARY_PROJECTION,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " IN (SELECT " + SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID 
                            + " FROM " + SchemeContract.BookmarkEntry.TABLE_NAME 
                            + " WHERE " + SchemeContract.BookmarkEntry.COLUMN_USER_ID + " = ?)",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Scheme s = cursorToScheme(cursor);
                    s.setBookmarked(true);
                    schemes.add(s);
                } while (cursor.moveToNext());
            }
            Log.d("BOOKMARK", "User " + userId + " saved list size=" + schemes.size());
        } catch (Exception e) {
            Log.e(TAG, "Error fetching bookmarked schemes", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public boolean isBookmarkedForUser(int schemeId, int userId) {
        return isSchemeBookmarked(schemeId, userId);
    }

    public boolean isSchemeBookmarked(int schemeId, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(SchemeContract.BookmarkEntry.TABLE_NAME, null,
                SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " = ? AND " + SchemeContract.BookmarkEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(schemeId), String.valueOf(userId)},
                null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean toggleBookmark(int schemeId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean isBookmarked = isSchemeBookmarked(schemeId, userId);
        boolean newState = !isBookmarked;
        
        try {
            Log.d("BOOKMARK", "=== toggleBookmark called: id=" + schemeId + ", user=" + userId + ", newState=" + newState);

            if (newState) {
                ContentValues bm = new ContentValues();
                bm.put(SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID, schemeId);
                bm.put(SchemeContract.BookmarkEntry.COLUMN_USER_ID, userId);
                bm.put(SchemeContract.BookmarkEntry.COLUMN_SAVED_TIMESTAMP, System.currentTimeMillis());
                db.insertWithOnConflict(SchemeContract.BookmarkEntry.TABLE_NAME, null, bm, SQLiteDatabase.CONFLICT_REPLACE);
            } else {
                db.delete(
                        SchemeContract.BookmarkEntry.TABLE_NAME,
                        SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " = ? AND " + SchemeContract.BookmarkEntry.COLUMN_USER_ID + " = ?",
                        new String[]{String.valueOf(schemeId), String.valueOf(userId)}
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling bookmark for schemeId: " + schemeId, e);
        }
        return newState;
    }

    public void updateSchemeTranslations(Scheme scheme) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_HI, scheme.getSchemeNameHi());
            values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_HI, scheme.getSchemeDescriptionHi());
            values.put(SchemeContract.SchemeEntry.COLUMN_BENEFITS_HI, scheme.getBenefitsHi());
            values.put(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_HI, scheme.getApplicationProcessHi());
            values.put(SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_HI, scheme.getDocumentsHi());
            values.put(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_HI, scheme.getEligibilityTextHi());

            values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_MR, scheme.getSchemeNameMr());
            values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_MR, scheme.getSchemeDescriptionMr());
            values.put(SchemeContract.SchemeEntry.COLUMN_BENEFITS_MR, scheme.getBenefitsMr());
            values.put(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_MR, scheme.getApplicationProcessMr());
            values.put(SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_MR, scheme.getDocumentsMr());
            values.put(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_MR, scheme.getEligibilityTextMr());

            db.update(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    values,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " = ?",
                    new String[]{String.valueOf(scheme.getSchemeId())}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating translations for schemeId: " + scheme.getSchemeId(), e);
        }
    }

    private Scheme cursorToScheme(Cursor cursor) {
        Scheme scheme = new Scheme();

        int idIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_ID);
        if (idIdx != -1 && !cursor.isNull(idIdx)) {
            scheme.setSchemeId(cursor.getInt(idIdx));
        }

        int nameIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME);
        if (nameIdx != -1 && !cursor.isNull(nameIdx)) {
            scheme.setSchemeName(cursor.getString(nameIdx));
        }

        int descIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION);
        if (descIdx != -1 && !cursor.isNull(descIdx)) {
            scheme.setSchemeDescription(cursor.getString(descIdx));
        }

        int minAgeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MIN_AGE);
        if (minAgeIdx != -1 && !cursor.isNull(minAgeIdx)) {
            scheme.setMinAge(cursor.getInt(minAgeIdx));
        }

        int maxAgeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MAX_AGE);
        if (maxAgeIdx != -1 && !cursor.isNull(maxAgeIdx)) {
            scheme.setMaxAge(cursor.getInt(maxAgeIdx));
        }

        int genderIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE);
        if (genderIdx != -1 && !cursor.isNull(genderIdx)) {
            scheme.setGenderEligible(cursor.getString(genderIdx));
        }

        int incomeLimitIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT);
        if (incomeLimitIdx != -1 && !cursor.isNull(incomeLimitIdx)) {
            scheme.setIncomeLimit(cursor.getLong(incomeLimitIdx));
        }

        int occIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS);
        if (occIdx != -1 && !cursor.isNull(occIdx)) {
            scheme.setEligibleOccupations(cursor.getString(occIdx));
        }

        int eduIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL);
        if (eduIdx != -1 && !cursor.isNull(eduIdx)) {
            scheme.setMinEducationLevel(cursor.getString(eduIdx));
        }

        int catIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY);
        if (catIdx != -1 && !cursor.isNull(catIdx)) {
            scheme.setEligibleCategory(cursor.getString(catIdx));
        }

        int stateIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES);
        if (stateIdx != -1 && !cursor.isNull(stateIdx)) {
            scheme.setEligibleStates(cursor.getString(stateIdx));
        }

        int maritalIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS);
        if (maritalIdx != -1 && !cursor.isNull(maritalIdx)) {
            scheme.setMaritalStatusRequirement(cursor.getString(maritalIdx));
        }

        int benefitsIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_BENEFITS);
        if (benefitsIdx != -1 && !cursor.isNull(benefitsIdx)) {
            scheme.setBenefits(cursor.getString(benefitsIdx));
        }

        int appIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS);
        if (appIdx != -1 && !cursor.isNull(appIdx)) {
            scheme.setApplicationProcess(cursor.getString(appIdx));
        }

        int websiteIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE);
        if (websiteIdx != -1 && !cursor.isNull(websiteIdx)) {
            scheme.setOfficialWebsite(cursor.getString(websiteIdx));
        }

        int typeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE);
        if (typeIdx != -1 && !cursor.isNull(typeIdx)) {
            scheme.setSchemeType(cursor.getString(typeIdx));
        }

        int createdDateIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_CREATED_DATE);
        if (createdDateIdx != -1 && !cursor.isNull(createdDateIdx)) {
            scheme.setCreatedDate(cursor.getString(createdDateIdx));
        }

        int activeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE);
        if (activeIdx != -1 && !cursor.isNull(activeIdx)) {
            scheme.setActive(cursor.getInt(activeIdx) == 1);
        }

        int slugIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SLUG);
        if (slugIdx != -1 && !cursor.isNull(slugIdx)) {
            scheme.setSlug(cursor.getString(slugIdx));
        }

        int docsIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_DOCUMENTS);
        if (docsIdx != -1 && !cursor.isNull(docsIdx)) {
            scheme.setDocuments(cursor.getString(docsIdx));
        }

        int schemeCategoryIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY);
        if (schemeCategoryIdx != -1 && !cursor.isNull(schemeCategoryIdx)) {
            scheme.setSchemeCategory(cursor.getString(schemeCategoryIdx));
        }

        int tagsIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_TAGS);
        if (tagsIdx != -1 && !cursor.isNull(tagsIdx)) {
            scheme.setTags(cursor.getString(tagsIdx));
        }

        int eligTextIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT);
        if (eligTextIdx != -1 && !cursor.isNull(eligTextIdx)) {
            scheme.setEligibilityText(cursor.getString(eligTextIdx));
        }

        // Read Translations
        int nameHiIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_HI);
        if (nameHiIdx != -1 && !cursor.isNull(nameHiIdx)) {
            scheme.setSchemeNameHi(cursor.getString(nameHiIdx));
        }
        int descHiIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_HI);
        if (descHiIdx != -1 && !cursor.isNull(descHiIdx)) {
            scheme.setSchemeDescriptionHi(cursor.getString(descHiIdx));
        }
        int benefitsHiIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_BENEFITS_HI);
        if (benefitsHiIdx != -1 && !cursor.isNull(benefitsHiIdx)) {
            scheme.setBenefitsHi(cursor.getString(benefitsHiIdx));
        }
        int appHiIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_HI);
        if (appHiIdx != -1 && !cursor.isNull(appHiIdx)) {
            scheme.setApplicationProcessHi(cursor.getString(appHiIdx));
        }
        int docsHiIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_HI);
        if (docsHiIdx != -1 && !cursor.isNull(docsHiIdx)) {
            scheme.setDocumentsHi(cursor.getString(docsHiIdx));
        }
        int eligHiIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_HI);
        if (eligHiIdx != -1 && !cursor.isNull(eligHiIdx)) {
            scheme.setEligibilityTextHi(cursor.getString(eligHiIdx));
        }

        int nameMrIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME_MR);
        if (nameMrIdx != -1 && !cursor.isNull(nameMrIdx)) {
            scheme.setSchemeNameMr(cursor.getString(nameMrIdx));
        }
        int descMrIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION_MR);
        if (descMrIdx != -1 && !cursor.isNull(descMrIdx)) {
            scheme.setSchemeDescriptionMr(cursor.getString(descMrIdx));
        }
        int benefitsMrIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_BENEFITS_MR);
        if (benefitsMrIdx != -1 && !cursor.isNull(benefitsMrIdx)) {
            scheme.setBenefitsMr(cursor.getString(benefitsMrIdx));
        }
        int appMrIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS_MR);
        if (appMrIdx != -1 && !cursor.isNull(appMrIdx)) {
            scheme.setApplicationProcessMr(cursor.getString(appMrIdx));
        }
        int docsMrIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_DOCUMENTS_MR);
        if (docsMrIdx != -1 && !cursor.isNull(docsMrIdx)) {
            scheme.setDocumentsMr(cursor.getString(docsMrIdx));
        }
        int eligMrIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT_MR);
        if (eligMrIdx != -1 && !cursor.isNull(eligMrIdx)) {
            scheme.setEligibilityTextMr(cursor.getString(eligMrIdx));
        }

        return scheme;
    }
}
