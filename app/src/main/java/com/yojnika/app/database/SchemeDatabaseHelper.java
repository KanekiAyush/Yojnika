package com.yojnika.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yojnika.app.models.Scheme;
import com.yojnika.app.utils.Constants;

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
        if (!dbFile.exists()) {
            try {
                if (dbFile.getParentFile() != null) {
                    dbFile.getParentFile().mkdirs();
                }
                InputStream is = mContext.getAssets().open(Constants.DATABASE_NAME);
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
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy prebuilt SQLite database from assets", e);
            }
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
                + SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED + " INTEGER DEFAULT 0"
                + ");";

        String CREATE_BOOKMARKS_TABLE = "CREATE TABLE IF NOT EXISTS " + SchemeContract.BookmarkEntry.TABLE_NAME + " ("
                + SchemeContract.BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " INTEGER UNIQUE,"
                + SchemeContract.BookmarkEntry.COLUMN_SAVED_TIMESTAMP + " INTEGER"
                + ");";

        db.execSQL(CREATE_SCHEMES_TABLE);
        db.execSQL(CREATE_BOOKMARKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SchemeContract.SchemeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SchemeContract.BookmarkEntry.TABLE_NAME);
        onCreate(db);
    }

    public List<Scheme> getAllSchemes() {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " = 1",
                    null,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schemes.add(cursorToScheme(cursor));
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

    public Scheme getSchemeById(int schemeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " = ?",
                    new String[]{String.valueOf(schemeId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursorToScheme(cursor);
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

    public List<Scheme> searchAndFilterSchemes(String query, String stateFilter, String typeFilter, String categoryFilter) {
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
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY).append(" LIKE '%All%')");
                String catPattern = "%" + categoryFilter + "%";
                selectionArgs.add(catPattern);
                selectionArgs.add(catPattern);
            }

            String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    selection.toString(),
                    args,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schemes.add(cursorToScheme(cursor));
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

    public List<Scheme> getBookmarkedSchemes() {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED + " = 1",
                    null,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schemes.add(cursorToScheme(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching bookmarked schemes", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public boolean toggleBookmark(int schemeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean newBookmarkState = false;
        try {
            Scheme scheme = getSchemeById(schemeId);
            if (scheme != null) {
                newBookmarkState = !scheme.isBookmarked();
                ContentValues values = new ContentValues();
                values.put(SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED, newBookmarkState ? 1 : 0);
                db.update(
                        SchemeContract.SchemeEntry.TABLE_NAME,
                        values,
                        SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " = ?",
                        new String[]{String.valueOf(schemeId)}
                );

                if (newBookmarkState) {
                    ContentValues bm = new ContentValues();
                    bm.put(SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID, schemeId);
                    bm.put(SchemeContract.BookmarkEntry.COLUMN_SAVED_TIMESTAMP, System.currentTimeMillis());
                    db.insertWithOnConflict(SchemeContract.BookmarkEntry.TABLE_NAME, null, bm, SQLiteDatabase.CONFLICT_REPLACE);
                } else {
                    db.delete(
                            SchemeContract.BookmarkEntry.TABLE_NAME,
                            SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " = ?",
                            new String[]{String.valueOf(schemeId)}
                    );
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling bookmark for schemeId: " + schemeId, e);
        }
        return newBookmarkState;
    }

    private Scheme cursorToScheme(Cursor cursor) {
        Scheme scheme = new Scheme();
        scheme.setSchemeId(cursor.getInt(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_SCHEME_ID)));
        scheme.setSchemeName(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME)));
        scheme.setSchemeDescription(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION)));

        int minAgeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MIN_AGE);
        if (minAgeIdx != -1 && !cursor.isNull(minAgeIdx)) {
            scheme.setMinAge(cursor.getInt(minAgeIdx));
        }

        int maxAgeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MAX_AGE);
        if (maxAgeIdx != -1 && !cursor.isNull(maxAgeIdx)) {
            scheme.setMaxAge(cursor.getInt(maxAgeIdx));
        }

        int genderIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE);
        if (genderIdx != -1) {
            scheme.setGenderEligible(cursor.getString(genderIdx));
        }

        int incomeLimitIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT);
        if (incomeLimitIdx != -1 && !cursor.isNull(incomeLimitIdx)) {
            scheme.setIncomeLimit(cursor.getLong(incomeLimitIdx));
        }

        int occIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS);
        if (occIdx != -1) {
            scheme.setEligibleOccupations(cursor.getString(occIdx));
        }

        int eduIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL);
        if (eduIdx != -1) {
            scheme.setMinEducationLevel(cursor.getString(eduIdx));
        }

        int catIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY);
        if (catIdx != -1) {
            scheme.setEligibleCategory(cursor.getString(catIdx));
        }

        int stateIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES);
        if (stateIdx != -1) {
            scheme.setEligibleStates(cursor.getString(stateIdx));
        }

        int maritalIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS);
        if (maritalIdx != -1) {
            scheme.setMaritalStatusRequirement(cursor.getString(maritalIdx));
        }

        int benefitsIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_BENEFITS);
        if (benefitsIdx != -1) {
            scheme.setBenefits(cursor.getString(benefitsIdx));
        }

        int appIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS);
        if (appIdx != -1) {
            scheme.setApplicationProcess(cursor.getString(appIdx));
        }

        int websiteIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE);
        if (websiteIdx != -1) {
            scheme.setOfficialWebsite(cursor.getString(websiteIdx));
        }

        int typeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE);
        if (typeIdx != -1) {
            scheme.setSchemeType(cursor.getString(typeIdx));
        }

        int createdDateIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_CREATED_DATE);
        if (createdDateIdx != -1) {
            scheme.setCreatedDate(cursor.getString(createdDateIdx));
        }

        int activeIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE);
        if (activeIdx != -1) {
            scheme.setActive(cursor.getInt(activeIdx) == 1);
        }

        int bookmarkedIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED);
        if (bookmarkedIdx != -1) {
            scheme.setBookmarked(cursor.getInt(bookmarkedIdx) == 1);
        }

        // New CSV fields
        int slugIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SLUG);
        if (slugIdx != -1) {
            scheme.setSlug(cursor.getString(slugIdx));
        }

        int docsIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_DOCUMENTS);
        if (docsIdx != -1) {
            scheme.setDocuments(cursor.getString(docsIdx));
        }

        int schemeCategoryIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_SCHEME_CATEGORY);
        if (schemeCategoryIdx != -1) {
            scheme.setSchemeCategory(cursor.getString(schemeCategoryIdx));
        }

        int tagsIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_TAGS);
        if (tagsIdx != -1) {
            scheme.setTags(cursor.getString(tagsIdx));
        }

        int eligTextIdx = cursor.getColumnIndex(SchemeContract.SchemeEntry.COLUMN_ELIGIBILITY_TEXT);
        if (eligTextIdx != -1) {
            scheme.setEligibilityText(cursor.getString(eligTextIdx));
        }

        return scheme;
    }
}
