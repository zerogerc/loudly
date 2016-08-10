package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.models.*;
import ly.loud.loudly.networks.facebook.FacebookModel;
import ly.loud.loudly.networks.instagram.InstagramModel;
import ly.loud.loudly.networks.vk.VKModel;

import static android.os.Looper.getMainLooper;

/**
 * Created by ZeRoGerc on 20/07/16.
 */
@Module
public class AppModule {
    @NonNull
    private final Loudly loudlyApplication;

    public AppModule(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @Provides
    @Singleton
    @NonNull
    Loudly provideLoudlyApplication() {
        return loudlyApplication;
    }

    @Provides
    @Singleton
    @NonNull
    Handler provideMainThreadHandler() {
        return new Handler(getMainLooper());
    }

    @Provides
    @Singleton
    @NonNull
    GetterModel providePeopleGetterModel(@NonNull CoreModel coreModel) {
        return new GetterModel(
                loudlyApplication,
                coreModel
        );
    }

    @Provides
    @Singleton
    @NonNull
    CoreModel provideCoreModel(
            @NonNull FacebookModel facebookModel,
            @NonNull VKModel vkModel,
            @NonNull InstagramModel instagramModel
    ) {
        return new CoreModel(
                loudlyApplication,
                facebookModel,
                vkModel,
                instagramModel
        );
    }

    @Provides
    @Singleton
    @NonNull
    KeysModel provideKeysModel(@NonNull @Named("key") StorIOSQLite keysDatabse) {
        return new KeysModel(keysDatabse);
    }

    @Provides
    @Singleton
    @NonNull
    PostDeleterModel providePostDeleterModel(@NonNull CoreModel coreModel) {
        return new PostDeleterModel(loudlyApplication, coreModel);
    }

    @Provides
    @Singleton
    @NonNull
    PostUploadModel providePostUploadModel(@NonNull CoreModel coreModel) {
        return new PostUploadModel(loudlyApplication, coreModel);
    }

    @Provides
    @Singleton
    @NonNull
    PostLoadModel providePostLoadModel(@NonNull CoreModel coreModel) {
        return new PostLoadModel(loudlyApplication, coreModel);
    }
}
