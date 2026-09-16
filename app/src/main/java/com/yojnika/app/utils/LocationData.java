package com.yojnika.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationData {

    public static final String[] STATE_ORDER = {
        "Select State",
        "Andhra Pradesh",
        "Arunachal Pradesh",
        "Assam",
        "Bihar",
        "Chhattisgarh",
        "Goa",
        "Gujarat",
        "Haryana",
        "Himachal Pradesh",
        "Jharkhand",
        "Karnataka",
        "Kerala",
        "Madhya Pradesh",
        "Maharashtra",
        "Manipur",
        "Meghalaya",
        "Mizoram",
        "Nagaland",
        "Odisha",
        "Punjab",
        "Rajasthan",
        "Sikkim",
        "Tamil Nadu",
        "Telangana",
        "Tripura",
        "Uttar Pradesh",
        "Uttarakhand",
        "West Bengal",
        "Delhi",
        "Jammu and Kashmir",
        "Ladakh",
        "Chandigarh",
        "Puducherry",
        "Andaman and Nicobar Islands",
        "Dadra and Nagar Haveli and Daman and Diu",
        "Lakshadweep",
        "Other"
    };

    public static List<String> getAllStates(String selectPrompt, String otherPrompt) {
        List<String> states = new ArrayList<>(Arrays.asList(STATE_ORDER));
        if (selectPrompt != null && !selectPrompt.isEmpty()) {
            states.set(0, selectPrompt);
        }
        if (otherPrompt != null && !otherPrompt.isEmpty()) {
            states.set(states.size() - 1, otherPrompt);
        }
        return states;
    }
}