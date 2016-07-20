package ly.loud.loudly.application;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Component;
import ly.loud.loudly.application.models.CommentsGetterModel;
import ly.loud.loudly.application.models.PeopleGetterModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.ui.FullPostInfoActivity;
import ly.loud.loudly.ui.MainActivity;
import ly.loud.loudly.ui.PeopleListFragment;

/**
 * Created by ZeRoGerc on 20/07/16.
 */
@Singleton @Component (modules = AppModule.class)
public interface AppComponent {

    @NonNull
    Handler mainThreadHandler();

    @NonNull
    PeopleGetterModel peopleGetterModel();

    @NonNull
    CommentsGetterModel commentsGetterModel();

    @NonNull
    PostDeleterModel postDeleterModel();

    @NonNull
    PostUploadModel postUploadModel();

    void inject(MainActivity mainActivity);
    void inject(FullPostInfoActivity fullPostInfoActivity);

    void inject(PeopleListFragment peopleListFragment);
}
