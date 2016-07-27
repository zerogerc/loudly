package ly.loud.loudly.networks.VK;

import android.support.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Singleton;

/**
 * Module for VK client
 *
 * @author Danil Kolikov
 */
@Module
public class VKClientModule {
    private String API_VERSION = "5.53";
    private String MAIN_SERVER = "https://api.vk.com/method/";

    @Provides
    @NonNull
    @Singleton
    public VKClient provideVKClient(/*@NonNull Retrofit retrofit*/) {
        return /*retrofit*/provideVKRetrofit().create(VKClient.class);
    }

//    @Provides
//    @NonNull
//    @Singleton
    private Retrofit provideVKRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(MAIN_SERVER)
                .client(new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            Request original = chain.request();
                            HttpUrl url = original.url();
                            HttpUrl modified = url.newBuilder()
                                    .addQueryParameter("v", API_VERSION)
                                    .build();
                            return chain.proceed(original.newBuilder()
                                    .url(modified).build());
                        })
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
