package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.util.Pair;

import ly.loud.loudly.base.exceptions.FatalNetworkException;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import rx.Completable;
import rx.Observable;
import rx.subjects.PublishSubject;

import static ly.loud.loudly.util.RxUtils.retry3TimesAndFail;

public class PostDeleterModel {
    @NonNull
    private final CoreModel coreModel;

    @NonNull
    private final PostsDatabaseModel postsDatabaseModel;

    @NonNull
    private final InfoUpdateModel infoUpdateModel;

    @NonNull
    private final PublishSubject<Pair<LoudlyPost, Throwable>> deleteErrors;

    public PostDeleterModel(@NonNull CoreModel coreModel,
                            @NonNull PostsDatabaseModel postsDatabaseModel,
                            @NonNull InfoUpdateModel infoUpdateModel) {
        this.coreModel = coreModel;
        this.postsDatabaseModel = postsDatabaseModel;
        this.infoUpdateModel = infoUpdateModel;
        deleteErrors = PublishSubject.create();
    }

    @NonNull
    public Observable<Pair<LoudlyPost, Throwable>> observeDeleteErrors() {
        return deleteErrors.asObservable();
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
                .flatMap(n -> safeDelete(post, n))
                .first()
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Observable<Integer> safeDelete(@NonNull LoudlyPost post,
                                           @NonNull NetworkContract networkContract) {
        SinglePost singleNetworkInstance = post.getSingleNetworkInstance(networkContract.getId());
        if (singleNetworkInstance == null) {
            return Observable.empty();
        }
        return networkContract
                .delete(singleNetworkInstance)
                .toSingle(networkContract::getId)
                .toObservable();
    }

    @CheckResult
    @NonNull
    private Observable<Integer> safeDeleteWithHandling(@NonNull LoudlyPost post,
                                                       @NonNull NetworkContract networkContract) {
        return retry3TimesAndFail(
                safeDelete(post, networkContract),
                new FatalNetworkException(networkContract.getId())
        )
                .doOnError(error -> deleteErrors.onNext(new Pair<>(post, error)))
                .onErrorResumeNext(Observable.empty());
    }

    @CheckResult
    @NonNull
    public Observable<LoudlyPost> deletePostFromAllNetworks(@NonNull LoudlyPost post) {
        return coreModel
                .elementExistsIn(post)
                .flatMap(networkContract -> safeDeleteWithHandling(post, networkContract))
                .scan(post, LoudlyPost::deleteNetworkInstance)
                .flatMap(loudlyPost ->
                                postsDatabaseModel
                                        .updatePostLinks(loudlyPost)
                                        .toObservable()
                                        .doOnError(error -> deleteErrors.onNext(new Pair<>(loudlyPost, error)))
                                        .onErrorReturn(error -> loudlyPost)
                )
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
