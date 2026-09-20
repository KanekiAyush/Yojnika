package com.yojnika.app.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

import java.util.HashMap;
import java.util.Map;

public class TranslationManager {
    private static final String TAG = "TranslationManager";
    private static TranslationManager instance;
    private final Map<String, Translator> translators = new HashMap<>();
    private final RemoteModelManager modelManager;

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(Exception e);
    }

    public interface ModelDownloadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private TranslationManager() {
        modelManager = RemoteModelManager.getInstance();
        Log.i(TAG, "TranslationManager initialized");
    }

    public static synchronized TranslationManager getInstance() {
        if (instance == null) {
            instance = new TranslationManager();
        }
        return instance;
    }

    public void translate(String text, String targetLangCode, TranslationCallback callback) {
        String sourceLang = TranslateLanguage.ENGLISH;
        String targetLang = getMlKitLangCode(targetLangCode);

        if (targetLang == null) {
            callback.onError(new Exception("Unsupported language: " + targetLangCode));
            return;
        }

        Translator translator = getTranslator(sourceLang, targetLang);
        translator.translate(text)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    public void isModelDownloaded(String langCode, OnSuccessListener<Boolean> listener) {
        Log.d(TAG, "isModelDownloaded check for: " + langCode);
        String lang = getMlKitLangCode(langCode);
        if (lang == null) {
            listener.onSuccess(false);
            return;
        }
        TranslateRemoteModel model = new TranslateRemoteModel.Builder(lang).build();
        modelManager.isModelDownloaded(model)
                .addOnSuccessListener(isDownloaded -> {
                    Log.d(TAG, "isModelDownloaded result for " + langCode + ": " + isDownloaded);
                    listener.onSuccess(isDownloaded);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "isModelDownloaded check failed for " + langCode, e);
                    listener.onSuccess(false);
                });
    }

    public void downloadModel(String langCode, ModelDownloadCallback callback) {
        try {
            String lang = getMlKitLangCode(langCode);
            if (lang == null) {
                callback.onFailure(new Exception("Unsupported language: " + langCode));
                return;
            }

            Translator translator = getTranslator(TranslateLanguage.ENGLISH, lang);
            DownloadConditions conditions = new DownloadConditions.Builder()
                    .build();

            Log.d(TAG, "Calling downloadModelIfNeeded for " + langCode);
            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, langCode + " model download task success");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, langCode + " model download task failure", e);
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in downloadModel for " + langCode, e);
            callback.onFailure(e);
        }
    }

    private synchronized Translator getTranslator(String sourceLang, String targetLang) {
        String key = sourceLang + "_" + targetLang;
        if (translators.containsKey(key)) {
            return translators.get(key);
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build();
        Translator translator = Translation.getClient(options);
        translators.put(key, translator);
        return translator;
    }

    private String getMlKitLangCode(String langCode) {
        if (langCode == null) return null;
        switch (langCode.toLowerCase()) {
            case "hi": return TranslateLanguage.HINDI;
            case "mr": return TranslateLanguage.MARATHI;
            case "en": return TranslateLanguage.ENGLISH;
            default: return null;
        }
    }
    
    public void close() {
        for (Translator translator : translators.values()) {
            translator.close();
        }
        translators.clear();
    }
}
