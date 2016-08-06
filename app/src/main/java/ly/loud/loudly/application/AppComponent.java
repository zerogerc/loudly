package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Component;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.networks.Facebook.FacebookClientModule;
import ly.loud.loudly.networks.VK.VKClientModule;
import ly.loud.loudly.ui.PeopleListFragment;
import ly.loud.loudly.ui.brand_new.feed.FeedActivity;
import ly.loud.loudly.ui.brand_new.feed.FeedFragment;
import ly.loud.loudly.ui.brand_new.full_post.FullPostInfoFragment;
import ly.loud.loudly.ui.brand_new.post.NetworksChooseLayout;
import ly.loud.loudly.ui.brand_new.post.NewPostFragment;

/**
 * Created by ZeRoGerc on 20/07/16.
 */
@Singleton @Component (modules = {AppModule.class,
        VKClientModule.class, FacebookClientModule.class})
public interface AppComponent {

    @NonNull
    Loudly loudlyApplication();

    @NonNull
    Handler mainThreadHandler();

    @NonNull
    GetterModel peopleGetterModel();

    @NonNull
    PostDeleterModel postDeleterModel();

    @NonNull
    PostUploadModel postUploadModel();

    @NonNull
    CoreModel coreModel();

    void inject(@NonNull FeedActivity feedActivity);

    void inject(@NonNull PeopleListFragment peopleListFragment);
    void inject(@NonNull NewPostFragment newPostFragment);
    void inject(@NonNull FeedFragment feedFragment);
    void inject(@NonNull FullPostInfoFragment fullPostInfoFragment);

    void inject(@NonNull NetworksChooseLayout networksChooseLayout);
}
