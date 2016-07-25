package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Post;
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
    Single<Boolean> reset();

    /**
     * Upload image to network
     * @param image - image to load
     * @return id of image in given network
     */
    @CheckResult
    Single<String> upload(@NonNull Image image);

    /**
     * Upload post to network
     * @param post - post to load
     * @return id of post in network
     */
    @CheckResult
    Single<String> upload(@NonNull Post post);

    /**
     * Delete post from network
     */
    @CheckResult
    Single<Boolean> delete(@NonNull Post post);

    /**
     * Load posts from network
     * @param timeInterval - desirable interval for loading
     * @return loaded posts
     */
    @CheckResult
    Single<List<Post>> loadPosts(@NonNull TimeInterval timeInterval);

    /**
     * Get persons by request type. For example: peoples that like certain post.
     */
    @CheckResult
    Single<List<Person>> getPersons(@NonNull SingleNetwork element, @RequestType int requestType);

    /**
     * Get comments for element of {@link SingleNetwork}.
     */
    @CheckResult
    Single<List<Comment>> getComments(@NonNull SingleNetwork element);

    /**
     * Connect this network for proper work.
     * @param keyKeeper - auth token
     * @return  <code>true</code> if connected successfully
     */
    @CheckResult
    Single<Boolean> connect(@NonNull KeyKeeper keyKeeper);

    /**
     * Disconnect from network
     * @return <code>true</code> if disconnected successfully
     */
    @CheckResult
    Single<Boolean> disconnect();


    /**
     * @return <code>true</code> if this network connected.
     */
    @CheckResult
    boolean isConnected();

    /**
     * @return id of network.
     */
    // TODO: intDef
    int getId();
}
