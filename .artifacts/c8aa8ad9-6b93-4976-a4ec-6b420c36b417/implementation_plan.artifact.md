# Implementation Plan - Dynamic Profile Form Enhancements

This plan outlines the changes required to implement dynamic dropdowns for State-District, Income Ranges, and expanded Occupation options in the `ProfileActivity`.

## User Review Required

> [!IMPORTANT]
> The list of districts is quite extensive. I will provide a comprehensive list for major states and a generic "Other" option for all states to allow manual entry as requested.

## Proposed Changes

### [Component: Data & Resources]

#### [NEW] [LocationData.java](file:///C:/Users/91866/Desktop/Yojnika/app/src/main/java/com/yojnika/app/utils/LocationData.java)
- Create a utility class to store a mapping of Indian States to their respective Districts.

#### [MODIFY] [strings.xml](file:///C:/Users/91866/Desktop/Yojnika/app/src/main/res/values/strings.xml)
- Expand `occupation_array` with detailed student categories and more professions.
- Add `income_range_array` with predefined ranges (₹0 - ₹8 Lakh+).
- Add new error strings and labels if necessary.

---

### [Component: UI Layout]

#### [MODIFY] [activity_profile.xml](file:///C:/Users/91866/Desktop/Yojnika/app/src/main/res/layout/activity_profile.xml)
- **Income Section**:
    - Replace the manual income `TextInputLayout` with a `Spinner` (`spinnerIncomeRange`).
    - Add a `TextInputLayout` (`tilCustomIncome`) for manual entry, set to `android:visibility="gone"`.
- **Location Section**:
    - Change `tilDistrict` to a `Spinner` (`spinnerDistrict`).
    - Add a `TextInputLayout` (`tilCustomDistrict`) for manual entry, set to `android:visibility="gone"`.

---

### [Component: Logic]

#### [MODIFY] [ProfileActivity.java](file:///C:/Users/91866/Desktop/Yojnika/app/src/main/java/com/yojnika/app/activities/ProfileActivity.java)
- Initialize new `Spinner` and `TextInputLayout` views.
- Implement dynamic update logic for `spinnerDistrict` based on `spinnerState` selection using `LocationData`.
- Implement visibility logic for "Other" options in Income and District dropdowns.
- Update `saveProfile()` to read from the correct source (Spinner or custom EditText).
- Update `loadExistingProfile()` to map saved values back to the dropdowns.

## Verification Plan

### Manual Verification
1. Open Profile screen.
2. Select different States and verify that the District list updates accordingly.
3. Select "Other" in District dropdown and verify the manual entry field appears.
4. Select different Income ranges.
5. Select "Other" in Income dropdown and verify the manual entry field appears.
6. Verify the expanded Occupation list.
7. Save the profile and verify that the data persists correctly (by reopening the profile).
