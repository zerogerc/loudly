package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.networks.vk.VKClient;

@Module
public class VKClientTestModule {

    @Provides
    @Singleton
    @NonNull
    public VKClient provideVKClient() {
        return new VKClientTestImpl();
    }
}
