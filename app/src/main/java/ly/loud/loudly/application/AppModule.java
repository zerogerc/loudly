package ly.loud.loudly.application;

import android.os.Handler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.models.CommentsGetterModel;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.FacebookModel;
import ly.loud.loudly.application.models.InstagramModel;
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
    Handler provideMainThreadHandler() {
        return new Handler(getMainLooper());
    }

    @Provides
    @Singleton
    PeopleGetterModel providePeopleGetterModel() {
        return new PeopleGetterModel(
                loudlyApplication,
                loudlyApplication.getAppComponent().coreModel()
        );
    }

    @Provides
    @Singleton
    CommentsGetterModel provideCommentsGetterModel() {
        return new CommentsGetterModel(loudlyApplication);
    }

    @Provides
    @Singleton
    CoreModel provideCoreModel() {
        return new CoreModel(
                loudlyApplication,
                new FacebookModel(loudlyApplication),
                new VKModel(loudlyApplication),
                new InstagramModel(loudlyApplication)
        );
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
