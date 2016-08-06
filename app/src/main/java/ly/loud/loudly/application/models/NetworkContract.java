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
import rx.Observable;
import solid.collections.SolidList;

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
    Observable<Boolean> reset();

    /**
     * Upload image to network
     * @param image - image to load
     * @return id of image in given network
     */
    @CheckResult
    @NonNull
    Observable<SingleImage> upload(@NonNull PlainImage image);

    /**
     * Upload post to network
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
     * @param timeInterval - desirable interval for loading
     * @return loaded posts
     */
    @CheckResult
    @NonNull
    Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval);

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
     * Connect this network for proper work.
     * @param keyKeeper - auth token
     * @return  <code>true</code> if connected successfully
     */
    @CheckResult
    @NonNull
    Observable<Boolean> connect(@NonNull KeyKeeper keyKeeper);

    /**
     * Disconnect from network
     * @return <code>true</code> if disconnected successfully
     */
    @CheckResult
    @NonNull
    Observable<Boolean> disconnect();

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
