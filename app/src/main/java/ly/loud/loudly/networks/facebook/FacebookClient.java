package ly.loud.loudly.networks.facebook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.networks.facebook.entities.*;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

/**
 * Client for Facebook API
 *
 * @author Danil Kolikov
 */
public interface FacebookClient {
    String CLIENT_ID = "443913362466352";
    String LIKES_ENDPOINT = "likes";
    String SHARES_ENDPOINT = "sharedposts";

    @GET("me/feed?date_format=U&" +
            "fields=message,created_time,id,likes.limit(0).summary(true),shares,comments.limit(0).summary(true)," +
            "attachments%7Bmedia%7D")
    @NonNull
    Call<Data<List<Post>>> loadPosts(@Nullable @Query("since") Long since,
                                     @Nullable @Query("until") Long until,
                                     @NonNull @Query("access_token") String accessToken);

    @GET("?fields=likes.limit(0).summary(true),comments.limit(0).summary(true),shares")
    @NonNull
    Call<Map<String, Post>> getInfo(@NonNull @Query("ids") String ids,
                                   @NonNull @Query("access_token") String accessToken);

    @GET
    @NonNull
    Call<Data<List<Post>>> continueLoadPostsWithPagination(@NonNull @Url String url);

    @GET("{id}/comments?date_format=U&fields=message,from%7Bid%7D,created_time,id,comment_count,like_count,attachment")
    @NonNull
    Call<Data<List<FbComment>>> loadComments(@NonNull @Path("id") String id,
                                             @NonNull @Query("access_token") String accessToken);

    @GET("?fields=id,first_name,last_name,picture")
    @NonNull
    Call<Map<String, FbPerson>> getPersonsInfo(@NonNull @Query("ids") String ids,
                                               @NonNull @Query("access_token") String accessToken);

    @GET("{id}/{endpoint}")
    @NonNull
    Call<Data<List<FbPerson>>> getLikesOrShares(@NonNull @Path("id") String id,
                                                @NonNull @Path("endpoint") String endpoint,
                                                @NonNull @Query("access_token") String accessToken);

    @DELETE("{id}")
    Call<Result> deleteElement(@NonNull @Path("id") String id,
                               @NonNull @Query("access_token") String accessToken);

    @Multipart
    @POST("me/photos?published=true&no_story=true")
    @NonNull
    Call<ElementId> uploadPhoto(@NonNull @Part MultipartBody.Part file,
                                @Query("access_token") String accessToken);

    @GET("?fields=link,width,height")
    @NonNull
    Call<Map<String, Picture>> getPictureInfos(@NonNull @Query("ids") String ids,
                                               @NonNull @Query("access_token") String accessToken);

    @POST("me/feed")
    @NonNull
    Call<ElementId> uploadPost(@Nullable @Query("message") String message,
                               @Nullable @Query("object_attachment") String attachmentId,
                               @NonNull @Query("access_token") String accessToken);
}
