package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.networks.Facebook.FacebookAuthorizer;
import ly.loud.loudly.networks.Facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.Facebook.FacebookWrap;
import ly.loud.loudly.util.TimeInterval;
import rx.Single;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class FacebookModel implements NetworkContract {

    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private FacebookKeyKeeper keyKeeper;

    @Nullable
    private FacebookAuthorizer authorizer;

    @Nullable
    private FacebookWrap wrap;

    @Inject
    public FacebookModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }


    public FacebookWrap getWrap() {
        if (wrap == null) {
            this.wrap = new FacebookWrap();
        }
        return wrap;
    }

    @Override
    public Single<String> upload(@NonNull Image image) {
        return Single.just("");
    }

    @Override
    public Single<String> upload(@NonNull Post post) {
        return Single.just("");
    }

    @Override
    public Single<Boolean> delete(@NonNull Post post) {
        return Single.just(false);
    }

    @Override
    public Single<List<Post>> loadPosts(@NonNull TimeInterval timeInterval) {
        return null;
    }

    @Override
    public Single<List<Person>> getPersons(@NonNull SingleNetwork element, @PeopleGetterModel.RequestType int requestType) {
        return Single.just(Collections.emptyList());
    }
    
    @Override
    public Single<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        return Single.just(false);
    }

    @Override
    public Single<Boolean> disconnect() {
        return Single.just(false);
    }

    @Override
    public Single<Boolean> isConnected() {
        return Single.just(false);
    }

    @Override
    public int getId() {
        return Networks.FB;
    }
}
