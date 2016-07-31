package ly.loud.loudly.application.models;

import android.graphics.Point;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.new_base.KeyKeeper;
import ly.loud.loudly.new_base.Link;
import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.new_base.Person;
import ly.loud.loudly.new_base.Info;
import ly.loud.loudly.networks.VK.VKClient;
import ly.loud.loudly.networks.VK.VKKeyKeeper;
import ly.loud.loudly.networks.VK.entities.*;
import ly.loud.loudly.new_base.Comment;
import ly.loud.loudly.new_base.SingleImage;
import ly.loud.loudly.new_base.SinglePost;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.LocalFile;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainImage;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.util.TimeInterval;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Single;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ly.loud.loudly.application.models.GetterModel.*;

public class VKModel implements NetworkContract {
    private static final String TAG = "VK_MODEL";

    @NonNull
    private final List<PlainPost> posts = new ArrayList<>();

    private int offset;

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private KeysModel keysModel;

    @NonNull
    private VKClient client;

    @Inject
    public VKModel(
            @NonNull Loudly loudlyApplication,
            @NonNull KeysModel keysModel,
            @NonNull VKClient client
    ) {
        this.loudlyApplication = loudlyApplication;
        this.keysModel = keysModel;
        this.client = client;
        loadFromDB();
        offset = 0;
    }

    @NonNull
    @Override
    public Single<Boolean> reset() {
        offset = 0;
        return Single.just(true);
    }

    /**
     * Load wrap from DataBase
     */
    private void loadFromDB() {
        // TODO: implement
    }

    @NonNull
    @Override
    @CheckResult
    public Single<SingleImage> upload(@NonNull PlainImage image) {
        return Single.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                return null;
            }
            if (!(image instanceof LocalFile)) {
                // Can't upload such image
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

            File file = new File(/* ToDo: get file path from URI */ "");

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
                        getId(), new Link(photo.id));
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

