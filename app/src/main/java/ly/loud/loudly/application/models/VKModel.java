package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.networks.VK.VKKeyKeeper;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;
import rx.Single;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class VKModel implements NetworkContract {

    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private VKKeyKeeper keyKeeper;

    public VKModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
        loadFromDB();
    }

    /**
     * Load wrap from DataBase
     */
    private void loadFromDB() {
        // TODO: implement
    }

    @Override
    @CheckResult
    public Single<Long> upload(Image image) {
        return null;
    }

    @Override
    @CheckResult
    public Single<Long> upload(Post post) {
        return null;
    }

    @Override
    @CheckResult
    public Single<Long> delete(Post post) {
        return null;
    }

    @Override
    @CheckResult
    public Observable<List<Post>> loadPosts(TimeInterval timeInterval) {
        return null;
    }

    @Override
    @CheckResult
    public Single<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        if (!(keyKeeper instanceof ly.loud.loudly.networks.VK.VKKeyKeeper))
            throw new AssertionError("KeyKeeper must be VkKeyKeeper");

        this.keyKeeper = ((VKKeyKeeper) keyKeeper);
        return Single.fromCallable(() -> true);
    }

    @Override
    @CheckResult
    public Single<Boolean> disconnect() {
        deleteKeyKeeperFromDB();
        this.keyKeeper = null;
        return Single.fromCallable(() -> true);
    }

    @Override
    @CheckResult
    public boolean isConnected() {
        return keyKeeper != null;
    }

    @Override
    public int getId() {
        return Networks.VK;
    }

    private void deleteKeyKeeperFromDB() {
        // TODO: implement
    }
}

