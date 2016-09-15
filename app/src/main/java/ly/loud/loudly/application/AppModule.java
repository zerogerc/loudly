package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.InfoUpdateModel;
import ly.loud.loudly.application.models.KeysModel;
import ly.loud.loudly.application.models.LoadMoreStrategyModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.application.models.PostsDatabaseModel;
import ly.loud.loudly.networks.facebook.FacebookModel;
import ly.loud.loudly.networks.instagram.InstagramModel;
import ly.loud.loudly.networks.ok.OkModel;
import ly.loud.loudly.networks.vk.VKModel;

import static android.os.Looper.getMainLooper;

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
            @NonNull InstagramModel instagramModel,
            @NonNull OkModel okModel
    ) {
        return new CoreModel(
                loudlyApplication,
                facebookModel,
                vkModel,
                instagramModel,
                okModel
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
    PostDeleterModel providePostDeleterModel(@NonNull CoreModel coreModel,
                                             @NonNull PostsDatabaseModel postsDatabaseModel,
                                             @NonNull InfoUpdateModel infoUpdateModel) {
        return new PostDeleterModel(coreModel, postsDatabaseModel, infoUpdateModel);
    }

    @Provides
    @Singleton
    @NonNull
    PostUploadModel providePostUploadModel(@NonNull CoreModel coreModel,
                                           @NonNull PostsDatabaseModel postsDatabaseModel,
                                           @NonNull InfoUpdateModel infoUpdateModel) {
        return new PostUploadModel(coreModel, postsDatabaseModel, infoUpdateModel);
    }

    @Provides
    @Singleton
    @NonNull
    PostLoadModel providePostLoadModel(@NonNull CoreModel coreModel,
                                       @NonNull PostsDatabaseModel postsDatabaseModel,
                                       @NonNull InfoUpdateModel infoUpdateModel) {
        return new PostLoadModel(coreModel, postsDatabaseModel, infoUpdateModel);
    }

    @Provides
    @Singleton
    @NonNull
    PostsDatabaseModel providePostsDatabaseModel(
            @NonNull @Named("post") StorIOSQLite postsDatabase) {
        return new PostsDatabaseModel(postsDatabase);
    }

    @Provides
    @Singleton
    @NonNull
    LoadMoreStrategyModel provideLoadMoreStrategy() {
        return new LoadMoreStrategyModel(loudlyApplication);
    }

    @Provides
    @Singleton
    @NonNull
    InfoUpdateModel provideInfoUpdateModel() {
        return new InfoUpdateModel(loudlyApplication);
    }
}
