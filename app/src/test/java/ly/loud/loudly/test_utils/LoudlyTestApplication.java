package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

import ly.loud.loudly.application.AppComponent;
import ly.loud.loudly.application.AppModule;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.util.database.KeysDbModule;
import ly.loud.loudly.util.database.PostDbModule;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;

public class LoudlyTestApplication extends Loudly implements TestLifecycleApplication {

    @NonNull
    protected AppComponent getComponent() {
        return DaggerTestAppComponent.builder()
                .appModule(new AppModule(this))
                .keysDbModule(new KeysDbModule(this))
                .postDbModule(new PostDbModule(this))
                .build();
    }

    @Override
    public void beforeTest(Method method) {
        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @Override
    public void prepareTest(Object test) {

    }

    @Override
    public void afterTest(Method method) {
        RxAndroidPlugins.getInstance().reset();
    }
}
