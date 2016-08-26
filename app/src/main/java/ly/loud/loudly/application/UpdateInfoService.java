package ly.loud.loudly.application;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.PostsDatabaseModel;
import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.util.Utils;
import rx.Observable;
import solid.collections.SolidList;

import static ly.loud.loudly.networks.Networks.LOUDLY;
import static ly.loud.loudly.util.RxUtils.repeat;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;
import static solid.collectors.ToSolidList.toSolidList;

/**
 * Service used for updating information
 */
public class UpdateInfoService extends Service {
    private static final String TAG = "UPDATE";
    public static final long MAXIMAL_UPDATE_INTERVAL = 10 * 60;
    public static final long MINIMAL_UPDATE_INTERVAL = 5;

    @Inject
    @SuppressWarnings("NullableProblems")   // onCreate
    @NonNull
    PostsDatabaseModel postsDatabaseModel;

    @Inject
    @SuppressWarnings("NullableProblems")   // onCreate
    @NonNull
    CoreModel coreModel;

    @NonNull
    private final Map<Long, Subscription> updateIntervals;

    @Nullable
    private rx.Subscription updateSubscription;

    public UpdateInfoService() {
        super();
        updateIntervals = new HashMap<>();
        updateSubscription = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.getApplicationContext(this).getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new UpdateInfoServiceBinder();
    }

    /**
     * Subscribe post for updates
     *
     * @param loudlyPost   Post to subscribe
     * @param timeInterval Initial interval in minutes for checking for updates.
     *                     Interval will be exponentially increased to
     *                     {@link UpdateInfoService#MAXIMAL_UPDATE_INTERVAL MAXIMAL_UPDATE_INTERVAL}
     * @return true, if post was successfully subscribed, false otherwise
     */
    public boolean subscribe(@NonNull LoudlyPost loudlyPost, long timeInterval) {
        boolean result = subscribeWithoutRestart(loudlyPost, timeInterval);
        if (result) {
            restartUpdates();
        }
        return result;
    }

    /**
     * Drop post's subscription for updates
     *
     * @param loudlyPost Post to remove subscription
     * @return True, if subscription was removed, false otherwise
     */
    public boolean unSubscribe(@NonNull LoudlyPost loudlyPost) {
        SinglePost loudlyInstance = loudlyPost.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            return false;
        }
        long id = Long.parseLong(loudlyInstance.getLink());

        if (updateIntervals.containsKey(id)) {
            updateIntervals.remove(id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Subcribe list of posts for updates with same interval
     *
     * @param loudlyPosts  Posts to subscribe
     * @param timeInterval Initial update interval
     * @return True, if any post was subscribed
     */
    public boolean subscribe(@NonNull List<LoudlyPost> loudlyPosts, long timeInterval) {
        boolean subscribed = false;
        for (LoudlyPost post : loudlyPosts) {
            subscribed |= subscribeWithoutRestart(post, timeInterval);
        }
        if (subscribed) {
            restartUpdates();
        }
        return subscribed;
    }

    /**
     * Drop post's subscription for updates
     *
     * @param loudlyPosts List of posts for drop subscription
     * @return True, if any subscription was dropped
     */
    public boolean unSubscribe(@NonNull List<LoudlyPost> loudlyPosts) {
        boolean unSubscribed = false;
        for (LoudlyPost post : loudlyPosts) {
            unSubscribed |= unSubscribe(post);
        }
        return unSubscribed;
    }

    private void restartUpdates() {
        if (updateSubscription != null && !updateSubscription.isUnsubscribed()) {
            updateSubscription.unsubscribe();
        }
        // ToDo: handle errors
        updateSubscription = repeat(Observable.defer(this::getUpdates), this::getRefreshInterval)
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(info -> {
                    Log.i(TAG, "new info: " + info.like + " " + info.repost + " " + info.comment);
                }, error -> {
                    Log.e(TAG, "error", error);
                });
    }

    @CheckResult
    @NonNull
    private Observable<Info> getUpdates() {
        return Observable
                .just(selectPostsToUpdateInfo())
                .flatMap(postsDatabaseModel::selectPostsByIds)
                .flatMap(this::getUpdates);
    }

    @NonNull
    private List<Long> selectPostsToUpdateInfo() {
        long currentTime = System.currentTimeMillis();
        List<Long> ids = new ArrayList<>();
        for (Map.Entry<Long, Subscription> entry : updateIntervals.entrySet()) {
            Subscription subscription = entry.getValue();
            if (subscription.nextUpdate < currentTime) {
                ids.add(entry.getKey());
                long nextInterval = getNextUpdateInterval(subscription.interval);
                subscription.interval = nextInterval;
                subscription.nextUpdate = currentTime + nextInterval;
            }
        }
        return ids;
    }

    @CheckResult
    @NonNull
    private Observable<Info> getUpdates(@NonNull SolidList<LoudlyPost> posts) {
        return coreModel
                .getConnectedNetworksModels()
                .<Pair<SinglePost, Info>>flatMap(networkContract -> {
                    SolidList<SinglePost> singlePosts = posts
                            .map(post -> post.getSingleNetworkInstance(networkContract.getId()))
                            .filter(a -> a != null)
                            .collect(toSolidList());
                    // ToDo: remove nullability check
                    return networkContract
                            .getUpdates(singlePosts)
                            .filter(list -> list != null)
                            .flatMap(Observable::from);
                })
                .flatMap(pair -> saveChanges(pair, posts))
                .reduce(new Info(), Info::add);
    }

    @CheckResult
    @NonNull
    private Observable<Info> saveChanges(@NonNull Pair<SinglePost, Info> update,
                                         @NonNull SolidList<LoudlyPost> loudlyPosts) {
        if (update.second.isEmpty()) {
            return Observable.just(update.second);
        }
        final SinglePost post = update.first;
        final Info diff = update.second;
        LoudlyPost loudlyPost = loudlyPosts.filter(lPost -> lPost
                .getSingleNetworkInstance(post.getNetwork()) == post)
                .first()
                .orNull();
        if (loudlyPost == null) {
            return Observable.empty();
        }
        return postsDatabaseModel.updateStoredInfo(loudlyPost, post.getNetwork(), diff);
    }

    private long getRefreshInterval() {
        long minimal = MAXIMAL_UPDATE_INTERVAL;
        for (Subscription subscription : updateIntervals.values()) {
            minimal = Math.min(minimal, subscription.interval);
        }
        return minimal;
    }

    private long getNextUpdateInterval(long interval) {
        return Math.min(interval * 7 / 5, MAXIMAL_UPDATE_INTERVAL);
    }

    private boolean subscribeWithoutRestart(@NonNull LoudlyPost loudlyPost, long timeInterval) {
        SinglePost loudlyInstance = loudlyPost.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            return false;
        }
        long id = Long.parseLong(loudlyInstance.getLink());
        long nextUpdate = System.currentTimeMillis() + timeInterval;
        if (!updateIntervals.containsKey(id)) {
            updateIntervals.put(id, new Subscription(timeInterval, nextUpdate));
            return true;
        }
        return false;
    }

    /**
     * Binder for UpdateInfoService. Contains link to it
     */
    public class UpdateInfoServiceBinder extends Binder {
        @NonNull
        public UpdateInfoService getService() {
            return UpdateInfoService.this;
        }
    }

    private class Subscription {
        long interval;
        long nextUpdate;

        public Subscription(long interval, long nextUpdate) {
            this.interval = interval;
            this.nextUpdate = nextUpdate;
        }
    }
}
