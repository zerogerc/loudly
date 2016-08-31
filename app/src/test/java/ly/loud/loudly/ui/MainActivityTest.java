package ly.loud.loudly.ui;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import ly.loud.loudly.BuildConfig;
import ly.loud.loudly.test_utils.LoudlyIntegrationTestRunner;

import static org.robolectric.Robolectric.buildActivity;

@RunWith(LoudlyIntegrationTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {

    @Test
    public void should_NoThrowOnNormalLifecycle() {
        final ActivityController<MainActivity> controller = buildActivity(MainActivity.class);
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