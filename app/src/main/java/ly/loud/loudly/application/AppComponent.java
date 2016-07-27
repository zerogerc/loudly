package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Component;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.networks.VK.VKClientModule;
import ly.loud.loudly.ui.FullPostInfoActivity;
import ly.loud.loudly.ui.MainActivity;
import ly.loud.loudly.ui.PeopleListFragment;
import ly.loud.loudly.ui.brand_new.FeedActivity;
import ly.loud.loudly.ui.brand_new.FeedFragment;

/**
 * Created by ZeRoGerc on 20/07/16.
 */
@Singleton @Component (modules = {AppModule.class, VKClientModule.class})
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

    void inject(FeedActivity feedActivity);
    void inject(MainActivity mainActivity);
    void inject(FullPostInfoActivity fullPostInfoActivity);

    void inject(PeopleListFragment peopleListFragment);
    void inject(FeedFragment feedFragment);
}
