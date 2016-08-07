package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Component;
import ly.loud.loudly.application.models.AuthModel;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.networks.facebook.FacebookClientModule;
import ly.loud.loudly.networks.vk.VKClientModule;
import ly.loud.loudly.ui.MainActivity;
import ly.loud.loudly.ui.auth.AuthFragment;
import ly.loud.loudly.ui.people_list.PeopleListFragment;
import ly.loud.loudly.ui.feed.FeedFragment;
import ly.loud.loudly.ui.full_post.FullPostInfoFragment;
import ly.loud.loudly.ui.new_post.NetworksChooseLayout;
import ly.loud.loudly.ui.new_post.NewPostFragment;

@Singleton @Component (modules = {AppModule.class,
        VKClientModule.class, FacebookClientModule.class})
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
    CoreModel coreModel();

    void inject(@NonNull MainActivity mainActivity);

    void inject(@NonNull PeopleListFragment peopleListFragment);
    void inject(@NonNull NewPostFragment newPostFragment);
    void inject(@NonNull FeedFragment feedFragment);
    void inject(@NonNull FullPostInfoFragment fullPostInfoFragment);
    void inject(@NonNull AuthFragment authFragment);

    void inject(@NonNull NetworksChooseLayout networksChooseLayout);
}
