package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.networks.facebook.FacebookClient;

@Module
public class FacebookClientTestModule {

    @Provides
    @Singleton
    @NonNull
    public FacebookClient provideFacebookClient() {
        // provide fake retrofit client in order to not work with face
        return new FacebookClientTestImpl();
    }
}
