package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ly.loud.loudly.networks.vk.VKClient;
import ly.loud.loudly.networks.vk.entities.Photo;
import ly.loud.loudly.networks.vk.entities.PhotoUploadServer;
import ly.loud.loudly.networks.vk.entities.PhotoUploadServerResponse;
import ly.loud.loudly.networks.vk.entities.Post;
import ly.loud.loudly.networks.vk.entities.Profile;
import ly.loud.loudly.networks.vk.entities.Say;
import ly.loud.loudly.networks.vk.entities.VKItems;
import ly.loud.loudly.networks.vk.entities.VKResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

// TODO: implement
public class VKClientTestImpl implements VKClient {
    @NonNull
    @Override
    public Call<VKResponse<VKItems<Say>>> getComments(
            @Query("owner_id") @NonNull String ownerId,
            @Query("post_id") @NonNull String postId,
            @Query("access_token") @NonNull String token
    ) {
        return null;
    }

    @NonNull
    @Override
    public Call<VKResponse<VKItems<Say>>> getPosts(
            @Query("owner_id") @NonNull String ownerId,
            @Query("offset") int offset,
            @Query("access_token") @NonNull String token
    ) {
        return null;
    }

    @NonNull
    @Override
    public Call<VKResponse<VKItems<Profile>>> getLikersIds(
            @Query("owner_id") @NonNull String ownerId,
            @Query("item_id") @NonNull String itemId,
            @Query("type") @NonNull String type,
            @Query("filter") @Nullable String filter,
            @Query("access_token") @NonNull String token
    ) {
        return null;
    }

    @NonNull
    @Override
    public Call<VKResponse<List<Profile>>> getProfiles(
            @Query("user_ids") @NonNull String ids,
            @Query("access_token") @NonNull String token
    ) {
        return null;
    }

    @NonNull
    @Override
    public Call<VKResponse<List<Say>>> getPostsByIds(
            @Query("posts") @NonNull String ids,
            @Query("access_token") @NonNull String token
    ) {
        return null;
    }

    @NonNull
    @Override
    public Call<VKResponse<Integer>> deletePost(
            @Query("owner_id") @NonNull String ownerId,
            @Query("post_id") @NonNull String postId,
            @Query("access_token") @NonNull String token
    ) {
        return null;
    }

    @NonNull
    @Override
    public Call<VKResponse<Post>> uploadPost(
            @Query("message") @Nullable String message,
            @Query("attachment") @Nullable String attachmentIds,
            @Query("access_token") @NonNull String token
    ) {
        return null;
    }

    @Override
    public Call<VKResponse<PhotoUploadServer>> getPhotoUploadServer
            (@Query("user_id") String userId,
             @Query("access_token") String token
            ) {
        return null;
    }

    @Override
    public Call<PhotoUploadServerResponse> uploadPhoto(
            @NonNull @Url String url,
            @NonNull @Part MultipartBody.Part file
    ) {
        return null;
    }

    @Override
    public Call<VKResponse<List<Photo>>> saveWallPhoto(
            @Query("user_id") String userId,
            @Query("photo") String photo,
            @Query("server") String server,
            @Query("hash") String hash,
            @Query("access_token") String token
    ) {
        return null;
    }
}
