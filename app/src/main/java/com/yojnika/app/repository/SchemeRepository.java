package com.yojnika.app.repository;

import android.content.Context;

import com.yojnika.app.database.SchemeDatabaseHelper;
import com.yojnika.app.ml.ONNXInference;
import com.yojnika.app.models.Recommendation;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.utils.SharedPrefsManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchemeRepository {
    private static SchemeRepository instance;

    private final SchemeDatabaseHelper dbHelper;
    private final SharedPrefsManager prefsManager;
    private final ONNXInference onnxInference;
    private final ExecutorService executorService;

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    private SchemeRepository(Context context) {
        dbHelper = SchemeDatabaseHelper.getInstance(context);
        prefsManager = SharedPrefsManager.getInstance(context);
        onnxInference = ONNXInference.getInstance(context);
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
            List<Scheme> list = dbHelper.getAllSchemes();
            callback.onDataLoaded(list);
        });
    }

    public void getSchemeById(int schemeId, DataCallback<Scheme> callback) {
        executorService.execute(() -> {
            Scheme scheme = dbHelper.getSchemeById(schemeId);
            callback.onDataLoaded(scheme);
        });
    }

    public void searchAndFilterSchemes(String query, String stateFilter, String typeFilter, String categoryFilter, DataCallback<List<Scheme>> callback) {
        executorService.execute(() -> {
            List<Scheme> list = dbHelper.searchAndFilterSchemes(query, stateFilter, typeFilter, categoryFilter);
            callback.onDataLoaded(list);
        });
    }

    public void getBookmarkedSchemes(DataCallback<List<Scheme>> callback) {
        executorService.execute(() -> {
            List<Scheme> list = dbHelper.getBookmarkedSchemes();
            callback.onDataLoaded(list);
        });
    }

    public void toggleBookmark(int schemeId, DataCallback<Boolean> callback) {
        executorService.execute(() -> {
            boolean newState = dbHelper.toggleBookmark(schemeId);
            callback.onDataLoaded(newState);
        });
    }

    public void getRecommendedSchemes(UserProfile profile, DataCallback<List<Recommendation>> callback) {
        executorService.execute(() -> {
            List<Scheme> allSchemes = dbHelper.getAllSchemes();
            List<Recommendation> recommendations = onnxInference.predict(profile, allSchemes);
            callback.onDataLoaded(recommendations);
        });
    }

    public UserProfile getUserProfile() {
        return prefsManager.getUserProfile();
    }

    public void saveUserProfile(UserProfile profile) {
        prefsManager.saveUserProfile(profile);
    }

    public boolean hasUserProfile() {
        return prefsManager.hasUserProfile();
    }

    public boolean isMlModelLoaded() {
        return onnxInference.isModelLoaded();
    }
}
