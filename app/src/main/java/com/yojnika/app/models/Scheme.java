package com.yojnika.app.models;

import java.io.Serializable;

public class Scheme implements Serializable {
    private int schemeId;
    private String schemeName;
    private String schemeDescription;
    private Integer minAge;
    private Integer maxAge;
    private String genderEligible;
    private Long incomeLimit;
    private String eligibleOccupations; // JSON Array or comma separated
    private String minEducationLevel;
    private String eligibleCategory;    // JSON Array or comma separated
    private String eligibleStates;      // JSON Array or comma separated
    private String maritalStatusRequirement;
    private String benefits;
    private String applicationProcess;
    private String officialWebsite;
    private String schemeType;          // "Central Government" or "State Government"
    private String slug;
    private String documents;
    private String schemeCategory;
    private String tags;
    private String eligibilityText;
    private String createdDate;
    private boolean isActive;
    private boolean isBookmarked;
    private float matchScore;          // Used for recommendation sorting

    public Scheme() {
    }

    public Scheme(int schemeId, String schemeName, String schemeDescription,
                  Integer minAge, Integer maxAge, String genderEligible,
                  Long incomeLimit, String eligibleOccupations, String minEducationLevel,
                  String eligibleCategory, String eligibleStates, String maritalStatusRequirement,
                  String benefits, String applicationProcess, String officialWebsite,
                  String schemeType, String createdDate, boolean isActive, boolean isBookmarked) {
        this.schemeId = schemeId;
        this.schemeName = schemeName;
        this.schemeDescription = schemeDescription;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.genderEligible = genderEligible;
        this.incomeLimit = incomeLimit;
        this.eligibleOccupations = eligibleOccupations;
        this.minEducationLevel = minEducationLevel;
        this.eligibleCategory = eligibleCategory;
        this.eligibleStates = eligibleStates;
        this.maritalStatusRequirement = maritalStatusRequirement;
        this.benefits = benefits;
        this.applicationProcess = applicationProcess;
        this.officialWebsite = officialWebsite;
        this.schemeType = schemeType;
        this.createdDate = createdDate;
        this.isActive = isActive;
        this.isBookmarked = isBookmarked;
    }

    public int getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(int schemeId) {
        this.schemeId = schemeId;
    }

    public String getSchemeName() {
        return schemeName != null ? schemeName : "";
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getSchemeDescription() {
        return schemeDescription != null ? schemeDescription : "";
    }

    public void setSchemeDescription(String schemeDescription) {
        this.schemeDescription = schemeDescription;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getGenderEligible() {
        return genderEligible != null ? genderEligible : "All";
    }

    public void setGenderEligible(String genderEligible) {
        this.genderEligible = genderEligible;
    }

    public Long getIncomeLimit() {
        return incomeLimit;
    }

    public void setIncomeLimit(Long incomeLimit) {
        this.incomeLimit = incomeLimit;
    }

    public String getEligibleOccupations() {
        return eligibleOccupations != null ? eligibleOccupations : "[\"All\"]";
    }

    public void setEligibleOccupations(String eligibleOccupations) {
        this.eligibleOccupations = eligibleOccupations;
    }

    public String getMinEducationLevel() {
        return minEducationLevel;
    }

    public void setMinEducationLevel(String minEducationLevel) {
        this.minEducationLevel = minEducationLevel;
    }

    public String getEligibleCategory() {
        return eligibleCategory != null ? eligibleCategory : "[\"All\"]";
    }

    public void setEligibleCategory(String eligibleCategory) {
        this.eligibleCategory = eligibleCategory;
    }

    public String getEligibleStates() {
        return eligibleStates != null ? eligibleStates : "[\"All India\"]";
    }

    public void setEligibleStates(String eligibleStates) {
        this.eligibleStates = eligibleStates;
    }

    public String getMaritalStatusRequirement() {
        return maritalStatusRequirement != null ? maritalStatusRequirement : "All";
    }

    public void setMaritalStatusRequirement(String maritalStatusRequirement) {
        this.maritalStatusRequirement = maritalStatusRequirement;
    }

    public String getBenefits() {
        return benefits != null ? benefits : "";
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getApplicationProcess() {
        return applicationProcess != null ? applicationProcess : "";
    }

    public void setApplicationProcess(String applicationProcess) {
        this.applicationProcess = applicationProcess;
    }

    public String getOfficialWebsite() {
        return officialWebsite != null ? officialWebsite : "";
    }

    public void setOfficialWebsite(String officialWebsite) {
        this.officialWebsite = officialWebsite;
    }

    public String getSchemeType() {
        return schemeType != null ? schemeType : "Central Government";
    }

    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public float getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(float matchScore) {
        this.matchScore = matchScore;
    }

    public String getSlug() {
        return slug != null ? slug : "";
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDocuments() {
        return documents != null ? documents : "";
    }

    public void setDocuments(String documents) {
        this.documents = documents;
    }

    public String getSchemeCategory() {
        return schemeCategory != null ? schemeCategory : "";
    }

    public void setSchemeCategory(String schemeCategory) {
        this.schemeCategory = schemeCategory;
    }

    public String getTags() {
        return tags != null ? tags : "";
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getEligibilityText() {
        return eligibilityText != null ? eligibilityText : "";
    }

    public void setEligibilityText(String eligibilityText) {
        this.eligibilityText = eligibilityText;
    }
}
