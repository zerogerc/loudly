package ly.loud.loudly.networks;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.List;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;
import rx.Single;
import solid.collections.SolidList;

import static ly.loud.loudly.application.models.GetterModel.RequestType;

/**
 * Interface that all Network models must implement.
 */
//TODO: nonnull annoations
public interface NetworkContract {

    /**
     * @return id of network.
     */
    @CheckResult
    @Network
    int getId();

    /**
     * User-readable full name of network
     */
    @CheckResult
    @NonNull
    String getFullName();

    /**
     * Get URL of initial web page for authorization
     *
     * @return Single url
     */
    @CheckResult
    @NonNull
    Single<String> getBeginAuthUrl();

    /**
     * Proceed urls of web pages, opened during user's authorization
     *
     * @param urls Observable of page urls
     * @return True, if authorization is successful, False otherwise
     */
    @CheckResult
    @NonNull
    Single<KeyKeeper> proceedAuthUrls(@NonNull Observable<String> urls);

    /**
     * @return <code>true</code> if this network connected.
     */
    @CheckResult
    boolean isConnected();

    /**
     * Disconnect from network
     *
     * @return <code>true</code> if disconnected successfully
     */
    @CheckResult
    @NonNull
    Single<Boolean> disconnect();

    /**
     * Upload image to network
     *
     * @param image - image to load
     * @return id of image in given network
     */
    @CheckResult
    @NonNull
    Observable<SingleImage> upload(@NonNull PlainImage image);

    /**
     * Upload post to network
     *
     * @param post - post to load
     * @return id of post in network
     */
    @CheckResult
    @NonNull
    Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post);

    /**
     * Delete post from network
     */
    @CheckResult
    @NonNull
    Observable<Boolean> delete(@NonNull SinglePost post);

    /**
     * Load posts from network
     *
     * @param timeInterval - desirable interval for loading
     * @return loaded posts
     */
    @CheckResult
    @NonNull
    Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval);

    /**
     * Get posts that previously been cached
     */
    @CheckResult
    @NonNull
    SolidList<SinglePost> getCachedPosts();

    /**
     * Get persons by request type. For example: peoples that like certain post.
     */
    @CheckResult
    @NonNull
    Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element, @RequestType int requestType);

    /**
     * Get comments for element of {@link SingleNetworkElement}.
     */
    @CheckResult
    @NonNull
    Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element);

    /**
     * Get new updates of specified post. Updates is new like, share or comment
     *
     * @param posts List of posts to find update
     * @return Observable from list of pairs. First element in pair is post,
     * second - difference in infos between stored and updated
     */
    @CheckResult
    @NonNull
    Observable<List<Pair<SinglePost, Info>>> getUpdates(@NonNull SolidList<SinglePost> posts);

    /**
     * Get url of person's page in this network
     *
     * @param person Person from this network
     * @return URL of page
     */
    @NonNull
    String getPersonPageUrl(@NonNull Person person);
}
