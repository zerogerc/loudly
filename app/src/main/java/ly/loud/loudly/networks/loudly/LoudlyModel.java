package ly.loud.loudly.networks.loudly;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.util.TimeInterval;
import rx.Completable;
import rx.Observable;
import rx.Single;
import solid.collections.SolidList;

import static ly.loud.loudly.networks.Networks.LOUDLY;

public class LoudlyModel implements NetworkContract {

    @NonNull
    private Loudly loudlyApplication;

    @Inject
    public LoudlyModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @Network
    @Override
    public int getId() {
        return LOUDLY;
    }

    @NonNull
    @Override
    public String getFullName() {
        return loudlyApplication.getString(R.string.loudly);
    }

    @Override
    public int getNetworkIconResource() {
        return R.drawable.loudly_icon;
    }

    @Override
    public int getBrandColorResourcePrimary() {
        return R.color.accent;
    }

    @NonNull
    @Override
    public Single<String> getBeginAuthUrl() {
        return Single.just("");
    }

    @NonNull
    @Override
    public Single<KeyKeeper> proceedAuthUrls(@NonNull Observable<String> urls) {
        return Single.just(null);
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @NonNull
    @Override
    public Completable disconnect() {
        return Completable.complete();
    }

    @NonNull
    @Override
    public Observable<SingleImage> upload(@NonNull PlainImage image) {
        return Observable.empty();
    }

    @NonNull
    @Override
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Observable.empty();
    }

    @NonNull
    @Override
    public Completable delete(@NonNull SinglePost post) {
        return Completable.complete();
    }

    @NonNull
    @Override
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.empty();
    }

    @NonNull
    @Override
    public SolidList<SinglePost> getCachedPosts() {
        return SolidList.empty();
    }

    @NonNull
    @Override
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element,
                                                    @RequestType int requestType) {
        return Observable.empty();
    }

    @NonNull
    @Override
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.empty();
    }

    @NonNull
    @Override
    public Observable<List<Pair<SinglePost, Info>>> getUpdates(@NonNull SolidList<SinglePost> posts) {
        return Observable.empty();
    }

    @NonNull
    @Override
    public String getPersonPageUrl(@NonNull Person person) {
        return "";
    }

    @NonNull
    @Override
    public Single<String> getCommentUrl(@NonNull Comment comment, @NonNull SinglePost post) {
        return Single.just("");
    }
}
