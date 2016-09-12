package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import ly.loud.loudly.application.models.AuthModel;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.KeysModel;
import ly.loud.loudly.application.models.LoadMoreStrategyModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.networks.facebook.FacebookClientModule;
import ly.loud.loudly.networks.instagram.InstagramClientModule;
import ly.loud.loudly.networks.vk.VKClientModule;
import ly.loud.loudly.ui.MainActivity;
import ly.loud.loudly.ui.auth.AuthFragment;
import ly.loud.loudly.ui.feed.FeedFragment;
import ly.loud.loudly.ui.feed.FeedFragment.FeedComponent;
import ly.loud.loudly.ui.feed.FeedFragment.FeedModule;
import ly.loud.loudly.ui.full_post.FullPostInfoFragment;
import ly.loud.loudly.ui.full_post.FullPostInfoFragment.FullPostInfoComponent;
import ly.loud.loudly.ui.full_post.FullPostInfoFragment.FullPostInfoModule;
import ly.loud.loudly.ui.new_post.NetworksChooseLayout;
import ly.loud.loudly.ui.new_post.NewPostFragment;
import ly.loud.loudly.ui.new_post.NewPostFragment.NewPostComponent;
import ly.loud.loudly.ui.new_post.NewPostFragment.NewPostModule;
import ly.loud.loudly.ui.people_list.PeopleListFragment;
import ly.loud.loudly.ui.sidebar.SideBarFragment.SideBarComponent;
import ly.loud.loudly.ui.sidebar.SideBarFragment.SideBarModule;
import ly.loud.loudly.util.database.KeysDbModule;
import ly.loud.loudly.util.database.PostDbModule;

@Singleton @Component (modules = {
        AppModule.class, KeysDbModule.class, PostDbModule.class,
        VKClientModule.class, FacebookClientModule.class, InstagramClientModule.class})
public interface AppComponent {

    @NonNull
    Loudly loudlyApplication();

    @NonNull
    Handler mainThreadHandler();

    @NonNull
    AuthModel authModel();

    @NonNull
    GetterModel peopleGetterModel();

    @NonNull
    PostDeleterModel postDeleterModel();

    @NonNull
    PostUploadModel postUploadModel();

    @NonNull
    KeysModel keysModel();

    @NonNull
    CoreModel coreModel();

    @NonNull
    LoadMoreStrategyModel loadMoreStartegyModel();

    /**
     * Get database that stores Post, Keys, Links and Location tables
     *
     * @return Post Database
     */
    @Named("post")
    @NonNull
    StorIOSQLite postsDatabase();

    /**
     * Get database that stores keys
     *
     * @return Keys database
     */
    @Named("key")
    @NonNull
    StorIOSQLite keysDatabase();

    void inject(@NonNull MainActivity mainActivity);

    void inject(@NonNull PeopleListFragment peopleListFragment);

    void inject(@NonNull AuthFragment authFragment);

    void inject(@NonNull NetworksChooseLayout networksChooseLayout);

    void inject(@NonNull UpdateInfoService updateInfoService);

    @NonNull
    SideBarComponent plus(@NonNull SideBarModule sideBarModule);
    
    @NonNull
    FeedComponent plus(@NonNull FeedModule FeedModule);

    @NonNull
    FullPostInfoComponent plus(@NonNull FullPostInfoModule fullPostInfoModule);

    @NonNull
    NewPostComponent plus(@NonNull NewPostModule newPostModule);
}
