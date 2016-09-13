package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.exceptions.FatalNetworkException;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;
import rx.Single;
import rx.subjects.PublishSubject;
import solid.collections.SolidList;
import solid.collectors.ToArrayList;

import static ly.loud.loudly.util.RxUtils.retry3TimesAndFail;
import static solid.collectors.ToList.toList;
import static solid.collectors.ToSolidList.toSolidList;

/**
 * Model for post loading
 *
 * @author Danil Kolikov
 */
public class PostLoadModel {
    @NonNull
    private final CoreModel coreModel;

    @NonNull
    private final PostsDatabaseModel postsDatabaseModel;

    @NonNull
    private final InfoUpdateModel infoUpdateModel;

    @NonNull
    private final PublishSubject<Throwable> loadErrors;

    public PostLoadModel(@NonNull CoreModel coreModel,
                         @NonNull PostsDatabaseModel postsDatabaseModel,
                         @NonNull InfoUpdateModel infoUpdateModel) {
        this.coreModel = coreModel;
        this.postsDatabaseModel = postsDatabaseModel;
        this.infoUpdateModel = infoUpdateModel;
        loadErrors = PublishSubject.create();
    }

    @CheckResult
    @NonNull
    public Observable<Throwable> observeLoadErrors() {
        return loadErrors.asObservable();
    }

    @NonNull
    private static SolidList<PlainPost> merge(
            @NonNull SolidList<PlainPost> oldList,
            @NonNull SolidList<SinglePost> newList) {
        // Set new instances to LoudlyPosts
        SolidList<PlainPost> withInstances = oldList.map(post -> {
            if (post instanceof LoudlyPost) {
                LoudlyPost loudlyPost = ((LoudlyPost) post);
                return newList
                        // Filter posts, which has same IDs as loudlyPost
                        .filter(single -> equals(single, loudlyPost))
                                // And set this posts as instances
                        .reduce(loudlyPost, LoudlyPost::setSingleNetworkInstance);
            }
            return post;
        }).collect(toSolidList());
        // Drop post which instances were set
        List<SinglePost> notSet = newList.filter(post ->
                        !withInstances.any(otherPost -> (otherPost instanceof LoudlyPost) &&
                                ((LoudlyPost) otherPost)
                                        .getSingleNetworkInstance(post.getNetwork()) == post)
        ).collect(toList());
        // Merge this two lists
        List<PlainPost> result = new ArrayList<>();
        result.addAll(withInstances);
        result.addAll(notSet);
        Collections.sort(result);
        return ListUtils.asSolidList(result);
    }

    private static boolean equals(@NonNull SinglePost post, @NonNull LoudlyPost loudlyPost) {
        SinglePost loudlyInstance = loudlyPost.getSingleNetworkInstance(post.getNetwork());
        return loudlyInstance != null && loudlyInstance.getLink().equals(post.getLink());
    }

    @NonNull
    private Observable<SolidList<SinglePost>> updateStoredInfo(
            @NonNull SolidList<SinglePost> newPosts,
            @NonNull SolidList<LoudlyPost> loudlyPosts) {
        List<Pair<Pair<LoudlyPost, Integer>, Info>> needUpdate = newPosts
                .map(post -> {
                            LoudlyPost corresponding = loudlyPosts
                                    .filter(loudlyPost -> equals(post, loudlyPost))
                                    .first()
                                    .orNull();
                            if (corresponding == null) {
                                return null;
                            }
                            SinglePost oldPost = corresponding
                                    .getSingleNetworkInstance(post.getNetwork());
                            //noinspection ConstantConditions corresponding has required instance
                            Info difference = post
                                    .getInfo()
                                    .subtract(oldPost.getInfo());
                            if (difference.isEmpty()) {
                                return null;
                            }
                            return new Pair<>(new Pair<>(corresponding, post.getNetwork()), difference);
                        }
                )
                .filter(a -> a != null)
                .collect(toList());
        //noinspection ResourceType Second element in first pair is network ID
        return Observable.from(needUpdate)
                .flatMap(pair -> postsDatabaseModel
                        .updateStoredInfo(pair.first.first, pair.first.second, pair.second))
                .toCompletable()
                .toSingleDefault(newPosts)
                .toObservable();
    }

    @CheckResult
    @NonNull
    private Observable<SolidList<SinglePost>> safeLoadPosts(
            @NonNull TimeInterval timeInterval,
            @NonNull NetworkContract networkContract) {
        return retry3TimesAndFail(
                networkContract.loadPosts(timeInterval),
                new FatalNetworkException(networkContract.getId())
        )
                .doOnError(loadErrors::onNext)
                .onErrorResumeNext(Observable.empty());
    }

    /**
     * Get list posts to show
     *
     * @param interval Interval to show posts
     * @return Observable, containing lists of post to show. First list in stream contains
     * posts from DB, next contains previous list merged with posts from some new network
     */
    @CheckResult
    @NonNull
    public Observable<SolidList<PlainPost>> loadPosts(@NonNull TimeInterval interval) {
        return postsDatabaseModel.loadPostsByTimeInterval(interval)
                .doOnError(loadErrors::onNext)
                .onErrorResumeNext(Observable.just(SolidList.empty()))
                .flatMap(list -> infoUpdateModel
                                .subscribeOnFrequentUpdates(list)
                                .toSingleDefault(list)
                                .toObservable()
                )
                .flatMap(list -> coreModel
                                .observeConnectedNetworksModels()
                                .flatMap(networkContract -> safeLoadPosts(interval, networkContract))
                                .flatMap(posts -> updateStoredInfo(posts, list))
                                .scan(
                                        list.cast(PlainPost.class).collect(toSolidList()),
                                        PostLoadModel::merge
                                )
                );
    }

    /**
     * Get updated lists of posts
     */
    @CheckResult
    @NonNull
    public Observable<SolidList<PlainPost>> observeUpdatedList() {
        return postsDatabaseModel
                .getEventsCount()
                .skip(1)    // Skip first value, because it's old number of events
                .map(count -> getCachedPosts());
    }

    /**
     * Get list of posts by {@link TimeInterval} as single.
     */
    @CheckResult
    @NonNull
    public Single<SolidList<PlainPost>> getPostsByInterval(@NonNull TimeInterval interval) {
        return loadPosts(interval).last().toSingle();
    }

    /**
     * Get list of all cached posts.
     */
    @CheckResult
    @NonNull
    public SolidList<PlainPost> getCachedPosts() {
        ArrayList<SolidList<SinglePost>> cachedPosts = coreModel.getAllNetworkModels()
                .map(NetworkContract::getCachedPosts)
                .collect(ToArrayList.toArrayList());

        SolidList<PlainPost> result = postsDatabaseModel
                .getCachedPosts()
                .cast(PlainPost.class)
                .collect(toSolidList());
        for (SolidList<SinglePost> newList : cachedPosts) {
            result = merge(result, newList);
        }
        return result;
    }
}
