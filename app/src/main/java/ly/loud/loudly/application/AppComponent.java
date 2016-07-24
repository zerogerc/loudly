package ly.loud.loudly.application;

import javax.inject.Singleton;

import dagger.Component;
import ly.loud.loudly.application.models.PeopleGetterModel;
import ly.loud.loudly.ui.PeopleListFragment;
import ly.loud.loudly.util.database.DatabaseComponent;
import ly.loud.loudly.util.database.KeysDbModule;
import ly.loud.loudly.util.database.PostDbModule;

/**
 * Created by ZeRoGerc on 20/07/16.
 */
@Singleton @Component (modules = AppModule.class)
public interface AppComponent {

    Loudly loudlyApp();
    PeopleGetterModel peopleGetterModel();

    void inject(PeopleListFragment peopleListFragment);
}
