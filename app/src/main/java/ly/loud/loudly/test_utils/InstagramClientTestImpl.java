package ly.loud.loudly.test_utils;

import android.support.annotation.NonNull;

import java.util.List;

import ly.loud.loudly.networks.instagram.InstagramClient;
import ly.loud.loudly.networks.instagram.entities.Data;
import ly.loud.loudly.networks.instagram.entities.InstagramComment;
import ly.loud.loudly.networks.instagram.entities.InstagramPerson;
import ly.loud.loudly.networks.instagram.entities.InstagramPost;
import retrofit2.Call;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public class InstagramClientTestImpl implements InstagramClient {
    @NonNull
    @Override
    public Call<Data<List<InstagramPost>>> loadPosts(@NonNull @Query("max_id") String maxId,
                                                     @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Data<List<InstagramPost>>> continueLoadPostsWithPagination(@NonNull @Url String url) {
        return null;
    }

    @NonNull
    @Override
    public Call<Data<List<InstagramPerson>>> getLikers(@NonNull @Path("id") String id,
                                                       @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Data<List<InstagramComment>>> getComments(@NonNull @Path("id") String id,
                                                          @NonNull @Query("access_token") String accessToken) {
        return null;
    }

    @NonNull
    @Override
    public Call<Data<InstagramPost>> getPost(@NonNull @Path("id") String id,
                                             @NonNull @Query("access_token") String accessToken) {
        return null;
    }
}
