package ly.loud.loudly.ui;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import ly.loud.loudly.test_utils.LoudlyIntegrationTestRunner;

@RunWith(LoudlyIntegrationTestRunner.class)
@Config(sdk = 21)
public class MainActivityTest {

    @Test
    public void should_NoThrowOnNormalLifecycle() {
        final ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class);
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