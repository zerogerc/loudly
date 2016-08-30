package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import rx.Completable;
import rx.Observable;

public class PostDeleterModel {
    @NonNull
    private final CoreModel coreModel;

    @NonNull
    private final PostsDatabaseModel postsDatabaseModel;

    @NonNull
    private final InfoUpdateModel infoUpdateModel;

    public PostDeleterModel(@NonNull CoreModel coreModel,
                            @NonNull PostsDatabaseModel postsDatabaseModel,
                            @NonNull InfoUpdateModel infoUpdateModel) {
        this.coreModel = coreModel;
        this.postsDatabaseModel = postsDatabaseModel;
        this.infoUpdateModel = infoUpdateModel;
    }

    @CheckResult
    @NonNull
    public Completable deletePostFromNetwork(@NonNull LoudlyPost post,
                                             @Network int network) {
        if (post.getSingleNetworkInstance(network) == null) {
            return Completable.complete();
        }
        return coreModel.elementExistsIn(post)
                .filter(n -> n.getId() == network)
                .first()
                .flatMap(n -> safeDelete(post, n).toObservable())
                .first()
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Completable safeDelete(@NonNull LoudlyPost post,
                                   @NonNull NetworkContract networkContract) {
        SinglePost singleNetworkInstance = post.getSingleNetworkInstance(networkContract.getId());
        if (singleNetworkInstance == null) {
            return Completable.complete();
        }
        return networkContract.delete(singleNetworkInstance);
    }

    @CheckResult
    @NonNull
    public Observable<LoudlyPost> deletePostFromAllNetworks(@NonNull LoudlyPost post) {
        return coreModel
                .elementExistsIn(post)
                .flatMap(networkContract ->
                        safeDelete(post, networkContract)
                                .toSingle(networkContract::getId)
                                .toObservable())
                .scan(post, LoudlyPost::deleteNetworkInstance)
                .flatMap(loudlyPost ->
                        postsDatabaseModel
                                .updatePostLinks(loudlyPost)
                                .toObservable())
                .flatMap(loudlyPost -> {
                    if (loudlyPost.getNetworkInstances().size() == 1) {
                        // Has only one instance in DB - should delete from database
                        return infoUpdateModel
                                .unsubscribe(loudlyPost)
                                .toSingleDefault(loudlyPost)
                                .flatMap(postsDatabaseModel::deletePost)
                                .toObservable();
                    } else {
                        return Observable.just(loudlyPost);
                    }
                });
    }
}
