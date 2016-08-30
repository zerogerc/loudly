package ly.loud.loudly.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import ly.loud.loudly.BuildConfig;
import ly.loud.loudly.test_utils.LoudlyIntegrationTestRunner;

@RunWith(LoudlyIntegrationTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TempActivityTest {
    @Test
    public void onCreate_shouldWorkOnNormalLifecycle() {
        final ActivityController<TempActivity> controller = Robolectric.buildActivity(TempActivity.class);
        controller
                .create()
                .start()
                .resume();

        controller
                .pause()
                .stop()
                .destroy();
    }
}