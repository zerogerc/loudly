package ly.loud.loudly.application.models;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.says.LoudlyPost;
import rx.Single;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class PostUploadModel {
    private Loudly loudlyApplication;

    public PostUploadModel(Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    public Single<LoudlyPost> uploadPost(int... networks) {
        return null;
    }
}
