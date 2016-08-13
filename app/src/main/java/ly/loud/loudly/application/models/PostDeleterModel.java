package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import rx.Observable;
import rx.Single;

public class PostDeleterModel {
    @NonNull
    private CoreModel coreModel;

    @NonNull
    private PostsDatabaseModel postsDatabaseModel;

    public PostDeleterModel(@NonNull CoreModel coreModel,
                            @NonNull PostsDatabaseModel postsDatabaseModel) {
        this.coreModel = coreModel;
        this.postsDatabaseModel = postsDatabaseModel;
    }

    @CheckResult
    @NonNull
    public Single<Boolean> deletePostFromNetwork(@NonNull LoudlyPost post,
                                                 @Network int network) {
        if (post.getSingleNetworkInstance(network) == null) {
            return Single.just(false);
        }
        return coreModel.elementExistsIn(post)
                .filter(n -> n.getId() == network)
                .flatMap(n -> safeDelete(post, n))
                .first()
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

    @CheckResult
    @NonNull
    public Observable<LoudlyPost> deletePostFromAllNetworks(@NonNull LoudlyPost post) {
        return coreModel.elementExistsIn(post)
                .flatMap(networkContract -> safeDelete(post, networkContract)
                        .map(result -> networkContract.getId()))
                .scan(post, LoudlyPost::deleteNetworkInstance)
                .flatMap(loudlyPost ->
                        postsDatabaseModel
                                .updatePostLinks(loudlyPost)
                                .toObservable())
                .flatMap(loudlyPost -> {
                    if (loudlyPost.getNetworkInstances().size() == 1) {
                        // Has only one instance in DB - should delete from database
                        return postsDatabaseModel
                                .deletePost(loudlyPost)
                                .toObservable();
                    } else {
                        return Observable.just(loudlyPost);
                    }
                });
    }
}
