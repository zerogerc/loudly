package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.networks.instagram.InstagramClient;

@Module
public class InstagramClientTestModule {
    @Provides
    @Singleton
    @NonNull
    InstagramClient provideInstagramClient() {
        return new InstagramClientTestImpl();
    }
}
