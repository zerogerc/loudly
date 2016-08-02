package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import ly.loud.loudly.new_base.KeyKeeper;
import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.new_base.Person;
import ly.loud.loudly.new_base.Comment;
import ly.loud.loudly.new_base.SingleImage;
import ly.loud.loudly.new_base.SinglePost;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainImage;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.util.TimeInterval;
import rx.Single;

import static ly.loud.loudly.application.models.GetterModel.RequestType;

/**
 * Interface that all Network models must implement.
 */
//TODO: nonnull annoations
public interface NetworkContract {
    /**
     * Reset inner counters of modules
     *
     * @return True, if reset, False otherwise
     */
    @CheckResult
    @NonNull
    Single<Boolean> reset();

    /**
     * Upload image to network
     * @param image - image to load
     * @return id of image in given network
     */
    @CheckResult
    @NonNull
    Single<SingleImage> upload(@NonNull PlainImage image);

    /**
     * Upload post to network
     * @param post - post to load
     * @return id of post in network
     */
    @CheckResult
    @NonNull
    Single<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post);

    /**
     * Delete post from network
     */
    @CheckResult
    @NonNull
    Single<Boolean> delete(@NonNull SinglePost post);

    /**
     * Load posts from network
     * @param timeInterval - desirable interval for loading
     * @return loaded posts
     */
    @CheckResult
    @NonNull
    Single<List<PlainPost>> loadPosts(@NonNull TimeInterval timeInterval);

    /**
     * Get persons by request type. For example: peoples that like certain post.
     */
    @CheckResult
    @NonNull
    Single<List<Person>> getPersons(@NonNull SingleNetworkElement element, @RequestType int requestType);

    /**
     * Get comments for element of {@link SingleNetworkElement}.
     */
    @CheckResult
    @NonNull
    Single<List<Comment>> getComments(@NonNull SingleNetworkElement element);

    /**
     * Connect this network for proper work.
     * @param keyKeeper - auth token
     * @return  <code>true</code> if connected successfully
     */
    @CheckResult
    @NonNull
    Single<Boolean> connect(@NonNull KeyKeeper keyKeeper);

    /**
     * Disconnect from network
     * @return <code>true</code> if disconnected successfully
     */
    @CheckResult
    @NonNull
    Single<Boolean> disconnect();

    /**
     * User-readable full name of network
     */
    @CheckResult
    @NonNull
    String getFullName();

    /**
     * @return <code>true</code> if this network connected.
     */
    @CheckResult
    boolean isConnected();

    /**
     * @return id of network.
     */
    @CheckResult
    @Network
    int getId();
}
