package com.yojnika.app.models;

public class Recommendation implements Comparable<Recommendation> {
    private int schemeId;
    private float score; // 0.0 to 1.0
    private Scheme scheme;

    public Recommendation(int schemeId, float score) {
        this.schemeId = schemeId;
        this.score = score;
    }

    public Recommendation(int schemeId, float score, Scheme scheme) {
        this.schemeId = schemeId;
        this.score = score;
        this.scheme = scheme;
    }

    public int getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(int schemeId) {
        this.schemeId = schemeId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public int getScorePercentage() {
        return Math.round(score * 100);
    }

    @Override
    public int compareTo(Recommendation other) {
        // Descending order of score
        return Float.compare(other.score, this.score);
    }
}
