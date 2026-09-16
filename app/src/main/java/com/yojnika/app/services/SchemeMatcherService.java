package com.yojnika.app.services;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import com.yojnika.app.models.Recommendation;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.utils.EligibilityChecker;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchemeMatcherService {
    private static final String TAG = "SchemeMatcherService";
    private static final String MODEL_PATH = "models/all-MiniLM-L6-v2-int8.onnx";
    private static final String VOCAB_PATH = "models/vocab.txt";
    private static final String EMBEDDINGS_PATH = "data/scheme_embeddings.bin";
    private static final int EMBEDDING_DIM = 384;
    private static final int MAX_SEQ_LEN = 128;

    private static SchemeMatcherService instance;

    private final Context context;
    private final ExecutorService executorService;
    private OrtEnvironment ortEnvironment;
    private OrtSession ortSession;
    private Tokenizer tokenizer;
    private boolean isModelLoaded = false;

    // Cache precomputed scheme embeddings: Scheme ID -> float[384]
    private final Map<Integer, float[]> precomputedEmbeddings = new HashMap<>();
    private final LruCache<String, float[]> profileEmbeddingCache = new LruCache<>(50);

    public interface RecommendationCallback {
        void onRecommendationsReady(List<Scheme> recommendations);
    }

    private SchemeMatcherService(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        initModelAndEmbeddings();
    }

    public static synchronized SchemeMatcherService getInstance(Context context) {
        if (instance == null) {
            instance = new SchemeMatcherService(context.getApplicationContext());
        }
        return instance;
    }

    private void initModelAndEmbeddings() {
        executorService.execute(() -> {
            try {
                tokenizer = new Tokenizer(context, VOCAB_PATH);
                ortEnvironment = OrtEnvironment.getEnvironment();

                try (InputStream is = context.getAssets().open(MODEL_PATH)) {
                    byte[] modelBytes = new byte[is.available()];
                    int read = is.read(modelBytes);
                    if (read > 0) {
                        ortSession = ortEnvironment.createSession(modelBytes, new OrtSession.SessionOptions());
                        isModelLoaded = true;
                        Log.i(TAG, "ONNX model loaded successfully.");
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "ONNX model init warning (will use rule-based fallback): " + e.getMessage());
                isModelLoaded = false;
            }

            loadPrecomputedEmbeddings();
        });
    }

    private void loadPrecomputedEmbeddings() {
        try (InputStream is = context.getAssets().open(EMBEDDINGS_PATH)) {
            byte[] bytes = new byte[is.available()];
            int read = is.read(bytes);
            if (read > 0) {
                ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                int totalSchemes = bb.getInt();
                for (int i = 0; i < totalSchemes; i++) {
                    int schemeId = bb.getInt();
                    float[] emb = new float[EMBEDDING_DIM];
                    for (int d = 0; d < EMBEDDING_DIM; d++) {
                        emb[d] = bb.getFloat();
                    }
                    precomputedEmbeddings.put(schemeId, emb);
                }
                Log.i(TAG, "Loaded " + precomputedEmbeddings.size() + " precomputed scheme embeddings.");
            }
        } catch (Exception e) {
            Log.w(TAG, "No precomputed binary embeddings file found, generating dynamically or using fallbacks: " + e.getMessage());
        }
    }

    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[EMBEDDING_DIM];
        }

        float[] cached = profileEmbeddingCache.get(text);
        if (cached != null) {
            return cached;
        }

        if (!isModelLoaded || ortSession == null || ortEnvironment == null || tokenizer == null || !tokenizer.isInitialized()) {
            return new float[EMBEDDING_DIM];
        }

        try {
            Tokenizer.TokenResult tokenResult = tokenizer.encode(text, MAX_SEQ_LEN);
            long[] shape = new long[]{1, MAX_SEQ_LEN};

            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(ortEnvironment, LongBufferWrap(tokenResult.inputIds), shape);
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(ortEnvironment, LongBufferWrap(tokenResult.attentionMask), shape);
            OnnxTensor tokenTypeTensor = OnnxTensor.createTensor(ortEnvironment, LongBufferWrap(tokenResult.tokenTypeIds), shape);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);
            inputs.put("token_type_ids", tokenTypeTensor);

            OrtSession.Result results = ortSession.run(inputs);
            // Expected output: [1, seq_len, 384]
            Object outputObj = results.get(0).getValue();
            float[] embedding = new float[EMBEDDING_DIM];

            if (outputObj instanceof float[][][]) {
                float[][][] lastHiddenState = (float[][][]) outputObj;
                int seqLen = lastHiddenState[0].length;
                int count = 0;

                // Mean pooling over token embeddings
                for (int s = 0; s < seqLen; s++) {
                    if (tokenResult.attentionMask[s] == 1L) {
                        for (int d = 0; d < EMBEDDING_DIM; d++) {
                            embedding[d] += lastHiddenState[0][s][d];
                        }
                        count++;
                    }
                }
                if (count > 0) {
                    for (int d = 0; d < EMBEDDING_DIM; d++) {
                        embedding[d] /= count;
                    }
                }
            }

            // Normalize vector
            normalize(embedding);
            profileEmbeddingCache.put(text, embedding);
            return embedding;
        } catch (Exception e) {
            Log.e(TAG, "Error in embed(): ", e);
            return new float[EMBEDDING_DIM];
        }
    }

    private java.nio.LongBuffer LongBufferWrap(long[] array) {
        java.nio.LongBuffer buffer = java.nio.LongBuffer.allocate(array.length);
        buffer.put(array);
        buffer.flip();
        return buffer;
    }

    private void normalize(float[] vec) {
        float norm = 0.0f;
        for (float v : vec) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vec.length; i++) {
                vec[i] /= norm;
            }
        }
    }

    private float cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length) return 0.0f;
        float dot = 0.0f;
        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
        }
        return dot;
    }

    public void getRecommendationsAsync(UserProfile profile, List<Scheme> allSchemes, RecommendationCallback callback) {
        executorService.execute(() -> {
            List<Scheme> results = getRecommendations(profile, allSchemes);
            callback.onRecommendationsReady(results);
        });
    }

    public List<Scheme> getRecommendations(UserProfile profile, List<Scheme> allSchemes) {
        if (profile == null || !profile.isComplete() || allSchemes == null || allSchemes.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. HARD RULE FILTER
        List<Scheme> eligibleSchemes = new ArrayList<>();
        for (Scheme scheme : allSchemes) {
            EligibilityChecker.EligibilityReport report = EligibilityChecker.checkEligibility(profile, scheme);
            if (report.isEligible()) {
                scheme.setMatchScore(report.getScore());
                eligibleSchemes.add(scheme);
            }
        }

        if (eligibleSchemes.isEmpty()) {
            // Fallback to top-scoring schemes if strict filter yields 0
            for (Scheme scheme : allSchemes) {
                EligibilityChecker.EligibilityReport report = EligibilityChecker.checkEligibility(profile, scheme);
                scheme.setMatchScore(report.getScore());
                if (report.getScore() >= 0.4f) {
                    eligibleSchemes.add(scheme);
                }
            }
        }

        // 2. ONNX SEMANTIC MATCHING & RANKING
        String profileText = buildProfileText(profile);
        float[] profileVector = embed(profileText);

        List<Recommendation> recommendations = new ArrayList<>();
        for (Scheme scheme : eligibleSchemes) {
            float sim = 0.0f;
            float[] schemeVector = precomputedEmbeddings.get(scheme.getSchemeId());

            if (schemeVector != null) {
                sim = cosineSimilarity(profileVector, schemeVector);
            } else {
                // Fallback scoring blend with eligibility checker score
                sim = scheme.getMatchScore();
            }

            float compositeScore = (scheme.getMatchScore() * 0.6f) + (sim * 0.4f);
            compositeScore = Math.max(0.0f, Math.min(1.0f, compositeScore));
            scheme.setMatchScore(compositeScore);
            recommendations.add(new Recommendation(scheme.getSchemeId(), compositeScore, scheme));
        }

        Collections.sort(recommendations);

        List<Scheme> sortedSchemes = new ArrayList<>();
        for (Recommendation r : recommendations) {
            sortedSchemes.add(r.getScheme());
        }

        return sortedSchemes;
    }

    private String buildProfileText(UserProfile profile) {
        return "Age " + profile.getAge() + " year old " + profile.getGender() +
                " from " + profile.getState() +
                ", occupation " + profile.getOccupation() +
                ", education " + profile.getEducationLevel() +
                ", category " + profile.getCategory() +
                ", annual income " + profile.getAnnualIncome();
    }

    public boolean isModelLoaded() {
        return isModelLoaded;
    }
}
