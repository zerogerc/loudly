package ly.loud.loudly.networks.vk;

import android.graphics.Point;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.KeysModel;
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
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.networks.vk.entities.Attachment;
import ly.loud.loudly.networks.vk.entities.Counter;
import ly.loud.loudly.networks.vk.entities.Photo;
import ly.loud.loudly.networks.vk.entities.PhotoUploadServer;
import ly.loud.loudly.networks.vk.entities.PhotoUploadServerResponse;
import ly.loud.loudly.networks.vk.entities.Profile;
import ly.loud.loudly.networks.vk.entities.Say;
import ly.loud.loudly.networks.vk.entities.VKItems;
import ly.loud.loudly.networks.vk.entities.VKResponse;
import ly.loud.loudly.util.NetworkUtils;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Single;
import solid.collections.SolidList;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.RequestType;
import static ly.loud.loudly.application.models.GetterModel.SHARES;
import static ly.loud.loudly.util.ListUtils.asSolidList;

public class VKModel implements NetworkContract {
    private static final String TAG = "VK_MODEL";
    static final String RESPONSE_URL = "https://oauth.vk.com/blank.html";
    static final String AUTHORIZE_URL = "https://oauth.vk.com/authorize";

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private KeysModel keysModel;

    @NonNull
    private VKClient client;

    @NonNull
    private final List<SinglePost> cached;

    @Inject
    public VKModel(
            @NonNull Loudly loudlyApplication,
            @NonNull KeysModel keysModel,
            @NonNull VKClient client
    ) {
        this.loudlyApplication = loudlyApplication;
        this.keysModel = keysModel;
        this.client = client;
        cached = new ArrayList<>();
    }

    @Network
    @Override
    public int getId() {
        return Networks.VK;
    }

    @Override
    @NonNull
    public String getFullName() {
        return loudlyApplication.getString(R.string.network_vk);
    }

    @Override
    @NonNull
    public Single<String> getBeginAuthUrl() {
        return Single.fromCallable(() ->
                new Query(AUTHORIZE_URL)
                        .addParameter("client_id", VKClient.CLIENT_ID)
                        .addParameter("redirect_uri", RESPONSE_URL)
                        .addParameter("display_type", "mobile")
                        .addParameter("scope", "wall,photos")
                        .addParameter("response_type", "token")
                        .toURL());
    }

    @Override
    @NonNull
    public Single<KeyKeeper> proceedAuthUrls(@NonNull Observable<String> urls) {
        return urls
                .takeFirst(url -> url.startsWith(RESPONSE_URL))
                .toSingle()
                .map(url -> {
                    Query response = Query.fromResponseUrl(url);
                    if (response == null) {
                        // ToDo: handle
                        return null;
                    }
                    String accessToken = response.getParameter("access_token");
                    String userId = response.getParameter("user_id");
                    if (accessToken == null || userId == null) {
                        // ToDo: handle
                        return null;
                    }
                    return new VKKeyKeeper(accessToken, userId);
                });
    }

    @Override
    @CheckResult
    public boolean isConnected() {
        return keysModel.getVKKeyKeeper() != null;
    }

    @Override
    @CheckResult
    @NonNull
    public Single<Boolean> disconnect() {
        return Single.just(true);
    }

    @Override
    @CheckResult
    @NonNull
    public Observable<SingleImage> upload(@NonNull PlainImage image) {
        return Observable.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                return null;
            }
            String url = image.getUrl();
            if (url == null) {
                return null;
            }
            Call<VKResponse<PhotoUploadServer>> getServerCall =
                    client.getPhotoUploadServer(keyKeeper.getUserId(), keyKeeper.getAccessToken());
            Response<VKResponse<PhotoUploadServer>> serverGot = getServerCall.execute();
            VKResponse<PhotoUploadServer> serverGotBody = serverGot.body();
            if (serverGotBody.error != null) {
                // ToDo: Handle
                Log.e(TAG, serverGotBody.error.errorMessage);
                return null;
            }
            if (serverGotBody.response == null) {
                // Impossible
                return null;
            }

            File file = new File(url);

            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("photo", file.getName(), requestBody);
            Call<PhotoUploadServerResponse> uploadPhotoCall =
                    client.uploadPhoto(serverGotBody.response.uploadUrl, part);
            Response<PhotoUploadServerResponse> photoUploaded = uploadPhotoCall.execute();
            PhotoUploadServerResponse serverResponse = photoUploaded.body();
            if (serverResponse == null) {
                return null;
            }
            Call<VKResponse<List<Photo>>> savePhotoCall = client.saveWallPhoto(
                    keyKeeper.getUserId(), serverResponse.photo, serverResponse.server, serverResponse.hash,
                    keyKeeper.getAccessToken());

