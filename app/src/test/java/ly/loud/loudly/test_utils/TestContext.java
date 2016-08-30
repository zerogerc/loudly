package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;

import org.robolectric.shadows.ShadowApplication;

public class TestContext {

    private TestContext() { }

    @SuppressWarnings("NullableProblems") // init
    @NonNull
    public static ShadowApplication shapp;

    /**
     * Should be called before each test
     */
    public static void init() {
        /*
            Application is recreated for every test, so we need to reinitialize these
         */
//        shapp = Shadows.shadowOf(LoudlyIntegrationTestRunner.app());
    }

    /**
     * Should be called after each test
     */
    public static void reset() {
        shapp = null;
    }
}
