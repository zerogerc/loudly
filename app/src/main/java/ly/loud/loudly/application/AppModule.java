package ly.loud.loudly.application;

import android.os.Handler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.models.CommentsGetterModel;
import ly.loud.loudly.application.models.PeopleGetterModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostUploadModel;

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
        return new PeopleGetterModel(loudlyApplication);
    }

    @Provides
    @Singleton
    CommentsGetterModel provideCommentsGetterModel() {
        return new CommentsGetterModel(loudlyApplication);
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
