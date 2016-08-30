package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import ly.loud.loudly.networks.facebook.FacebookClient;
import ly.loud.loudly.networks.facebook.entities.Data;
import ly.loud.loudly.networks.facebook.entities.ElementId;
import ly.loud.loudly.networks.facebook.entities.FbComment;
import ly.loud.loudly.networks.facebook.entities.FbPerson;
import ly.loud.loudly.networks.facebook.entities.Picture;
import ly.loud.loudly.networks.facebook.entities.Post;
import ly.loud.loudly.networks.facebook.entities.Result;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public class FacebookClientTestImpl implements FacebookClient {

    @NonNull
    @Override
    public Call<Data<List<Post>>> loadPosts(@Nullable @Query("since") Long since, @Nullable @Query("until") Long until, @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Map<String, Post>> getInfo(@NonNull @Query("ids") String ids, @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Data<List<Post>>> continueLoadPostsWithPagination(@NonNull @Url String url) {
        return null;
    }

    @NonNull
    @Override
    public Call<Data<List<FbComment>>> loadComments(@NonNull @Path("id") String id, @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Map<String, FbPerson>> getPersonsInfo(@NonNull @Query("ids") String ids, @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Data<List<FbPerson>>> getLikesOrShares(@NonNull @Path("id") String id, @NonNull @Path("endpoint") String endpoint, @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @Override
    public Call<Result> deleteElement(@NonNull @Path("id") String id, @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<ElementId> uploadPhoto(@NonNull @Part MultipartBody.Part file, @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Map<String, Picture>> getPictureInfos(@NonNull @Query("ids") String ids, @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<ElementId> uploadPost(@Nullable @Query("message") String message, @Nullable @Query("object_attachment") String attachmentId, @NonNull @Query("access_token") String accessToken) {
        return null;
    }
}
