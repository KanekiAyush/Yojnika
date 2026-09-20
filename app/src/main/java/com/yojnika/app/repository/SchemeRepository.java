package com.yojnika.app.repository;

import android.content.Context;

import com.yojnika.app.database.SchemeDatabaseHelper;
import com.yojnika.app.models.Recommendation;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.services.SchemeMatcherService;
import com.yojnika.app.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchemeRepository {
    private static SchemeRepository instance;

    private final SchemeDatabaseHelper dbHelper;
    private final SharedPrefsManager prefsManager;
    private final SchemeMatcherService matcherService;
    private final ExecutorService executorService;

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    private SchemeRepository(Context context) {
        dbHelper = SchemeDatabaseHelper.getInstance(context);
        prefsManager = SharedPrefsManager.getInstance(context);
        matcherService = SchemeMatcherService.getInstance(context);
        executorService = Executors.newFixedThreadPool(2);
    }

    public static synchronized SchemeRepository getInstance(Context context) {
        if (instance == null) {
            instance = new SchemeRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getAllSchemes(DataCallback<List<Scheme>> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            List<Scheme> list = dbHelper.getAllSchemes(userId);
            callback.onDataLoaded(list);
        });
    }

    public void getSchemeById(int schemeId, DataCallback<Scheme> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            Scheme scheme = dbHelper.getSchemeById(schemeId, userId);
            callback.onDataLoaded(scheme);
        });
    }

    public void searchAndFilterSchemes(String query, String stateFilter, String typeFilter, String categoryFilter, DataCallback<List<Scheme>> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            List<Scheme> list = dbHelper.searchAndFilterSchemes(query, stateFilter, typeFilter, categoryFilter, userId);
            callback.onDataLoaded(list);
        });
    }

    public void searchAndFilterSchemesPaged(int page, int pageSize, String query, String stateFilter, String typeFilter, String categoryFilter, DataCallback<List<Scheme>> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            List<Scheme> list = dbHelper.searchAndFilterSchemesPaged(page, pageSize, query, stateFilter, typeFilter, categoryFilter, userId);
            callback.onDataLoaded(list);
        });
    }

    public void toggleBookmark(int schemeId, DataCallback<Boolean> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            boolean newState = dbHelper.toggleBookmark(schemeId, userId);
            callback.onDataLoaded(newState);
        });
    }

    public void getBookmarkedSchemes(DataCallback<List<Scheme>> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            List<Scheme> list = dbHelper.getBookmarkedSchemes(userId);
            callback.onDataLoaded(list);
        });
    }

    public void registerUser(String name, String email, String phone, String password, DataCallback<Long> callback) {
        executorService.execute(() -> {
            long id = dbHelper.registerUser(name, email, phone, password, null);
            callback.onDataLoaded(id);
        });
    }

    public void loginUser(String input, String password, DataCallback<Integer> callback) {
        executorService.execute(() -> {
            int userId = dbHelper.loginUser(input, password);
            callback.onDataLoaded(userId);
        });
    }

    public void migrateLegacyData(UserProfile profile, String email, String phone, String password, DataCallback<Integer> callback) {
        executorService.execute(() -> {
            String name = profile != null ? profile.getFullName() : "User";
            long userId = dbHelper.registerUser(name, email, phone, password, profile);
            if (userId != -1) {
                dbHelper.migrateLegacyBookmarks((int) userId);
                callback.onDataLoaded((int) userId);
            } else {
                int existingId = dbHelper.loginUser(email, password);
                callback.onDataLoaded(existingId);
            }
        });
    }

    public int getLoggedInUserId() {
        return prefsManager.getLoggedInUserId();
    }

    public void updateSchemeTranslations(Scheme scheme) {
        executorService.execute(() -> dbHelper.updateSchemeTranslations(scheme));
    }

    public void getRecommendedSchemes(UserProfile profile, DataCallback<List<Recommendation>> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            List<Scheme> allSchemes = dbHelper.getAllSchemes(userId);
            List<Scheme> matchedSchemes = matcherService.getRecommendations(profile, allSchemes);
            List<Recommendation> recommendations = new ArrayList<>();
            for (Scheme s : matchedSchemes) {
                recommendations.add(new Recommendation(s.getSchemeId(), s.getMatchScore(), s));
            }
            callback.onDataLoaded(recommendations);
        });
    }

    public void getMatchedSchemes(UserProfile profile, DataCallback<List<Scheme>> callback) {
        executorService.execute(() -> {
            int userId = prefsManager.getLoggedInUserId();
            List<Scheme> allSchemes = dbHelper.getAllSchemes(userId);
            List<Scheme> matchedSchemes = matcherService.getRecommendations(profile, allSchemes);
            callback.onDataLoaded(matchedSchemes);
        });
    }

    public UserProfile getUserProfile() {
        int userId = prefsManager.getLoggedInUserId();
        if (userId != -1) {
            UserProfile profile = dbHelper.getUserById(userId);
            if (profile != null) {
                // For now, some secondary fields like profile image path might still be in prefs
                return profile;
            }
        }
        return prefsManager.getUserProfile();
    }

    public void saveUserProfile(UserProfile profile) {
        int userId = prefsManager.getLoggedInUserId();
        if (userId != -1) {
            dbHelper.updateUserProfile(userId, profile);
        }
        prefsManager.saveUserProfile(profile);
    }

    public boolean hasUserProfile() {
        return prefsManager.hasUserProfile();
    }

    public boolean isMlModelLoaded() {
        return matcherService.isModelLoaded();
    }
}
