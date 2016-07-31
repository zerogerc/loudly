package ly.loud.loudly.application.models;

import android.graphics.Point;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.networks.VK.VKClient;
import ly.loud.loudly.networks.VK.VKKeyKeeper;
import ly.loud.loudly.networks.VK.entities.Attachment;
import ly.loud.loudly.networks.VK.entities.Counter;
import ly.loud.loudly.networks.VK.entities.Photo;
import ly.loud.loudly.networks.VK.entities.Profile;
import ly.loud.loudly.networks.VK.entities.Say;
import ly.loud.loudly.networks.VK.entities.VKItems;
import ly.loud.loudly.networks.VK.entities.VKResponse;
import ly.loud.loudly.util.TimeInterval;
import retrofit2.Call;
import retrofit2.Response;
import rx.Single;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.RequestType;
import static ly.loud.loudly.application.models.GetterModel.SHARES;

public class VKModel implements NetworkContract {
    private static final String TAG = "VK_MODEL";

    @NonNull
    private final List<Post> posts = new ArrayList<>();

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
    public Single<String> upload(@NonNull Image image) {
        return Single.just("");
    }

    @NonNull
    @Override
    @CheckResult
    public Single<String> upload(@NonNull Post post) {
        return Single.just("");
    }

    @NonNull
    @Override
    @CheckResult
    public Single<Boolean> delete(@NonNull Post post) {
        return Single.just(false);
    }

    @NonNull
    @Override
    @CheckResult
    public Single<List<Post>> loadPosts(@NonNull TimeInterval timeInterval) {
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
                try {
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
                            Post post = new Post(say.text, say.date, null, Networks.VK, new Link(say.id));
                            post.setInfo(getInfo(say));
                            setAttachments(post, say);
                            posts.add(post);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return posts;
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
    public Single<List<Person>> getPersons(@NonNull SingleNetwork element, @RequestType int requestType) {
        return Single.fromCallable(() -> {
            final VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return Collections.emptyList();
            }

            String type, filter;
            if (element instanceof Post) {
                type = "post";
            } else if (element instanceof Image) {
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
            if (elementLink == null) {
                return Collections.emptyList();
            }
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
        return new Person(profile.firstName, profile.lastName, profile.photo50, Networks.VK);
    }

    private int get(@Nullable Counter counter) {
        return counter == null ? 0 : counter.count;
    }

    private Info getInfo(@NonNull Say say) {
        return new Info(get(say.likes), get(say.reposts), get(say.comments));
    }

    @Nullable
    private ly.loud.loudly.base.attachments.Attachment toAttachment(@NonNull Attachment attachment) {
        Photo photo = attachment.photo;
        if (photo != null) {
            return new Image(attachment.photo.photo604, new Point(photo.widthPx, photo.heightPx),
                    Networks.VK, new Link(photo.id));
        }
        return null;
    }

    private void setAttachments(@NonNull ly.loud.loudly.base.says.Say say, @NonNull Say loaded) {
        if (loaded.attachments == null) {
            return;
        }
        for (Attachment attachment : loaded.attachments) {
            ly.loud.loudly.base.attachments.Attachment filled = toAttachment(attachment);
            if (filled == null) {
                continue;
            }
            say.addAttachment(filled);
        }
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
    public Single<List<Comment>> getComments(@NonNull SingleNetwork element) {
        return Single.fromCallable(() -> {
            VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return Collections.emptyList();
            }

            if (element.getLink() == null) {
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

                        Comment comment = new Comment(say.text, say.date,
                                toPerson(profile), Networks.VK, new Link(say.id));
                        comment.setInfo(getInfo(say));
                        setAttachments(comment, say);

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
        if (!(keyKeeper instanceof ly.loud.loudly.networks.VK.VKKeyKeeper))
            throw new AssertionError("KeyKeeper must be VkKeyKeeper");

        keysModel.setVKKeyKeeper((VKKeyKeeper) keyKeeper);
        return Single.just(true);
    }

    @NonNull
    @Override
    @CheckResult
    public Single<Boolean> disconnect() {
        return keysModel.disconnectFromNetwork(Networks.VK);
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

