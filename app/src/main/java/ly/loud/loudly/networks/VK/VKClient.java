package ly.loud.loudly.networks.VK;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.networks.VK.entities.*;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

/**
 * Client for VK api
 *
 * @author Danil Kolikov
 */
public interface VKClient {
    /**
     * Get comments for post
     *
     * @param ownerId ID of post owner
     * @param postId  ID of post
     * @param token   Access token
     * @return Response from network
     * @see <a href=https://new.vk.com/dev/wall.getComments>VK api</a>
     */
    @GET("wall.getComments?need_likes=1&count=20&sort=asc&preview_length=0&extended=1")
    @NonNull
    Call<VKResponse<VKItems<Say>>> getComments(@Query("owner_id") @NonNull String ownerId,
                                               @Query("post_id") @NonNull String postId,
                                               @Query("access_token") @NonNull String token);

    /**
     * Get posts of user
     *
     * @param ownerId User to load posts
     * @param offset  Offset from first post (0 based)
     * @param token   Access token
     * @return Response from network
     * @see <a href=https://new.vk.com/dev/wall.get>VK api</a>
     */
    @GET("wall.get?filter=owner&count=10")
    @NonNull
    Call<VKResponse<VKItems<Say>>> getPosts(@Query("owner_id") @NonNull String ownerId,
                                            @Query("offset") int offset,
                                            @Query("access_token") @NonNull String token);

    /**
     * Get ids of people who liked/shared post/image/etc
     *
     * @param ownerId ID of owner
     * @param itemId  ID of item
     * @param type    Type of item. Supported types: post, comment, photo, note, photo_comment, etc
     * @param filter  likes (all users) / copies (only shared)
     * @param token   Access token
     * @return Response from network
     * @see <a href=https://new.vk.com/dev/likes.getList>VK api</a>
     */
    @GET("likes.getList?extended=1")
    @NonNull
    Call<VKResponse<VKItems<Profile>>> getLikersIds(@Query("owner_id") @NonNull String ownerId,
                                                    @Query("item_id") @NonNull String itemId,
                                                    @Query("type") @NonNull String type,
                                                    @Query("filter") @Nullable String filter,
                                                    @Query("access_token") @NonNull String token);

    /**
     * Get user's info
     *
     * @param ids   IDs of users, comma-separated
     * @param token Access token
     * @return Response from network
     * @see <a href=https://new.vk.com/dev/users.get>VK api</a>
     */
    @GET("users.get?fields=photo_50")
    @NonNull
    Call<VKResponse<List<Profile>>> getProfiles(@Query("user_ids") @NonNull String ids,
                                                @Query("access_token") @NonNull String token);

    /**
     * Get information about posts
     *
     * @param ids   IDs of posts in format [userId]_[postId], comma-separated
     * @param token Access token
     * @return Response from network
     * @see <a href=https://new.vk.com/dev/wall.getById>VK api</a>
     */
    @GET("wall.getById")
    @NonNull
    Call<VKResponse<List<Say>>> getPostsByIds(@Query("posts") @NonNull String ids,
                                              @Query("access_token") @NonNull String token);

    /**
     * Delete post
     *
     * @param ownerId Owner's id
     * @param postId  ID of post
     * @param token   Access token
     * @return Response from network, 1 if succeded
     * @see <a href=https://new.vk.com/dev/wall.delete>VK api</a>
     */
    @GET("wall.delete")
    @NonNull
    Call<VKResponse<Integer>> deletePost(@Query("owner_id") @NonNull String ownerId,
                                         @Query("post_id") @NonNull String postId,
                                         @Query("access_token") @NonNull String token);

    /**
     * Upload post
     *
     * @param message       Message
     * @param attachmentIds Ids of attachments in format [type][userId]_[attachmentId], comma-separated
     * @param token         Access token
     * @return Response from network, ID of uploaded post if succeeded
     * @see <a href=https://new.vk.com/dev/wall.post>VK api</a>
     */
    @POST("wall.post")
    @NonNull
    Call<VKResponse<Post>> uploadPost(@Query("message") @Nullable String message,
                                      @Query("attachment") @Nullable String attachmentIds,
                                      @Query("access_token") @NonNull String token);
}