    @NonNull
    @Override
    @CheckResult
    public Single<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Single.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                return null;
            }
            Call<VKResponse<ly.loud.loudly.networks.VK.entities.Post>> call = client
                    .uploadPost(post.getText(), describeAttachments(post.getAttachments(), keyKeeper.getUserId()),
                            keyKeeper.getAccessToken());
            Response<VKResponse<ly.loud.loudly.networks.VK.entities.Post>> executed = call.execute();
            VKResponse<ly.loud.loudly.networks.VK.entities.Post> body = executed.body();
            if (body.error != null) {
                // ToDo: handle
                Log.e(TAG, body.error.errorMessage);
                return null;
            }
            if (body.response != null) {
                return new SinglePost(post, getId(), new Link(body.response.postId));
            }
            return null;
        });
    }

    @NonNull
    @Override
    @CheckResult
    public Single<Boolean> delete(@NonNull SinglePost post) {
        return Single.just(false);
    }

    @NonNull
    @Override
    @CheckResult
    public Single<List<PlainPost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Single.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: handle
                return Collections.emptyList();
            }
            Call<VKResponse<VKItems<Say>>> call;
            long currentTime = 0;
            do {
                call = client.getPosts(keyKeeper.getUserId(), offset, keyKeeper.getAccessToken());
                Response<VKResponse<VKItems<Say>>> execute = call.execute();
                VKResponse<VKItems<Say>> body = execute.body();
                if (body.error != null) {
                    // ToDo: Handle
                    Log.e(TAG, body.error.errorMessage);
                    return Collections.emptyList();
                }
                if (body.response != null) {
                    if (body.response.items.isEmpty()) {
                        break;
                    }
                    for (Say say : body.response.items) {
                        currentTime = say.date;
                        if (!timeInterval.contains(currentTime)) {
                            break;
                        }
                        offset++;
                        SinglePost post = new SinglePost(say.text, say.date, toAttachments(say),
                                null, getId(), new Link(say.id), getInfo(say));
                        posts.add(post);
                    }
                }
            } while (timeInterval.contains(currentTime));
            return posts;
        });
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

    @NonNull
    @Override
    @CheckResult
    public Single<List<Person>> getPersons(@NonNull SingleNetworkElement element, @RequestType int requestType) {
        return Single.fromCallable(() -> {
            final VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return Collections.emptyList();
            }

            String type, filter;
            if (element instanceof Post) {
                type = "post";
            } else if (element instanceof SingleImage) {
                type = "photo";
            } else if (element instanceof Comment) {
                type = "comment";
            } else {
                return Collections.emptyList();
            }
            switch (requestType) {
                case LIKES:
                    filter = "likes";
                    break;
                case SHARES:
                    filter = "copies";
                    break;
                default:
                    return Collections.emptyList();
            }
            Link elementLink = element.getLink();
            String elementId = elementLink.get();
            if (elementId == null) {
                return Collections.emptyList();
            }

            Call<VKResponse<VKItems<Profile>>> likersIds = client.getLikersIds(keyKeeper.getUserId(),
                    elementId, type, filter, keyKeeper.getAccessToken());
            try {
                Response<VKResponse<VKItems<Profile>>> executed = likersIds.execute();
                VKResponse<VKItems<Profile>> body = executed.body();
                if (body.error != null) {
                    // ToDo: Handle
                    Log.e(TAG, body.error.errorMessage);
                    return Collections.emptyList();
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
                        return Collections.emptyList();
                    }
                    if (personsBody.response != null) {
                        List<Person> persons = new ArrayList<>();
                        for (Profile profile : personsBody.response) {
                            persons.add(toPerson(profile));
                        }
                        return persons;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

    @NonNull
    private Person toPerson(Profile profile) {
        return new Person(profile.firstName, profile.lastName, profile.photo50, getId());
    }

    private int get(@Nullable Counter counter) {
        return counter == null ? 0 : counter.count;
    }

    private Info getInfo(@NonNull Say say) {
        return new Info(get(say.likes), get(say.reposts), get(say.comments));
    }

    @Nullable
    private SingleAttachment toAttachment(@NonNull Attachment attachment) {
        Photo photo = attachment.photo;
        if (photo != null) {
            return new SingleImage(attachment.photo.photo604, new Point(photo.widthPx, photo.heightPx),
                    getId(), new Link(photo.id));
        }
        return null;
    }

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
    private Profile getProfileById(@NonNull List<Profile> profiles, String id) {
        for (Profile profile : profiles) {
            if (profile.id.equals(id)) {
                return profile;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public Single<List<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Single.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return Collections.emptyList();
            }

            String id = Link.getLink(element.getLink());
            if (id == null) {
                return Collections.emptyList();
            }
            Call<VKResponse<VKItems<Say>>> call = client.getComments(
                    keyKeeper.getUserId(), id,
                    keyKeeper.getAccessToken());
            try {
                Response<VKResponse<VKItems<Say>>> executed = call.execute();
                VKResponse<VKItems<Say>> body = executed.body();
                if (body.error != null) {
                    // ToDo: Handle
                    Log.e(TAG, body.error.errorMessage);
                    return Collections.emptyList();
                }
                if (body.response != null && body.response.profiles != null) {
                    List<Comment> comments = new ArrayList<>();
                    List<Profile> profiles = body.response.profiles;
                    for (Say say : body.response.items) {
                        Profile profile = getProfileById(profiles, say.fromId);

                        Comment comment = new Comment(say.text, say.date, toAttachments(say),
                                toPerson(profile), getId(), new Link(say.id));
                        comment.setInfo(getInfo(say));

                        comments.add(comment);
                    }
                    return comments;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        });
    }

    @NonNull
    @Override
    @CheckResult
    public Single<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        if (!(keyKeeper instanceof VKKeyKeeper))
            throw new AssertionError("KeyKeeper must be VkKeyKeeper");

        keysModel.setVKKeyKeeper((VKKeyKeeper) keyKeeper);
        return Single.just(true);
    }

    @NonNull
    @Override
    @CheckResult
    public Single<Boolean> disconnect() {
        return keysModel.disconnectFromNetwork(getId());
    }

    @NonNull
    @Override
    public String getFullName() {
        return loudlyApplication.getString(R.string.network_vk);
    }

    @Override
    @CheckResult
    public boolean isConnected() {
        return keysModel.getVKKeyKeeper() != null;
    }

    @Override
    public int getId() {
        return Networks.VK;
    }
}

