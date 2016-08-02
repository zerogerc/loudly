package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.new_base.LoudlyPost;
import ly.loud.loudly.new_base.SinglePost;
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
    public Single<Boolean> deletePostFromNetwork(@NonNull LoudlyPost post, int network) {
        if (post.getSingleNetworkInstance(network) == null) {
            return Single.just(true);
        }
        return coreModel.elementExistsIn(post)
                .filter(n -> n.getId() == network)
                .take(1)
                .flatMap(n -> safeDelete(post, n).toObservable())
                .toSingle();
    }

    private Single<Boolean> safeDelete(LoudlyPost post, NetworkContract networkContract) {
        SinglePost singleNetworkInstance = post.getSingleNetworkInstance(networkContract.getId());
        if (singleNetworkInstance == null) {
            return Single.just(true);
        }
        return networkContract.delete(singleNetworkInstance);
    }

    // ToDo: Delete from DB after deletion from networks
    @CheckResult
    @NonNull
    public Observable<Pair<Integer, Boolean>> deletePostFromAllNetworks(@NonNull LoudlyPost post) {
        return coreModel.elementExistsIn(post)
                .flatMap(n -> safeDelete(post, n)
                        .map(result -> new Pair<>(n.getId(), result))
                        .toObservable());
    }
}
