package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

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
import rx.Observable;

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

    public FacebookModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
        loadFromDB();
    }

    /**
     * Load wrap from DataBase
     */
    private void loadFromDB() {

    }

    public FacebookWrap getWrap() {
        if (wrap == null) {
            this.wrap = new FacebookWrap();
        }
        return wrap;
    }

    @Override
    public long upload(Image image) {
        return 0;
    }

    @Override
    public long upload(Post post) {
        return 0;
    }

    @Override
    public void delete(Post post) {

    }

    @Override
    public Observable<List<Post>> loadPosts(TimeInterval timeInterval) {
        return null;
    }

    @Override
    public List<Person> getPersons(@NonNull SingleNetwork element, @PeopleGetterModel.RequestType int requestType) {
        return Collections.emptyList();
    }

    @Override
    public boolean connect(@NonNull KeyKeeper keyKeeper) {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public int getId() {
        return Networks.FB;
    }
}
