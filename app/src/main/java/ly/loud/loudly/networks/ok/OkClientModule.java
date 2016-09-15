package ly.loud.loudly.networks.ok;

import android.support.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class OkClientModule {
    private static final String API_SERVER = "https://api.ok.ru/";
    private static final String SESSION_KEY_FIELD = "session_key";
    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String SIGNATURE_FIELD = "sig";

    @Provides
    @NonNull
    @Singleton
    OkClient provideOkClient(@NonNull MessageDigest MD5) {
        return provideRetrofit(MD5).create(OkClient.class);
    }

    @NonNull
    private Retrofit provideRetrofit(@NonNull MessageDigest MD5) {
        return new Retrofit.Builder()
                .baseUrl(API_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                                .addInterceptor(chain -> {
                                    Request request = chain.request();
                                    HttpUrl url = request.url();
                                    HttpUrl modified = url
                                            .newBuilder()
                                            .addQueryParameter(
                                                    SIGNATURE_FIELD,
                                                    generateSignature(url, MD5)
                                            )
                                            .build();
                                    return chain
                                            .proceed(request
                                                            .newBuilder()
                                                            .url(modified)
                                                            .build()
                                            );
                                })
                                .build()
                )
                .build();
    }

    @NonNull
    private static String generateSignature(@NonNull HttpUrl url, @NonNull MessageDigest MD5) {
        Set<String> queries = url.queryParameterNames();
        TreeSet<String> sorted = new TreeSet<>(queries);
        sorted.remove(SESSION_KEY_FIELD);
        sorted.remove(ACCESS_TOKEN_FIELD);

        String decoded = "";
        for (String key : sorted) {
            decoded += key + "=" + url.queryParameter(key);
        }
        decoded += url.queryParameter(SESSION_KEY_FIELD);
        MD5.update(decoded.getBytes());
        return new String(MD5.digest()).toLowerCase();
    }

    @Provides
    @NonNull
    @Singleton
    MessageDigest provideMessageDigester() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Impossible case");
        }
    }
}
