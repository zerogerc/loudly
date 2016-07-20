package ly.loud.loudly.application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.models.PeopleGetterModel;

/**
 * Created by ZeRoGerc on 20/07/16.
 */
@Module
public class AppModule {
    private final Loudly loudlyApplication;

    public AppModule(Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @Provides @Singleton
    Loudly provideLoudlyContext() {
        return loudlyApplication;
    }

    @Provides @Singleton
    PeopleGetterModel providePeopleGetterModel() {
        return new PeopleGetterModel();
    }
}
