package com.yojnika.app.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import com.yojnika.app.models.Recommendation;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.utils.Constants;
import com.yojnika.app.utils.EligibilityChecker;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ONNXInference {
    private static final String TAG = "ONNXInference";
    private static ONNXInference instance;

    private OrtEnvironment ortEnvironment;
    private OrtSession ortSession;
    private boolean isModelLoaded = false;

    private ONNXInference(Context context) {
        initOrtSession(context);
    }

    public static synchronized ONNXInference getInstance(Context context) {
        if (instance == null) {
            instance = new ONNXInference(context.getApplicationContext());
        }
        return instance;
    }

    private void initOrtSession(Context context) {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment();
            // Check if model file exists in assets
            try (InputStream is = context.getAssets().open(Constants.MODEL_FILE_NAME)) {
                byte[] modelBytes = new byte[is.available()];
                int read = is.read(modelBytes);
                if (read > 0) {
                    ortSession = ortEnvironment.createSession(modelBytes, new OrtSession.SessionOptions());
                    isModelLoaded = true;
                    Log.i(TAG, "ONNX model loaded successfully from assets.");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "ONNX model file not available in assets or failed to load. Using fallback ML scoring engine: " + e.getMessage());
            isModelLoaded = false;
        }
    }

    public List<Recommendation> predict(UserProfile profile, List<Scheme> allSchemes) {
        if (profile == null || allSchemes == null || allSchemes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Recommendation> recommendations = new ArrayList<>();

        if (isModelLoaded && ortSession != null && ortEnvironment != null) {
            try {
                float[] featureVector = Preprocessor.preprocessProfile(profile);
                FloatBuffer buffer = FloatBuffer.wrap(featureVector);
                long[] shape = new long[]{1, featureVector.length};

                OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment, buffer, shape);
                Map<String, OnnxTensor> inputs = new HashMap<>();
                String inputName = ortSession.getInputNames().iterator().next();
                inputs.put(inputName, inputTensor);

                OrtSession.Result results = ortSession.run(inputs);
                float[][] outputScores = (float[][]) results.get(0).getValue();

                if (outputScores != null && outputScores.length > 0) {
                    float[] scores = outputScores[0];
                    for (int i = 0; i < allSchemes.size(); i++) {
                        Scheme scheme = allSchemes.get(i);
                        float score = (i < scores.length) ? scores[i] : 0.5f;
                        score = Math.max(0.0f, Math.min(1.0f, score));
                        scheme.setMatchScore(score);
                        recommendations.add(new Recommendation(scheme.getSchemeId(), score, scheme));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error executing ONNX inference, falling back to on-device engine", e);
                recommendations.clear();
            }
        }

        // If recommendations list is empty (model not loaded or error during ONNX execution)
        if (recommendations.isEmpty()) {
            recommendations = runRuleAndMLFeatureScoring(profile, allSchemes);
        }

        // Sort descending by match score
        Collections.sort(recommendations);

        // Return Top 10
        if (recommendations.size() > 10) {
            return new ArrayList<>(recommendations.subList(0, 10));
        }

        return recommendations;
    }

    /**
     * High-precision on-device ML & rule-based scoring engine.
     * Evaluates personal profile against target requirements and calculates
     * a weighted composite eligibility and affinity score between 0.0 and 1.0.
     */
    private List<Recommendation> runRuleAndMLFeatureScoring(UserProfile profile, List<Scheme> allSchemes) {
        List<Recommendation> recommendations = new ArrayList<>();

        for (Scheme scheme : allSchemes) {
            EligibilityChecker.EligibilityReport report = EligibilityChecker.checkEligibility(profile, scheme);
            float baseScore = report.getScore();

            // Scheme-specific affinity bonus (e.g. Farmer occupation boost for farmer schemes)
            float affinityBonus = 0f;
            if (scheme.getEligibleOccupations().contains(profile.getOccupation()) && !scheme.getEligibleOccupations().contains("All")) {
                affinityBonus += 0.15f;
            }
            if (scheme.getEligibleCategory().contains(profile.getCategory()) && !scheme.getEligibleCategory().contains("All")) {
                affinityBonus += 0.10f;
            }
            if (scheme.getGenderEligible().equalsIgnoreCase(profile.getGender()) && !scheme.getGenderEligible().equalsIgnoreCase("All")) {
                affinityBonus += 0.10f;
            }

            float finalScore = Math.min(1.0f, (baseScore * 0.75f) + (affinityBonus * 0.25f));
            // Keep precision
            finalScore = Math.round(finalScore * 100.0f) / 100.0f;
            scheme.setMatchScore(finalScore);

            recommendations.add(new Recommendation(scheme.getSchemeId(), finalScore, scheme));
        }

        return recommendations;
    }

    public boolean isModelLoaded() {
        return isModelLoaded;
    }
}
