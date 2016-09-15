package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.networks.ok.OkClient;

@Module
public class OkClientTestModule {

    @Provides
    @Singleton
    @NonNull
    public OkClient provideOkClient() {
        return new OkClientTestImpl();
    }
}
