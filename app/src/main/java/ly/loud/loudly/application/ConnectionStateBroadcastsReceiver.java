package ly.loud.loudly.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.inject.Inject;

import ly.loud.loudly.application.models.InfoUpdateModel;
import ly.loud.loudly.application.models.LoadMoreStrategyModel;
import ly.loud.loudly.application.models.PostsDatabaseModel;
import ly.loud.loudly.base.multiple.LoudlyPost;
import rx.Observable;
import solid.collections.SolidList;

import static ly.loud.loudly.application.Loudly.getApplication;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class ConnectionStateBroadcastsReceiver extends BroadcastReceiver {
    private static final String TAG = "CONNECTION";

    @Inject
    @SuppressWarnings("NullableProblems")   // Inject in onReceive
    @NonNull
    InfoUpdateModel infoUpdateModel;

    @Inject
    @SuppressWarnings("NullableProblems")   // Inject in onReceive
    @NonNull
    PostsDatabaseModel postsDatabaseModel;

    @Inject
    @SuppressWarnings("NullableProblems") // Inject in onReceive
    @NonNull
    LoadMoreStrategyModel loadMoreStrategyModel;

    @Override
    public void onReceive(@Nullable Context context, @Nullable Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {
            return;
        }
        getApplication(context).getAppComponent().inject(this);

        @SuppressWarnings("deprecation") // ToDo: find other way
                NetworkInfo currentNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (currentNetworkInfo == null || !currentNetworkInfo.isConnected()) {
            handleDisconnected();
        }
        if (currentNetworkInfo != null && currentNetworkInfo.isConnected()) {
            handleConnected();
        }
    }

    private void handleConnected() {
        final Observable<SolidList<LoudlyPost>> postsObservable;
        SolidList<LoudlyPost> cachedPosts = postsDatabaseModel.getCachedPosts();
        if (cachedPosts.isEmpty()) {
            postsObservable = Observable.just(cachedPosts);
        } else {
            postsObservable = postsDatabaseModel
                    .loadPostsByTimeInterval(loadMoreStrategyModel.getCurrentTimeInterval());
        }
        postsObservable
                .flatMap(posts -> infoUpdateModel.subscribeOnFrequentUpdates(posts).toObservable())
                .toCompletable()
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(
                        () -> Log.i(TAG, "Posts subscribed on updates"),
                        error -> Log.e(TAG, "Can't subscribe posts on updates due to: ", error)
                );
    }

    private void handleDisconnected() {
        infoUpdateModel
                .unsubscribeAll()
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(() -> Log.i(TAG, "Posts update stopped"));
    }
}
