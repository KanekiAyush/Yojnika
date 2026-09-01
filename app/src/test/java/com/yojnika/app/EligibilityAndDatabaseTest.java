package com.yojnika.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.yojnika.app.database.SchemeDatabaseHelper;
import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;
import com.yojnika.app.utils.EligibilityChecker;
import com.yojnika.app.utils.SharedPrefsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EligibilityAndDatabaseTest {

    private Context context;
    private SchemeDatabaseHelper dbHelper;
    private SharedPrefsManager prefsManager;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = SchemeDatabaseHelper.getInstance(context);
        prefsManager = SharedPrefsManager.getInstance(context);
    }

    @Test
    public void testDatabaseInitializationAndSchemeCount() {
        List<Scheme> schemes = dbHelper.getAllSchemes();
        assertNotNull(schemes);
        assertTrue("Schemes count should be >= 15", schemes.size() >= 15);
    }

    @Test
    public void testUserProfilePersistence() {
        UserProfile profile = new UserProfile(
                "Aarav Sharma", 28, "Male", 250000L,
                "Farmer", "Graduate", "General",
                "Maharashtra", "Pune", "Married"
        );
        prefsManager.saveUserProfile(profile);

        assertTrue(prefsManager.hasUserProfile());
        UserProfile loaded = prefsManager.getUserProfile();
        assertNotNull(loaded);
        assertEquals("Aarav Sharma", loaded.getFullName());
        assertEquals(28, loaded.getAge());
        assertEquals("Farmer", loaded.getOccupation());
        assertEquals("Maharashtra", loaded.getState());
    }

    @Test
    public void testEligibilityCheckerPMKisanFarmer() {
        UserProfile farmerProfile = new UserProfile(
                "Ramesh Patil", 35, "Male", 150000L,
                "Farmer", "10th Pass", "OBC",
                "Maharashtra", "Nashik", "Married"
        );

        Scheme pmKisan = dbHelper.getSchemeById(1); // PM Kisan
        assertNotNull(pmKisan);

        EligibilityChecker.EligibilityReport report = EligibilityChecker.checkEligibility(farmerProfile, pmKisan);
        assertNotNull(report);
        assertEquals("Eligible", report.getStatus());
        assertEquals(1.0f, report.getScore(), 0.01f);
    }

    @Test
    public void testBookmarkToggle() {
        Scheme scheme = dbHelper.getSchemeById(1);
        assertNotNull(scheme);
        boolean initialStatus = scheme.isBookmarked();

        boolean updated = dbHelper.toggleBookmark(1);
        assertEquals(!initialStatus, updated);

        List<Scheme> bookmarked = dbHelper.getBookmarkedSchemes();
        if (updated) {
            assertTrue(bookmarked.stream().anyMatch(s -> s.getSchemeId() == 1));
        }
    }
}
