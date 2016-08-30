package ly.loud.loudly.test_utils;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.ShadowMap;

import java.util.Properties;

public class LoudlyIntegrationTestRunner extends RobolectricGradleTestRunner {

    @NonNull
    private static Class[] shadows = new Class[]{
            ShadowNetwork.class
    };

    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public LoudlyIntegrationTestRunner(@NonNull Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected ShadowMap createShadowMap() {
        return super.createShadowMap();
    }

    @Override
    @CallSuper
    @NonNull
    protected Properties getConfigProperties() {
        final Properties properties = new Properties();
        properties.setProperty("application", LoudlyTestApplication.class.getName());
        properties.setProperty("constants", "ly.loud.loudly.BuildConfig");
        properties.setProperty("packageName", "ly.loud.loudly");
        return properties;
    }

    @NonNull
    public static LoudlyTestApplication app() {
        return (LoudlyTestApplication) RuntimeEnvironment.application;
    }
}
