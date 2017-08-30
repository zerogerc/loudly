package ly.loud.loudly.test_utils;

import javax.inject.Singleton;

import dagger.Component;
import ly.loud.loudly.application.AppComponent;
import ly.loud.loudly.application.AppModule;
import ly.loud.loudly.util.database.KeysDbModule;
import ly.loud.loudly.util.database.PostDbModule;

@Singleton
@Component(modules = {
        AppModule.class, KeysDbModule.class, PostDbModule.class,
        VKClientTestModule.class, FacebookClientTestModule.class, InstagramClientTestModule.class})
public interface TestAppComponent extends AppComponent {
}
