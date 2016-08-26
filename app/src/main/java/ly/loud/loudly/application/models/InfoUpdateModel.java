package ly.loud.loudly.application.models;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.UpdateInfoService;
import ly.loud.loudly.base.multiple.LoudlyPost;
import rx.Observable;
import rx.Single;

import static ly.loud.loudly.application.UpdateInfoService.MAXIMAL_UPDATE_INTERVAL;
import static ly.loud.loudly.application.UpdateInfoService.MINIMAL_UPDATE_INTERVAL;
import static ly.loud.loudly.util.RxUtils.changeSubscription;
import static rx.schedulers.Schedulers.io;

/**
 * Model for updating information about post
 */
public class InfoUpdateModel {
    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private UpdateInfoService.UpdateInfoServiceBinder serviceBinder;

    public InfoUpdateModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @CheckResult
    @NonNull
    public Single<Boolean> subscribeOnUpdates(@NonNull LoudlyPost loudlyPost) {
        return getService()
                .map(service -> service.subscribe(loudlyPost, MAXIMAL_UPDATE_INTERVAL));
    }

    @CheckResult
    @NonNull
    public Single<Boolean> subscribeOnUpdates(@NonNull List<LoudlyPost> loudlyPosts) {
        return getService()
                .map(service -> service.subscribe(loudlyPosts, MAXIMAL_UPDATE_INTERVAL));
    }

    @CheckResult
    @NonNull
    public Single<Boolean> subscribeOnFrequentUpdates(@NonNull LoudlyPost loudlyPost) {
        return getService()
                .map(service -> service.subscribe(loudlyPost, MINIMAL_UPDATE_INTERVAL));
    }

    @CheckResult
    @NonNull
    public Single<Boolean> subscribeOnFrequentUpdates(@NonNull List<LoudlyPost> loudlyPosts) {
        return getService()
                .map(service -> service.subscribe(loudlyPosts, MINIMAL_UPDATE_INTERVAL));
    }

    @CheckResult
    @NonNull
    public Single<Boolean> unSubscribe(@NonNull LoudlyPost loudlyPost) {
        return getService()
                .map(service -> service.unSubscribe(loudlyPost));
    }

    @CheckResult
    @NonNull
    public Single<Boolean> unSubscribe(@NonNull List<LoudlyPost> loudlyPost) {
        return getService()
                .map(service -> service.unSubscribe(loudlyPost));
    }

    @CheckResult
    @NonNull
    private Single<UpdateInfoService> getService() {
        return changeSubscription(
                getBinder().map(UpdateInfoService.UpdateInfoServiceBinder::getService),
                io()
        );
    }

    @CheckResult
    @NonNull
    private Single<UpdateInfoService.UpdateInfoServiceBinder> getBinder() {
        if (serviceBinder == null) {
            return bindToService();
        } else {
            return Single.just(serviceBinder);
        }
    }

    @UiThread
    @CheckResult
    @NonNull
    private Single<UpdateInfoService.UpdateInfoServiceBinder> bindToService() {
        return Observable
                .<UpdateInfoService.UpdateInfoServiceBinder>create(observer -> {
                    Intent startService = new Intent(loudlyApplication, UpdateInfoService.class);
                    loudlyApplication.bindService(startService, new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            UpdateInfoService.UpdateInfoServiceBinder binder =
                                    (UpdateInfoService.UpdateInfoServiceBinder) iBinder;
                            serviceBinder = binder;
                            observer.onNext(binder);
                            observer.onCompleted();
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {
                            serviceBinder = null;
                        }
                    }, Context.BIND_AUTO_CREATE);
                })
                .first()
                .toSingle();
    }
}
