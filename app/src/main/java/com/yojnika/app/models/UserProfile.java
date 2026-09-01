package com.yojnika.app.models;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String fullName;
    private int age;
    private String gender;
    private long annualIncome;
    private String occupation;
    private String educationLevel;
    private String category;
    private String state;
    private String district;
    private String maritalStatus;

    public UserProfile() {
    }

    public UserProfile(String fullName, int age, String gender, long annualIncome,
                       String occupation, String educationLevel, String category,
                       String state, String district, String maritalStatus) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.annualIncome = annualIncome;
        this.occupation = occupation;
        this.educationLevel = educationLevel;
        this.category = category;
        this.state = state;
        this.district = district;
        this.maritalStatus = maritalStatus;
    }

    public String getFullName() {
        return fullName != null ? fullName : "";
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender != null ? gender : "All";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(long annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getOccupation() {
        return occupation != null ? occupation : "Any";
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getEducationLevel() {
        return educationLevel != null ? educationLevel : "Any";
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getState() {
        return state != null ? state : "All India";
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district != null ? district : "";
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMaritalStatus() {
        return maritalStatus != null ? maritalStatus : "All";
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public boolean isComplete() {
        return fullName != null && !fullName.trim().isEmpty() && age > 0 && district != null && !district.trim().isEmpty();
    }
}
