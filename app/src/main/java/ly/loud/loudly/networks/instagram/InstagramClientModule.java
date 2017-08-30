package ly.loud.loudly.networks.instagram;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class InstagramClientModule {
    private static final String MAIN_SERVER = "https://api.instagram.com/v1/";

    @Provides
    @Singleton
    @NonNull
    public InstagramClient provideInstagramClient() {
        return provideRetrofit().create(InstagramClient.class);
    }

    @NonNull
    private static Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(MAIN_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
