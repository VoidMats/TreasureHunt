package com.example.mats.treasurehunt;

public final class Constants {

    public enum PLAY_MODE {
        DEFAULT,
        QUIZ,
        HUNT
    }

    // My constants
    public static final String USER_ID = "ACTIVITY_USER";
    public static final String NULL_USER = "NO_USER_AUTH";
    public static final String USER = "ACTIVITY_USER_DATA";
    public static final String PATH_ID = "PATH_ID";
    public static final String NULL_PATH = "ERASED_PATH";
    public static final String TIME_FINSIHED = "TOTAL_TIME_RESULT";
    public static final String TIME_NEG = "TOTAL_TIME_NEG";
    public static final String HUNT = "ACTIVITY_HUNT_DATA";
    public static final String SETTING_RESULT_REVOKE = "INTENT_REVOKE";
    public static final String SETTING_RESULT_NAME = "INTENT_NAME";

    // Android
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

}
