package ly.loud.loudly.application.models;

import ly.loud.loudly.application.Loudly;
import rx.Single;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class PostDeleterModel {
    public Loudly loudlyApplication;

    public PostDeleterModel(Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    public Single<Boolean> deletePostFromNetwork(int network) {
        return null;
    }

    public Single<Boolean> deletePostFromAllNetworks() {
        return null;
    }
}
