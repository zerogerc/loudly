package ly.loud.loudly.application.models;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.UpdateInfoService;
import ly.loud.loudly.base.exceptions.FatalException;
import ly.loud.loudly.base.multiple.LoudlyPost;
import rx.Completable;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import static java.util.concurrent.TimeUnit.SECONDS;
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
    private BehaviorSubject<UpdateInfoService> serviceSubject;

    public InfoUpdateModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @CheckResult
    @NonNull
    public Completable subscribeOnUpdates(@NonNull LoudlyPost loudlyPost) {
        return getService()
                .map(service -> service.subscribe(loudlyPost, MAXIMAL_UPDATE_INTERVAL))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    public Completable subscribeOnUpdates(@NonNull List<LoudlyPost> loudlyPosts) {
        return getService()
                .map(service -> service.subscribe(loudlyPosts, MAXIMAL_UPDATE_INTERVAL))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    public Completable subscribeOnFrequentUpdates(@NonNull LoudlyPost loudlyPost) {
        return getService()
                .map(service -> service.subscribe(loudlyPost, MINIMAL_UPDATE_INTERVAL))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    public Completable subscribeOnFrequentUpdates(@NonNull List<LoudlyPost> loudlyPosts) {
        return getService()
                .map(service -> service.subscribe(loudlyPosts, MINIMAL_UPDATE_INTERVAL))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    public Completable unsubscribe(@NonNull LoudlyPost loudlyPost) {
        return getService()
                .map(service -> service.unsubscribe(loudlyPost))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    public Completable unsubscribe(@NonNull List<LoudlyPost> loudlyPost) {
        return getService()
                .map(service -> service.unsubscribe(loudlyPost))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Observable<UpdateInfoService> getService() {
        return changeSubscription(safeGetService(), io());
    }

    @CheckResult
    @NonNull
    private Observable<UpdateInfoService> safeGetService() {
        return getServiceSubject()
                .asObservable()
                .doOnError(fatalError -> bindToService()) // Try to reconnect to service after some time
                .retryWhen(errors -> errors.flatMap(ignored -> Observable.timer(5, SECONDS)))
                .first();
    }

    @NonNull
    public BehaviorSubject<UpdateInfoService> getServiceSubject() {
        if (serviceSubject == null) {
            serviceSubject = BehaviorSubject.create();
            bindToService();
        }
        return serviceSubject;
    }

    private void bindToService() {
        Intent startService = new Intent(loudlyApplication, UpdateInfoService.class);
        loudlyApplication.bindService(startService, new ServiceConnection() {
            @Override
            public void onServiceConnected(@Nullable ComponentName componentName,
                                           @Nullable IBinder iBinder) {
                UpdateInfoService.UpdateInfoServiceBinder binder =
                        (UpdateInfoService.UpdateInfoServiceBinder) iBinder;
                if (binder == null) {
                    getServiceSubject().onError(new FatalException());
                } else {
                    getServiceSubject().onNext(binder.getService());
                }
            }

            @Override
            public void onServiceDisconnected(@Nullable ComponentName componentName) {
                getServiceSubject().onError(new FatalException());
            }
        }, Context.BIND_AUTO_CREATE);
    }
}
