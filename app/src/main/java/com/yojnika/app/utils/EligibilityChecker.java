package com.yojnika.app.utils;

import com.yojnika.app.models.CriteriaCheckResult;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class EligibilityChecker {

    public static class EligibilityReport {
        private final List<CriteriaCheckResult> criteriaResults;
        private final int matchedCount;
        private final int totalCount;
        private final float score; // 0.0 to 1.0
        private final String status; // "Eligible", "Partially Eligible", "Not Eligible"

        public EligibilityReport(List<CriteriaCheckResult> criteriaResults, int matchedCount, int totalCount, float score, String status) {
            this.criteriaResults = criteriaResults;
            this.matchedCount = matchedCount;
            this.totalCount = totalCount;
            this.score = score;
            this.status = status;
        }

        public List<CriteriaCheckResult> getCriteriaResults() {
            return criteriaResults;
        }

        public int getMatchedCount() {
            return matchedCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public float getScore() {
            return score;
        }

        public int getScorePercentage() {
            return Math.round(score * 100);
        }

        public String getStatus() {
            return status;
        }
    }

    public static EligibilityReport checkEligibility(UserProfile profile, Scheme scheme) {
        List<CriteriaCheckResult> results = new ArrayList<>();
        if (profile == null || scheme == null) {
            return new EligibilityReport(results, 0, 0, 0f, "Not Eligible");
        }

        int totalCount = 0;
        int matchedCount = 0;

        // 1. Age check
        if (scheme.getMinAge() != null || scheme.getMaxAge() != null) {
            totalCount++;
            int minAge = scheme.getMinAge() != null ? scheme.getMinAge() : 0;
            int maxAge = scheme.getMaxAge() != null ? scheme.getMaxAge() : 120;
            boolean ageMatches = profile.getAge() >= minAge && profile.getAge() <= maxAge;
            if (ageMatches) matchedCount++;

            String req = minAge + " - " + maxAge + " years";
            results.add(new CriteriaCheckResult("Age Requirement", profile.getAge() + " years", req, ageMatches));
        }

        // 2. Gender check
        if (scheme.getGenderEligible() != null && !scheme.getGenderEligible().equalsIgnoreCase("All")) {
            totalCount++;
            boolean genderMatches = scheme.getGenderEligible().equalsIgnoreCase(profile.getGender());
            if (genderMatches) matchedCount++;
            results.add(new CriteriaCheckResult("Gender", profile.getGender(), scheme.getGenderEligible() + " only", genderMatches));
        } else {
            results.add(new CriteriaCheckResult("Gender", profile.getGender(), "All genders eligible", true));
        }

        // 3. Income check
        if (scheme.getIncomeLimit() != null && scheme.getIncomeLimit() > 0) {
            totalCount++;
            boolean incomeMatches = profile.getAnnualIncome() <= scheme.getIncomeLimit();
            if (incomeMatches) matchedCount++;
            String req = "Up to ₹" + String.format("%,d", scheme.getIncomeLimit());
            String userInc = "₹" + String.format("%,d", profile.getAnnualIncome());
            results.add(new CriteriaCheckResult("Income Limit", userInc, req, incomeMatches));
        } else {
            results.add(new CriteriaCheckResult("Income Limit", "₹" + String.format("%,d", profile.getAnnualIncome()), "No strict limit", true));
        }

        // 4. Occupation check
        String occStr = scheme.getEligibleOccupations();
        if (occStr != null && !occStr.contains("All")) {
            totalCount++;
            boolean occMatches = occStr.contains(profile.getOccupation());
            if (occMatches) matchedCount++;
            String cleanOcc = occStr.replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
            results.add(new CriteriaCheckResult("Occupation", profile.getOccupation(), cleanOcc, occMatches));
        } else {
            results.add(new CriteriaCheckResult("Occupation", profile.getOccupation(), "All Occupations", true));
        }

        // 5. Category check
        String catStr = scheme.getEligibleCategory();
        if (catStr != null && !catStr.contains("All")) {
            totalCount++;
            boolean catMatches = catStr.contains(profile.getCategory());
            if (catMatches) matchedCount++;
            String cleanCat = catStr.replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
            results.add(new CriteriaCheckResult("Category / Caste", profile.getCategory(), cleanCat, catMatches));
        } else {
            results.add(new CriteriaCheckResult("Category / Caste", profile.getCategory(), "All Categories", true));
        }

        // 6. State check
        String stateStr = scheme.getEligibleStates();
        if (stateStr != null && !stateStr.contains("All India")) {
            totalCount++;
            boolean stateMatches = stateStr.contains(profile.getState());
            if (stateMatches) matchedCount++;
            String cleanState = stateStr.replace("[", "").replace("]", "").replace("\"", "").replace(",", ", ");
            results.add(new CriteriaCheckResult("State Eligibility", profile.getState(), cleanState, stateMatches));
        } else {
            results.add(new CriteriaCheckResult("State Eligibility", profile.getState(), "All India (Nationwide)", true));
        }

        // 7. Marital Status check
        if (scheme.getMaritalStatusRequirement() != null && !scheme.getMaritalStatusRequirement().equalsIgnoreCase("All")) {
            totalCount++;
            boolean maritalMatches = scheme.getMaritalStatusRequirement().equalsIgnoreCase(profile.getMaritalStatus());
            if (maritalMatches) matchedCount++;
            results.add(new CriteriaCheckResult("Marital Status", profile.getMaritalStatus(), scheme.getMaritalStatusRequirement(), maritalMatches));
        }

        // Calculate score
        float score;
        if (totalCount == 0) {
            score = 1.0f;
        } else {
            score = (float) matchedCount / (float) totalCount;
        }

        String status;
        if (score >= 0.99f) {
            status = "Eligible";
        } else if (score >= 0.50f) {
            status = "Partially Eligible";
        } else {
            status = "Not Eligible";
        }

        return new EligibilityReport(results, matchedCount, totalCount, score, status);
    }
}
