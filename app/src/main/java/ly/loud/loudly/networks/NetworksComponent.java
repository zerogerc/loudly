package ly.loud.loudly.networks;

import android.support.annotation.NonNull;
import dagger.Component;
import dagger.Provides;
import ly.loud.loudly.networks.VK.VKClient;
import ly.loud.loudly.networks.VK.VKClientModule;

import javax.inject.Singleton;

/**
 * Component for all network clients
 *
 * @author Danil Kolikov
 */
@Singleton
@Component(dependencies = {VKClientModule.class})
public interface NetworksComponent {
    @NonNull
    VKClient getVKClient();
}
