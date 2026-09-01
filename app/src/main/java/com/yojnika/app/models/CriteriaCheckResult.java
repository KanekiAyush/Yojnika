package com.yojnika.app.models;

public class CriteriaCheckResult {
    private String criteriaName;
    private String userValue;
    private String requiredValue;
    private boolean isMatched;

    public CriteriaCheckResult(String criteriaName, String userValue, String requiredValue, boolean isMatched) {
        this.criteriaName = criteriaName;
        this.userValue = userValue;
        this.requiredValue = requiredValue;
        this.isMatched = isMatched;
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public String getUserValue() {
        return userValue;
    }

    public String getRequiredValue() {
        return requiredValue;
    }

    public boolean isMatched() {
        return isMatched;
    }
}
