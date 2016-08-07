package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.base.single.SinglePost;
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
    public Single<Boolean> deletePostFromNetwork(@NonNull LoudlyPost post,
                                                 @Network int network) {
        if (post.getSingleNetworkInstance(network) == null) {
            return Single.just(true);
        }
        return coreModel.elementExistsIn(post)
                .filter(n -> n.getId() == network)
                .take(1)
                .flatMap(n -> safeDelete(post, n))
                .toSingle();
    }

    @CheckResult
    @NonNull
    private Observable<Boolean> safeDelete(@NonNull LoudlyPost post,
                                           @NonNull NetworkContract networkContract) {
        SinglePost singleNetworkInstance = post.getSingleNetworkInstance(networkContract.getId());
        if (singleNetworkInstance == null) {
            return Observable.just(true);
        }
        return networkContract.delete(singleNetworkInstance);
    }

    // ToDo: Delete from DB after deletion from networks
    @CheckResult
    @NonNull
    public Observable<Pair<Integer, Boolean>> deletePostFromAllNetworks(@NonNull LoudlyPost post) {
        return coreModel.elementExistsIn(post)
                .flatMap(n -> safeDelete(post, n)
                        .map(result -> new Pair<>(n.getId(), result)));
    }
}
