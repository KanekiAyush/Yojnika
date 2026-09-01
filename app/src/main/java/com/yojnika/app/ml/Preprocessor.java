package com.yojnika.app.ml;

import com.yojnika.app.models.UserProfile;
import com.yojnika.app.utils.Constants;

import java.util.Arrays;
import java.util.List;

public class Preprocessor {

    public static float[] preprocessProfile(UserProfile profile) {
        if (profile == null) {
            return new float[Constants.FEATURE_VECTOR_SIZE];
        }

        float[] features = new float[Constants.FEATURE_VECTOR_SIZE];
        int index = 0;

        // 1. Normalized Age (0.0 to 1.0)
        float normalizedAge = Math.max(0f, Math.min(1f, (float) profile.getAge() / 100.0f));
        features[index++] = normalizedAge;

        // 2. Normalized Income (0.0 to 1.0, scaled with max income cap 20 Lakhs)
        float normalizedIncome = Math.max(0f, Math.min(1f, (float) profile.getAnnualIncome() / 2000000.0f));
        features[index++] = normalizedIncome;

        // 3. Gender One-Hot (3 classes)
        List<String> genders = Arrays.asList(Constants.GENDERS);
        for (String g : genders) {
            features[index++] = g.equalsIgnoreCase(profile.getGender()) ? 1.0f : 0.0f;
        }

        // 4. Occupation One-Hot (6 classes)
        List<String> occupations = Arrays.asList(Constants.OCCUPATIONS);
        for (String occ : occupations) {
            features[index++] = occ.equalsIgnoreCase(profile.getOccupation()) ? 1.0f : 0.0f;
        }

        // 5. Education One-Hot (7 classes)
        List<String> educations = Arrays.asList(Constants.EDUCATION_LEVELS);
        for (String edu : educations) {
            features[index++] = edu.equalsIgnoreCase(profile.getEducationLevel()) ? 1.0f : 0.0f;
        }

        // 6. Category One-Hot (5 classes)
        List<String> categories = Arrays.asList(Constants.CATEGORIES);
        for (String cat : categories) {
            features[index++] = cat.equalsIgnoreCase(profile.getCategory()) ? 1.0f : 0.0f;
        }

        // 7. Marital Status (2 classes)
        List<String> maritalStatuses = Arrays.asList(Constants.MARITAL_STATUSES);
        for (String m : maritalStatuses) {
            features[index++] = m.equalsIgnoreCase(profile.getMaritalStatus()) ? 1.0f : 0.0f;
        }

        return features;
    }
}