            Response<VKResponse<List<Photo>>> executed = savePhotoCall.execute();
            VKResponse<List<Photo>> body = executed.body();
            if (body.error != null) {
                // ToDo: handle
                Log.e(TAG, body.error.errorMessage);
                return null;
            }
            if (body.response != null) {
                Photo photo = body.response.get(0);
                return new SingleImage(photo.photo604, new Point(photo.widthPx, photo.heightPx),
                        getId(), photo.id);
            }
            // ToDo: Null? really?
            return null;
        });
    }

    @Nullable
    private String describeAttachments(@NonNull List<SingleAttachment> attachments,
                                       @NonNull String userId) {
        List<String> result = new ArrayList<>();
        for (SingleAttachment attachment : attachments) {
            String link = userId + "_" + attachment.getLink();
            if (attachment instanceof SingleImage) {
                link = "photo" + link;
            }
            result.add(link);
        }
        if (result.isEmpty()) {
            return null;
        }
        return toCommaSeparated(result);
    }

    @Override
    @CheckResult
    @NonNull
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Observable.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                return null;
            }
            Call<VKResponse<ly.loud.loudly.networks.vk.entities.Post>> call = client
                    .uploadPost(post.getText(), describeAttachments(post.getAttachments(), keyKeeper.getUserId()),
                            keyKeeper.getAccessToken());
            Response<VKResponse<ly.loud.loudly.networks.vk.entities.Post>> executed = call.execute();
            VKResponse<ly.loud.loudly.networks.vk.entities.Post> body = executed.body();
            if (body.error != null) {
                // ToDo: handle
                Log.e(TAG, body.error.errorMessage);
                return null;
            }
            if (body.response != null) {
                return new SinglePost(post, getId(), body.response.postId);
            }
            return null;
        });
    }

    @Override
    @CheckResult
    @NonNull
    public Observable<Boolean> delete(@NonNull SinglePost post) {
        return Observable.just(false);
    }

    @Override
    @CheckResult
    @NonNull
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.fromCallable(() -> {
            if (cached.isEmpty()) {
                List<SinglePost> downloaded = downloadPosts(0, timeInterval);
                cached.addAll(downloaded);
                return asSolidList(downloaded);
            }
            NetworkUtils.DividedList dividedList = NetworkUtils.divideListOfCachedPosts(cached, timeInterval);

            List<SinglePost> before = downloadPosts(0, dividedList.before);
            List<SinglePost> after = downloadPosts(cached.size() + before.size(),
                    dividedList.after);
            cached.addAll(before);
            cached.addAll(after);
            Collections.sort(cached);

            List<SinglePost> result = new ArrayList<>();
            result.addAll(before);
            result.addAll(dividedList.cached);
            result.addAll(after);
            return asSolidList(result);
        });
    }

    @NonNull
    @Override
    public SolidList<SinglePost> getCachedPosts() {
        return asSolidList(cached);
    }

    @NonNull
    private List<SinglePost> downloadPosts(int offset, @NonNull TimeInterval timeInterval) throws IOException {
        if (timeInterval.from >= timeInterval.to) {
            return SolidList.empty();
        }
        VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
        if (keyKeeper == null) {
            // ToDo: handle
            return SolidList.empty();
        }
        Call<VKResponse<VKItems<Say>>> call;
        long currentTime = 0;
        List<SinglePost> posts = new ArrayList<>();
        do {
            Log.i(TAG, "DOWNLOADING");
            call = client.getPosts(keyKeeper.getUserId(), offset, keyKeeper.getAccessToken());
            Response<VKResponse<VKItems<Say>>> execute = call.execute();
            VKResponse<VKItems<Say>> body = execute.body();
            if (body.error != null) {
                // ToDo: Handle
                Log.e(TAG, body.error.errorMessage);
                return posts;
            }
            if (body.response != null) {
                if (body.response.items.isEmpty()) {
                    break;
                }
                for (Say say : body.response.items) {
                    currentTime = say.date;
                    if (currentTime > timeInterval.to) {
                        break;
                    }
                    offset++;
                    SinglePost post = new SinglePost(say.text, say.date, toAttachments(say),
                            null, getId(), say.id, getInfo(say));
                    posts.add(post);
                }
            }

        } while (timeInterval.contains(currentTime));
        return posts;
    }

    @NonNull
    private String toCommaSeparated(@NonNull List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string);
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    @Override
    @CheckResult
    @NonNull
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element, @RequestType int requestType) {
        return Observable.fromCallable(() -> {
            final VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return SolidList.empty();
            }

            String type, filter;
            if (element instanceof SinglePost) {
                type = "post";
            } else if (element instanceof SingleImage) {
                type = "photo";
            } else if (element instanceof Comment) {
                type = "comment";
            } else {
                return SolidList.empty();
            }
            switch (requestType) {
                case LIKES:
                    filter = "likes";
                    break;
                case SHARES:
                    filter = "copies";
                    break;
                default:
                    return SolidList.empty();
            }
            String elementId = element.getLink();

            Call<VKResponse<VKItems<Profile>>> likersIds = client.getLikersIds(keyKeeper.getUserId(),
                    elementId, type, filter, keyKeeper.getAccessToken());
            Response<VKResponse<VKItems<Profile>>> executed = likersIds.execute();
            VKResponse<VKItems<Profile>> body = executed.body();
            if (body.error != null) {
                // ToDo: Handle
                Log.e(TAG, body.error.errorMessage);
                return SolidList.empty();
            }
            if (body.response != null) {
                VKItems<Profile> response = body.response;
                List<String> ids = new ArrayList<>();
                for (Profile profile : response.items) {
                    ids.add(profile.id);
                }
                Call<VKResponse<List<Profile>>> profiles = client.getProfiles(toCommaSeparated(ids),
                        keyKeeper.getAccessToken());
                Response<VKResponse<List<Profile>>> gotPerson = profiles.execute();
                VKResponse<List<Profile>> personsBody = gotPerson.body();
                if (personsBody.error != null) {
                    // ToDo: Handle
                    Log.e(TAG, personsBody.error.errorMessage);
                    return SolidList.empty();
                }
                if (personsBody.response != null) {
                    List<Person> persons = new ArrayList<>();
                    for (Profile profile : personsBody.response) {
                        persons.add(toPerson(profile));
                    }
                    return asSolidList(persons);
                }
            }
            return SolidList.empty();
        });
    }

    @NonNull
    private Person toPerson(Profile profile) {
        return new Person(profile.firstName, profile.lastName, profile.photo50, getId());
    }

    private int get(@Nullable Counter counter) {
        return counter == null ? 0 : counter.count;
    }

    @NonNull
    private Info getInfo(@NonNull Say say) {
        return new Info(get(say.likes), get(say.reposts), get(say.comments));
    }

    @Nullable
    private SingleAttachment toAttachment(@NonNull Attachment attachment) {
        Photo photo = attachment.photo;
        if (photo != null) {
            return new SingleImage(attachment.photo.photo604, new Point(photo.widthPx, photo.heightPx),
                    getId(), photo.id);
        }
        return null;
    }

    @NonNull
    private ArrayList<SingleAttachment> toAttachments(@NonNull Say loaded) {
        if (loaded.attachments == null) {
            return new ArrayList<>();
        }
        ArrayList<SingleAttachment> attachments = new ArrayList<>();
        for (Attachment attachment : loaded.attachments) {
            SingleAttachment filled = toAttachment(attachment);
            if (filled == null) {
                continue;
            }
            attachments.add(filled);
        }
        return attachments;
    }

    @Nullable
    private Profile getProfileById(@NonNull List<Profile> profiles, @NonNull String id) {
        for (Profile profile : profiles) {
            if (profile.id.equals(id)) {
                return profile;
            }
        }
        return null;
    }

    @Override
    @NonNull
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return SolidList.empty();
            }

            String id = element.getLink();

            Call<VKResponse<VKItems<Say>>> call = client.getComments(
                    keyKeeper.getUserId(), id,
                    keyKeeper.getAccessToken());
            try {
                Response<VKResponse<VKItems<Say>>> executed = call.execute();
                VKResponse<VKItems<Say>> body = executed.body();
                if (body.error != null) {
                    // ToDo: Handle
                    Log.e(TAG, body.error.errorMessage);
                    return SolidList.empty();
                }
                if (body.response != null && body.response.profiles != null) {
                    List<Comment> comments = new ArrayList<>();
                    List<Profile> profiles = body.response.profiles;
                    for (Say say : body.response.items) {
                        Profile profile = getProfileById(profiles, say.fromId);

                        Comment comment = new Comment(say.text, say.date, toAttachments(say),
                                toPerson(profile), getId(), say.id, getInfo(say));
                        comments.add(comment);
                    }
                    return asSolidList(comments);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return SolidList.empty();
        });
    }
}

