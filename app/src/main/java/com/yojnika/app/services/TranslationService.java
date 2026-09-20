package com.yojnika.app.services;

import android.content.Context;
import android.util.LruCache;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

public class TranslationService {
    private static TranslationService instance;
    private final TranslationManager translationManager;
    private final LruCache<String, String> cache = new LruCache<>(500);

    private TranslationService() {
        translationManager = TranslationManager.getInstance();
    }

    public static synchronized TranslationService getInstance() {
        if (instance == null) {
            instance = new TranslationService();
        }
        return instance;
    }

    public void isModelDownloaded(String lang, OnSuccessListener<Boolean> listener) {
        translationManager.isModelDownloaded(lang, listener);
    }

    public void downloadModel(String lang, TranslationManager.ModelDownloadCallback callback) {
        translationManager.downloadModel(lang, callback);
    }

    public void translate(String text, String lang, TranslationManager.TranslationCallback callback) {
        if (text == null || text.trim().isEmpty() || "en".equalsIgnoreCase(lang)) {
            callback.onSuccess(text);
            return;
        }

        String cacheKey = lang + ":" + text;
        String cachedText = cache.get(cacheKey);
        if (cachedText != null) {
            callback.onSuccess(cachedText);
            return;
        }

        translationManager.translate(text, lang, new TranslationManager.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                cache.put(cacheKey, translatedText);
                callback.onSuccess(translatedText);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
