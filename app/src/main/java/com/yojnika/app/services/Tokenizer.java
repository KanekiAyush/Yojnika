package com.yojnika.app.services;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokenizer {
    private static final String TAG = "Tokenizer";
    private static final String UNK_TOKEN = "[UNK]";
    private static final String CLS_TOKEN = "[CLS]";
    private static final String SEP_TOKEN = "[SEP]";

    private final Map<String, Integer> vocab = new HashMap<>();
    private final int unkId;
    private final int clsId;
    private final int sepId;
    private boolean isInitialized = false;

    public static class TokenResult {
        public final long[] inputIds;
        public final long[] attentionMask;
        public final long[] tokenTypeIds;

        public TokenResult(long[] inputIds, long[] attentionMask, long[] tokenTypeIds) {
            this.inputIds = inputIds;
            this.attentionMask = attentionMask;
            this.tokenTypeIds = tokenTypeIds;
        }
    }

    public Tokenizer(Context context, String vocabPath) {
        int tempUnk = 100, tempCls = 101, tempSep = 102;
        try (InputStream is = context.getAssets().open(vocabPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                vocab.put(line, index);
                if (UNK_TOKEN.equals(line)) tempUnk = index;
                if (CLS_TOKEN.equals(line)) tempCls = index;
                if (SEP_TOKEN.equals(line)) tempSep = index;
                index++;
            }
            isInitialized = true;
            Log.i(TAG, "Tokenizer loaded vocab with size: " + vocab.size());
        } catch (Exception e) {
            Log.e(TAG, "Failed to load vocab file: " + vocabPath, e);
        }
        unkId = tempUnk;
        clsId = tempCls;
        sepId = tempSep;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public TokenResult encode(String text, int maxSeqLength) {
        List<Long> ids = new ArrayList<>();
        ids.add((long) clsId);

        if (text != null && !text.trim().isEmpty()) {
            String cleaned = text.toLowerCase().trim();
            String[] words = cleaned.split("\\s+");

            for (String word : words) {
                String cleanWord = word.replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "");
                if (cleanWord.isEmpty()) cleanWord = word;

                List<Integer> wordPieceIds = wordPieceTokenize(cleanWord);
                for (int id : wordPieceIds) {
                    if (ids.size() >= maxSeqLength - 1) break;
                    ids.add((long) id);
                }
                if (ids.size() >= maxSeqLength - 1) break;
            }
        }

        ids.add((long) sepId);

        int seqLen = ids.size();
        long[] inputIds = new long[maxSeqLength];
        long[] attentionMask = new long[maxSeqLength];
        long[] tokenTypeIds = new long[maxSeqLength];

        for (int i = 0; i < maxSeqLength; i++) {
            if (i < seqLen) {
                inputIds[i] = ids.get(i);
                attentionMask[i] = 1L;
            } else {
                inputIds[i] = 0L;
                attentionMask[i] = 0L;
            }
            tokenTypeIds[i] = 0L;
        }

        return new TokenResult(inputIds, attentionMask, tokenTypeIds);
    }

    private List<Integer> wordPieceTokenize(String word) {
        List<Integer> tokens = new ArrayList<>();
        if (vocab.containsKey(word)) {
            tokens.add(vocab.get(word));
            return tokens;
        }

        int start = 0;
        int length = word.length();
        while (start < length) {
            int end = length;
            Integer curSubwordId = null;
            while (start < end) {
                String subword = word.substring(start, end);
                if (start > 0) {
                    subword = "##" + subword;
                }
                if (vocab.containsKey(subword)) {
                    curSubwordId = vocab.get(subword);
                    break;
                }
                end--;
            }
            if (curSubwordId == null) {
                tokens.add(unkId);
                break;
            } else {
                tokens.add(curSubwordId);
                start = end;
            }
        }
        return tokens;
    }
}
