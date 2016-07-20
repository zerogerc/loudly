package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.networks.Instagram.InstagramAuthorizer;
import ly.loud.loudly.networks.Instagram.InstagramKeyKeeper;
import ly.loud.loudly.networks.Instagram.InstagramWrap;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;
import rx.Single;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class InstagramModel implements NetworkContract {

    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private InstagramKeyKeeper keyKeeper;

    @Nullable
    private InstagramAuthorizer authorizer;

    @Nullable
    private InstagramWrap wrap;

    public InstagramModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
        loadFromDB();
    }

    /**
     * Load wrap from DataBase
     */
    private void loadFromDB() {

    }

    public InstagramWrap getWrap() {
        if (wrap == null) {
            this.wrap = new InstagramWrap();
        }
        return wrap;
    }

    @Override
    public Single<Long> upload(Image image) {
        return null;
    }

    @Override
    public Single<Long> upload(Post post) {
        return null;
    }

    @Override
    public Single<Long> delete(Post post) {
        return null;
    }

    @Override
    public Observable<List<Post>> loadPosts(TimeInterval timeInterval) {
        return null;
    }

    @Override
    public Single<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        return null;
    }

    @Override
    public Single<Boolean> disconnect() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public int getId() {
        return Networks.INSTAGRAM;
    }
}
