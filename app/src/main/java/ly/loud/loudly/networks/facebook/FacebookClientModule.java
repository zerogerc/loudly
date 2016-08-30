package ly.loud.loudly.networks.facebook;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Module for Facebook API client
 *
 * @author Danil Kolikov
 */
@Module
public class FacebookClientModule {
    private static final String MAIN_SERVER = "https://graph.facebook.com/v2.5/";

    @Provides
    @Singleton
    @NonNull
    public FacebookClient provideFacebookClient() {
        return provideRetrofit().create(FacebookClient.class);
    }

    private Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(MAIN_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
