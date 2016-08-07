package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

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
    private final Loudly loudlyApplication;

    public AppModule(Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @Provides
    @Singleton
    Loudly provideLoudlyApplication() {
        return loudlyApplication;
    }

    @Provides
    @Singleton
    Handler provideMainThreadHandler() {
        return new Handler(getMainLooper());
    }

    @Provides
    @Singleton
    GetterModel providePeopleGetterModel(@NonNull CoreModel coreModel) {
        return new GetterModel(
                loudlyApplication,
                coreModel
        );
    }

    @Provides
    @Singleton
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
    KeysModel provideKeysModel(@NonNull Loudly loudlyApplication) {
        return new KeysModel(loudlyApplication);
    }

    @Provides
    @Singleton
    PostDeleterModel providePostDeleterModel(@NonNull CoreModel coreModel) {
        return new PostDeleterModel(loudlyApplication, coreModel);
    }

    @Provides
    @Singleton
    PostUploadModel providePostUploadModel(@NonNull CoreModel coreModel) {
        return new PostUploadModel(loudlyApplication, coreModel);
    }

    @Provides
    @Singleton
    PostLoadModel providePostLoadModel(@NonNull CoreModel coreModel) {
        return new PostLoadModel(loudlyApplication, coreModel);
    }
}
