package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.says.Post;
import rx.Observable;
import rx.Single;

public class PostDeleterModel {
    @NonNull
    private Loudly loudlyApplication;
    @NonNull
    private CoreModel coreModel;

    public PostDeleterModel(@NonNull Loudly loudlyApplication, @NonNull CoreModel coreModel) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
    }

    @CheckResult
    @NonNull
    public Single<Boolean> deletePostFromNetwork(@NonNull Post post, int network) {
        return coreModel.elementExistsIn(post)
                .filter(n -> n.getId() == network)
                .take(1)
                .flatMap(n -> n.delete(post).toObservable())
                .toSingle();
    }

    // ToDo: Delete from DB after deletion from networks
    @CheckResult
    @NonNull
    public Observable<Pair<Integer, Boolean>> deletePostFromAllNetworks(@NonNull Post post) {
        return coreModel.elementExistsIn(post)
                .flatMap(n -> n.delete(post).map(flag -> new Pair<>(n.getId(), flag)).toObservable());
    }
}
