package com.yojnika.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yojnika.app.models.Scheme;
import com.yojnika.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SchemeDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "SchemeDatabaseHelper";
    private static SchemeDatabaseHelper instance;

    public static synchronized SchemeDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SchemeDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public SchemeDatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCHEMES_TABLE = "CREATE TABLE " + SchemeContract.SchemeEntry.TABLE_NAME + " ("
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " INTEGER PRIMARY KEY,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " TEXT NOT NULL,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_MIN_AGE + " INTEGER,"
                + SchemeContract.SchemeEntry.COLUMN_MAX_AGE + " INTEGER,"
                + SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT + " INTEGER,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_BENEFITS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_CREATED_DATE + " TEXT,"
                + SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED + " INTEGER DEFAULT 0"
                + ");";

        String CREATE_BOOKMARKS_TABLE = "CREATE TABLE " + SchemeContract.BookmarkEntry.TABLE_NAME + " ("
                + SchemeContract.BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " INTEGER UNIQUE,"
                + SchemeContract.BookmarkEntry.COLUMN_SAVED_TIMESTAMP + " INTEGER"
                + ");";

        db.execSQL(CREATE_SCHEMES_TABLE);
        db.execSQL(CREATE_BOOKMARKS_TABLE);

        seedDefaultSchemes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SchemeContract.SchemeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SchemeContract.BookmarkEntry.TABLE_NAME);
        onCreate(db);
    }

    private void seedDefaultSchemes(SQLiteDatabase db) {
        List<Scheme> sampleSchemes = new ArrayList<>();

        // 1. PM-KISAN
        sampleSchemes.add(new Scheme(
                1,
                "PM Kisan Samman Nidhi (PM-KISAN)",
                "A central sector scheme to provide income support to all landholding farmer families in the country to supplement their financial needs.",
                18, 75, "All", null,
                "[\"Farmer\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Financial benefit of ₹6,000 per year in three equal 4-monthly installments directly into bank accounts.",
                "1. Visit pmkisan.gov.in\n2. Click on 'New Farmer Registration'\n3. Enter Aadhaar number and state\n4. Submit land revenue details and bank credentials.",
                "https://pmkisan.gov.in",
                "Central Government", "2019-02-24", true, false
        ));

        // 2. PM Awas Yojana
        sampleSchemes.add(new Scheme(
                2,
                "Pradhan Mantri Awas Yojana (PMAY-Gramin & Urban)",
                "Provides pucca houses with basic amenities to all eligible houseless households and those living in kutcha and dilapidated houses.",
                18, 70, "All", 300000L,
                "[\"All\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Financial assistance of up to ₹1.20 lakh in plain areas and ₹1.30 lakh in hilly/difficult areas along with 90/95 days of unskilled labor wage under MGNREGS.",
                "1. Apply via Gram Panchayat or official portal pmayg.nic.in / pmaymis.gov.in\n2. Submit income certificate, Aadhaar, and bank passbook\n3. Verification by local administration.",
                "https://pmaymis.gov.in",
                "Central Government", "2015-06-25", true, false
        ));

        // 3. Ayushman Bharat PM-JAY
        sampleSchemes.add(new Scheme(
                3,
                "Ayushman Bharat - PM Jan Arogya Yojana (PM-JAY)",
                "World's largest government-funded health insurance assurance scheme providing cashless hospitalisation cover to vulnerable families.",
                1, 100, "All", 250000L,
                "[\"All\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Cashless health insurance coverage of up to ₹5,00,000 per family per year for secondary and tertiary care hospitalization across empanelled hospitals.",
                "1. Check eligibility at mera.pmjay.gov.in\n2. Visit nearest CSC or Ayushman Mitra at empanelled hospital\n3. Complete e-KYC and download Ayushman Golden Card.",
                "https://pmjay.gov.in",
                "Central Government", "2018-09-23", true, false
        ));

        // 4. Sukanya Samriddhi Yojana
        sampleSchemes.add(new Scheme(
                4,
                "Sukanya Samriddhi Yojana (SSY)",
                "A small deposit savings scheme specifically for the girl child launched under the 'Beti Bachao Beti Padhao' campaign.",
                18, 60, "Female", null,
                "[\"All\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "High attractive interest rate (8.2% p.a.), Section 80C tax exemption, and triple tax-free maturity amount for girl education and marriage.",
                "1. Visit any Post Office or authorized commercial bank branch\n2. Fill SSY account opening form with girl child's birth certificate\n3. Deposit initial minimum amount of ₹250.",
                "https://www.nsiindia.gov.in",
                "Central Government", "2015-01-22", true, false
        ));

        // 5. Pradhan Mantri Jan Dhan Yojana
        sampleSchemes.add(new Scheme(
                5,
                "Pradhan Mantri Jan Dhan Yojana (PMJDY)",
                "National mission for financial inclusion ensuring access to financial services like basic banking accounts, remittance, credit, insurance, and pension.",
                18, 65, "All", null,
                "[\"All\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Zero balance savings account, free RuPay debit card with ₹2 lakh accidental insurance, and overdraft facility up to ₹10,000.",
                "1. Visit nearest bank branch or Bank Mitra\n2. Submit account opening form with Aadhaar or Voter ID\n3. Instant account activation.",
                "https://pmjdy.gov.in",
                "Central Government", "2014-08-28", true, false
        ));

        // 6. Skill India Mission
        sampleSchemes.add(new Scheme(
                6,
                "Pradhan Mantri Kaushal Vikas Yojana (PMKVY)",
                "Skill certification scheme aimed at enabling Indian youth to take up industry-relevant skill training to secure a better livelihood.",
                18, 45, "All", null,
                "[\"Student\",\"Unemployed\"]", "10th Pass",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "100% free industry-aligned skill training, government-recognized NSQF certification, stipend support, and placement assistance.",
                "1. Register on skillindia.gov.in or pmkvyofficial.org\n2. Choose job role and nearest training centre\n3. Attend training and assessment.",
                "https://www.pmkvyofficial.org",
                "Central Government", "2015-07-15", true, false
        ));

        // 7. Stand-Up India
        sampleSchemes.add(new Scheme(
                7,
                "Stand-Up India Scheme for Women and SC/ST",
                "Facilitates bank loans between ₹10 lakh and ₹1 crore to at least one SC/ST borrower and one woman borrower per bank branch for setting up a greenfield enterprise.",
                18, 65, "All", null,
                "[\"Business\",\"Unemployed\",\"Employee\"]", "10th Pass",
                "[\"SC\",\"ST\",\"General\",\"OBC\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Bank loans from ₹10 lakh to ₹1 crore for manufacturing, services, agri-allied activities, or trading with handholding support.",
                "1. Apply online through standupmitra.in portal\n2. Submit project proposal and business plan\n3. Connect with lead district manager or bank branch.",
                "https://www.standupmitra.in",
                "Central Government", "2016-04-05", true, false
        ));

        // 8. PM Mudra Yojana
        sampleSchemes.add(new Scheme(
                8,
                "Pradhan Mantri MUDRA Yojana (PMMY)",
                "Provides collateral-free institutional loans up to ₹10 lakh to micro/small business enterprises in non-farm sector (Shishu, Kishore, Tarun categories).",
                18, 65, "All", null,
                "[\"Business\",\"Farmer\",\"Unemployed\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Collateral-free micro loans up to ₹50,000 (Shishu), ₹50k–₹5L (Kishore), and ₹5L–₹10L (Tarun) at competitive interest rates.",
                "1. Visit udyamimitra.in or any public/private bank\n2. Fill MUDRA loan application with business proof\n3. Sanction and disbursement to bank account.",
                "https://www.mudra.org.in",
                "Central Government", "2015-04-08", true, false
        ));

        // 9. Digital India Internship Scheme
        sampleSchemes.add(new Scheme(
                9,
                "Digital India Internship Scheme (MeitY)",
                "Provides opportunity to Indian students to secure practical experience and exposure to emerging technologies and governance systems.",
                18, 30, "All", null,
                "[\"Student\"]", "Graduate",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Monthly stipend of ₹10,000 along with government certificate of internship from Ministry of Electronics and Information Technology.",
                "1. Apply online on MeitY portal at meity.gov.in/internship\n2. Upload college recommendation letter and mark sheets\n3. Selection based on interview and merit.",
                "https://www.meity.gov.in",
                "Central Government", "2020-05-10", true, false
        ));

        // 10. PM Ujjwala Yojana
        sampleSchemes.add(new Scheme(
                10,
                "Pradhan Mantri Ujjwala Yojana 2.0 (PMUY)",
                "Aims to safeguard the health of women and children by providing clean LPG cooking gas connections to poor households.",
                18, 70, "Female", 200000L,
                "[\"All\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "Married",
                "Free LPG connection, first refill and stove completely free with subsidy directly credited per cylinder.",
                "1. Apply online at pmuy.gov.in or visit local LPG distributor\n2. Submit BPL ration card and Aadhaar details\n3. Connection release within 7 working days.",
                "https://pmuy.gov.in",
                "Central Government", "2016-05-01", true, false
        ));

        // 11. PM Fasal Bima Yojana
        sampleSchemes.add(new Scheme(
                11,
                "Pradhan Mantri Fasal Bima Yojana (PMFBY)",
                "Comprehensive crop insurance scheme protecting farmers against crop loss/damage arising out of unforeseen natural calamities.",
                18, 75, "All", null,
                "[\"Farmer\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Very low farmer premium (1.5% for Rabi, 2% for Kharif, 5% for commercial/horticultural crops) with full sum insured payout for crop damage.",
                "1. Apply via pmfby.gov.in, CSC center, or crop loan bank branch\n2. Upload sowing certificate and land records\n3. Claims assessed via satellite/ground survey.",
                "https://pmfby.gov.in",
                "Central Government", "2016-02-18", true, false
        ));

        // 12. Kisan Credit Card
        sampleSchemes.add(new Scheme(
                12,
                "Kisan Credit Card (KCC) Scheme",
                "Ensures adequate and timely credit support from the banking system to farmers for their cultivation and farm allied activities under single window.",
                18, 75, "All", null,
                "[\"Farmer\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Short-term credit limit up to ₹3 lakh at subsidized 4% effective interest rate with prompt repayment incentive.",
                "1. Download KCC form from any bank website or visit nearest rural bank branch\n2. Submit land title documents and crop pattern details\n3. KCC smart card issued.",
                "https://myscheme.gov.in",
                "Central Government", "1998-08-01", true, false
        ));

        // 13. National Scholarship Portal
        sampleSchemes.add(new Scheme(
                13,
                "National Scholarship Portal - Merit-cum-Means",
                "Provides financial assistance to poor and meritorious students from minority and low-income communities to pursue higher professional studies.",
                18, 30, "All", 250000L,
                "[\"Student\"]", "12th Pass",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Full course fee reimbursement plus monthly maintenance allowance of up to ₹20,000 per academic year.",
                "1. Register on scholarships.gov.in\n2. Enter student basic profile, academic records, and institute code\n3. Verification by institute nodal officer.",
                "https://scholarships.gov.in",
                "Central Government", "2015-07-01", true, false
        ));

        // 14. Post Matric Scholarship for SC/ST
        sampleSchemes.add(new Scheme(
                14,
                "Post Matric Scholarship for SC/ST Students",
                "Centrally sponsored scheme providing financial assistance to Scheduled Caste and Scheduled Tribe students studying at post-matriculation stage.",
                18, 35, "All", 250000L,
                "[\"Student\"]", "10th Pass",
                "[\"SC\",\"ST\"]",
                "[\"All India\"]", "All",
                "100% compulsory non-refundable fees payment along with annual academic allowance up to ₹13,500 directly via DBT.",
                "1. Apply via State scholarship portal or NSP scholarships.gov.in\n2. Upload caste certificate, income certificate, and fee receipt\n3. Direct DBT transfer.",
                "https://socialjustice.gov.in",
                "Central Government", "2006-04-01", true, false
        ));

        // 15. PM Vishwakarma Yojana
        sampleSchemes.add(new Scheme(
                15,
                "PM Vishwakarma Scheme",
                "Holistic scheme to provide end-to-end support to traditional artisans and craftspeople working with hands and tools.",
                18, 65, "All", null,
                "[\"Business\",\"Employee\",\"Unemployed\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Free skill training with ₹500/day stipend, modern toolkit incentive of ₹15,000, and collateral-free credit support up to ₹3,00,000 at 5% interest.",
                "1. Register at nearest CSC centre using biometric authentication\n2. Skill assessment and PM Vishwakarma digital ID issuance\n3. Loan application processing.",
                "https://pmvishwakarma.gov.in",
                "Central Government", "2023-09-17", true, false
        ));

        // 16. Atal Pension Yojana
        sampleSchemes.add(new Scheme(
                16,
                "Atal Pension Yojana (APY)",
                "Periodic pension scheme for unorganized sector workers delivering guaranteed monthly pension after the age of 60 years.",
                18, 40, "All", null,
                "[\"Unemployed\",\"Employee\",\"Farmer\",\"Business\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Guaranteed monthly pension of ₹1,000, ₹2,000, ₹3,000, ₹4,000, or ₹5,000 from age 60 until death with spouse pension continuation.",
                "1. Approach bank where savings account is maintained\n2. Provide APY registration mandate with auto-debit consent\n3. PRAN number generated.",
                "https://www.npscra.nsdl.co.in",
                "Central Government", "2015-05-09", true, false
        ));

        // 17. PM SVANidhi
        sampleSchemes.add(new Scheme(
                17,
                "PM SVANidhi (Street Vendor's AtmaNirbhar Nidhi)",
                "Special micro-credit facility scheme for street vendors to facilitate collateral-free working capital loan to resume their livelihoods.",
                18, 65, "All", null,
                "[\"Business\",\"Unemployed\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Initial working capital loan of ₹10,000, progressing to ₹20,000 and ₹50,000 on timely repayment with 7% interest subsidy.",
                "1. Apply on pmsvanidhi.mohua.gov.in or SVANidhi mobile app\n2. Enter vendor Certificate of Vending (CoV) / LOR\n3. Direct disbursal within 10 days.",
                "https://pmsvanidhi.mohua.gov.in",
                "Central Government", "2020-06-01", true, false
        ));

        // 18. Pre-Matric Scholarship for Minorities
        sampleSchemes.add(new Scheme(
                18,
                "Pre-Matric Scholarship Scheme for Minorities",
                "Encourages minority parents to send their school-going children to school, lighten their financial burden on school education, and sustain their efforts.",
                18, 25, "All", 100000L,
                "[\"Student\"]", "Below 10th",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"All India\"]", "All",
                "Admission and tuition fee plus maintenance allowance credited directly to students' accounts.",
                "1. Register on NSP scholarships.gov.in\n2. Submit previous year mark sheet (minimum 50%) and parent income certificate\n3. Head of Institution verification.",
                "https://minorityaffairs.gov.in",
                "Central Government", "2008-04-01", true, false
        ));

        // 19. Maharashtra Jyotirao Phule Jan Arogya Yojana
        sampleSchemes.add(new Scheme(
                19,
                "Mahatma Jyotirao Phule Jan Arogya Yojana (MJPJAY)",
                "State government healthcare insurance scheme in Maharashtra providing quality medical care for identified specialty services.",
                1, 95, "All", 200000L,
                "[\"All\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"Maharashtra\"]", "All",
                "Cashless medical coverage of up to ₹5,00,000 per family per year for 996 surgeries/therapies across Maharashtra network hospitals.",
                "1. Show Ration Card (Yellow/Orange) or Annapurna card at network hospital\n2. Arogyamitra assists with cashless admission\n3. Hassle-free treatment.",
                "https://www.jeevandayee.gov.in",
                "State Government", "2012-07-02", true, false
        ));

        // 20. Uttar Pradesh Mukhyamantri Kanya Sumangala Yojana
        sampleSchemes.add(new Scheme(
                20,
                "UP Mukhyamantri Kanya Sumangala Yojana",
                "State social security scheme in Uttar Pradesh providing conditional cash transfers to girl children at different stages of birth and education.",
                18, 50, "Female", 300000L,
                "[\"All\"]", "None",
                "[\"General\",\"OBC\",\"SC\",\"ST\",\"EWS\"]",
                "[\"Uttar Pradesh\"]", "All",
                "Total monetary grant of ₹25,000 disbursed in 6 installments from birth to girl child's admission in graduation degree/diploma.",
                "1. Apply online at mksy.up.gov.in\n2. Upload daughter birth certificate, domicile certificate, and bank details\n3. Verification by District Probation Officer.",
                "https://mksy.up.gov.in",
                "State Government", "2019-10-25", true, false
        ));

        for (Scheme scheme : sampleSchemes) {
            insertScheme(db, scheme);
        }
    }

    private void insertScheme(SQLiteDatabase db, Scheme scheme) {
        ContentValues values = new ContentValues();
        values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_ID, scheme.getSchemeId());
        values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME, scheme.getSchemeName());
        values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION, scheme.getSchemeDescription());
        values.put(SchemeContract.SchemeEntry.COLUMN_MIN_AGE, scheme.getMinAge());
        values.put(SchemeContract.SchemeEntry.COLUMN_MAX_AGE, scheme.getMaxAge());
        values.put(SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE, scheme.getGenderEligible());
        values.put(SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT, scheme.getIncomeLimit());
        values.put(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS, scheme.getEligibleOccupations());
        values.put(SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL, scheme.getMinEducationLevel());
        values.put(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY, scheme.getEligibleCategory());
        values.put(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES, scheme.getEligibleStates());
        values.put(SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS, scheme.getMaritalStatusRequirement());
        values.put(SchemeContract.SchemeEntry.COLUMN_BENEFITS, scheme.getBenefits());
        values.put(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS, scheme.getApplicationProcess());
        values.put(SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE, scheme.getOfficialWebsite());
        values.put(SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE, scheme.getSchemeType());
        values.put(SchemeContract.SchemeEntry.COLUMN_CREATED_DATE, scheme.getCreatedDate());
        values.put(SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE, scheme.isActive() ? 1 : 0);
        values.put(SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED, scheme.isBookmarked() ? 1 : 0);

        db.insertWithOnConflict(SchemeContract.SchemeEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Scheme> getAllSchemes() {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " = 1",
                    null,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schemes.add(cursorToScheme(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all schemes", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public Scheme getSchemeById(int schemeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " = ?",
                    new String[]{String.valueOf(schemeId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursorToScheme(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching scheme by id: " + schemeId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public List<Scheme> searchAndFilterSchemes(String query, String stateFilter, String typeFilter, String categoryFilter) {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            StringBuilder selection = new StringBuilder(SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE + " = 1");
            List<String> selectionArgs = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_BENEFITS).append(" LIKE ?)");
                String pattern = "%" + query.trim() + "%";
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
                selectionArgs.add(pattern);
            }

            if (typeFilter != null && !typeFilter.equals("All") && !typeFilter.equals("All Types")) {
                selection.append(" AND ").append(SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE).append(" = ?");
                selectionArgs.add(typeFilter);
            }

            if (stateFilter != null && !stateFilter.equals("All") && !stateFilter.equals("All India")) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES).append(" LIKE '%All India%')");
                selectionArgs.add("%" + stateFilter + "%");
            }

            if (categoryFilter != null && !categoryFilter.equals("All") && !categoryFilter.equals("All Categories")) {
                selection.append(" AND (")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY).append(" LIKE ? OR ")
                        .append(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY).append(" LIKE '%All%')");
                selectionArgs.add("%" + categoryFilter + "%");
            }

            String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    selection.toString(),
                    args,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schemes.add(cursorToScheme(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering schemes", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public List<Scheme> getBookmarkedSchemes() {
        List<Scheme> schemes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    SchemeContract.SchemeEntry.TABLE_NAME,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED + " = 1",
                    null,
                    null,
                    null,
                    SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    schemes.add(cursorToScheme(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching bookmarked schemes", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return schemes;
    }

    public boolean toggleBookmark(int schemeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean newBookmarkState = false;
        try {
            Scheme scheme = getSchemeById(schemeId);
            if (scheme != null) {
                newBookmarkState = !scheme.isBookmarked();
                ContentValues values = new ContentValues();
                values.put(SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED, newBookmarkState ? 1 : 0);
                db.update(
                        SchemeContract.SchemeEntry.TABLE_NAME,
                        values,
                        SchemeContract.SchemeEntry.COLUMN_SCHEME_ID + " = ?",
                        new String[]{String.valueOf(schemeId)}
                );

                if (newBookmarkState) {
                    ContentValues bm = new ContentValues();
                    bm.put(SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID, schemeId);
                    bm.put(SchemeContract.BookmarkEntry.COLUMN_SAVED_TIMESTAMP, System.currentTimeMillis());
                    db.insertWithOnConflict(SchemeContract.BookmarkEntry.TABLE_NAME, null, bm, SQLiteDatabase.CONFLICT_REPLACE);
                } else {
                    db.delete(
                            SchemeContract.BookmarkEntry.TABLE_NAME,
                            SchemeContract.BookmarkEntry.COLUMN_SCHEME_ID + " = ?",
                            new String[]{String.valueOf(schemeId)}
                    );
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling bookmark for schemeId: " + schemeId, e);
        }
        return newBookmarkState;
    }

    private Scheme cursorToScheme(Cursor cursor) {
        Scheme scheme = new Scheme();
        scheme.setSchemeId(cursor.getInt(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_SCHEME_ID)));
        scheme.setSchemeName(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_SCHEME_NAME)));
        scheme.setSchemeDescription(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_SCHEME_DESCRIPTION)));

        int minAgeIdx = cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_MIN_AGE);
        if (!cursor.isNull(minAgeIdx)) {
            scheme.setMinAge(cursor.getInt(minAgeIdx));
        }

        int maxAgeIdx = cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_MAX_AGE);
        if (!cursor.isNull(maxAgeIdx)) {
            scheme.setMaxAge(cursor.getInt(maxAgeIdx));
        }

        scheme.setGenderEligible(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_GENDER_ELIGIBLE)));

        int incomeLimitIdx = cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_INCOME_LIMIT);
        if (!cursor.isNull(incomeLimitIdx)) {
            scheme.setIncomeLimit(cursor.getLong(incomeLimitIdx));
        }

        scheme.setEligibleOccupations(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_OCCUPATIONS)));
        scheme.setMinEducationLevel(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_MIN_EDUCATION_LEVEL)));
        scheme.setEligibleCategory(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_CATEGORY)));
        scheme.setEligibleStates(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_ELIGIBLE_STATES)));
        scheme.setMaritalStatusRequirement(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_MARITAL_STATUS)));
        scheme.setBenefits(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_BENEFITS)));
        scheme.setApplicationProcess(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_APPLICATION_PROCESS)));
        scheme.setOfficialWebsite(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_OFFICIAL_WEBSITE)));
        scheme.setSchemeType(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_SCHEME_TYPE)));
        scheme.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_CREATED_DATE)));
        scheme.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_IS_ACTIVE)) == 1);
        scheme.setBookmarked(cursor.getInt(cursor.getColumnIndexOrThrow(SchemeContract.SchemeEntry.COLUMN_IS_BOOKMARKED)) == 1);

        return scheme;
    }
}
