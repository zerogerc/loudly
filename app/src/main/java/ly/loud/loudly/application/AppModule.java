package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.models.CommentsGetterModel;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.FacebookModel;
import ly.loud.loudly.application.models.InstagramModel;
import ly.loud.loudly.application.models.KeysModel;
import ly.loud.loudly.application.models.PeopleGetterModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.application.models.VKModel;

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
    PeopleGetterModel providePeopleGetterModel(@NonNull CoreModel coreModel) {
        return new PeopleGetterModel(
                loudlyApplication,
                coreModel
        );
    }

    @Provides
    @Singleton
    CommentsGetterModel provideCommentsGetterModel() {
        return new CommentsGetterModel(loudlyApplication);
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
    PostDeleterModel providePostDeleterModel() {
        return new PostDeleterModel(loudlyApplication);
    }

    @Provides
    @Singleton
    PostUploadModel providePostUploadModel() {
        return new PostUploadModel(loudlyApplication);
    }
}
