package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;

import javax.inject.Inject;
import java.util.List;

/**
 * Model for post loading
 *
 * @author Danil Kolikov
 */
public class PostLoadModel {
    @NonNull
    private Loudly loudlyApplication;
    @NonNull
    private CoreModel coreModel;

    @Inject
    public PostLoadModel(@NonNull Loudly loudlyApplication, @NonNull CoreModel coreModel) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
    }

    @CheckResult
    @NonNull
    public Observable<List<? extends PlainPost>> loadPosts(@NonNull TimeInterval interval) {
        return coreModel.getConnectedNetworksModels().flatMap(n -> n.loadPosts(interval).toObservable());
    }
}
