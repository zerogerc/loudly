package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.NetworkDescription;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.networks.VK.VKKeyKeeper;
import ly.loud.loudly.util.Network;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import rx.Single;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.RequestType;
import static ly.loud.loudly.application.models.GetterModel.SHARES;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class VKModel implements NetworkContract {

    private static final int NETWORK = Networks.VK;
    private static final String TAG = "VK_WRAP_TAG";
    private static final String API_VERSION = "5.40";
    private static final String MAIN_SERVER = "https://api.vk.com/method/";
    private static final String POST_METHOD = "wall.post";
    private static final String GET_METHOD = "wall.getById";
    private static final String DELETE_METHOD = "wall.delete";
    private static final String LOAD_POSTS_METHOD = "wall.get";
    private static final String PHOTO_UPLOAD_METHOD = "photos.getWallUploadServer";
    private static final String SAVE_PHOTO_METHOD = "photos.saveWallPhoto";
    private static final String ACCESS_TOKEN = "access_token";
    private static final NetworkDescription DESCRIPTION = new NetworkDescription() {
        @Override
        public boolean canPost() {
            return true;
        }

        @Override
        public boolean canDelete() {
            return true;
        }
    };

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private KeysModel keysModel;

    @Inject
    public VKModel(
            @NonNull Loudly loudlyApplication,
            @NonNull KeysModel keysModel
    ) {
        this.loudlyApplication = loudlyApplication;
        this.keysModel = keysModel;
        loadFromDB();
    }

    /**
     * Load wrap from DataBase
     */
    private void loadFromDB() {
        // TODO: implement
    }

    @Override
    @CheckResult
    public Single<String> upload(@NonNull Image image) {
        return Single.just("");
    }

    @Override
    @CheckResult
    public Single<String> upload(@NonNull Post post) {
        return Single.just("");
    }

    @Override
    @CheckResult
    public Single<Boolean> delete(@NonNull Post post) {
        return Single.just(false);
    }

    @Override
    @CheckResult
    public Single<List<Post>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Single.just(Collections.emptyList());
    }

    @Override
    @CheckResult
    public Single<List<Person>> getPersons(@NonNull SingleNetwork element, @RequestType int requestType) {
        return isConnected()
                .map(isConnected -> {
                    if (!isConnected) {
                        return Collections.emptyList();
                    }

                    final VKKeyKeeper keyKeeper = keysModel.getVKKeyKeeper();
                    assert keyKeeper != null;
                    // TODO: handle errors from keykeeper

                    try {
                        Query query = makeSignedAPICall("likes.getList", keyKeeper);
                        String type;
                        if (element instanceof Post) {
                            type = "post";
                        } else if (element instanceof Image) {
                            type = "photo";
                        } else if (element instanceof Comment) {
                            type = "comment";
                        } else {
                            return new LinkedList<>();
                        }

                        query.addParameter("type", type);
                        query.addParameter("owner_id", keyKeeper.getUserId());
                        query.addParameter("item_id", element.getLink());
                        String filter;
                        switch (requestType) {
                            case LIKES:
                                filter = "likes";
                                break;
                            case SHARES:
                                filter = "copies";
                                break;
                            default:
                                filter = "";
                                break;
                        }
                        query.addParameter("filter", filter);
                        query.addParameter("extended", 1);
                        // TODO: 12/3/2015 Add offset here

                        String response = Network.makeGetRequest(query);

                        Query getPeopleQuery = makeSignedAPICall("users.get", keyKeeper);

                        JSONObject parser;
                        try {
                            parser = new JSONObject(response).getJSONObject("response");
                            JSONArray likers = parser.getJSONArray("items");

                            if (likers.length() == 0) {
                                return Collections.emptyList();
                            }

                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < likers.length(); i++) {
                                long id = likers.getJSONObject(i).getLong("id");
                                sb.append(id);
                                sb.append(',');
                            }
                            sb.delete(sb.length() - 1, sb.length());

                            getPeopleQuery.addParameter("user_ids", sb);
                            getPeopleQuery.addParameter("fields", "photo_50");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return Collections.emptyList();
                        }

                        response = Network.makeGetRequest(getPeopleQuery);

                        LinkedList<Person> result = new LinkedList<>();

                        JSONArray people;
                        try {
                            people = new JSONObject(response).getJSONArray("response");
                            for (int i = 0; i < people.length(); i++) {
                                JSONObject person = people.getJSONObject(i);
                                String id = person.getString("id");
                                String firstName = person.getString("first_name");
                                String lastName = person.getString("last_name");
                                String photoURL = person.getString("photo_50");

                                Person person1 = new Person(firstName, lastName, photoURL, getId());
                                person1.setId(id);
                                result.add(person1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return new LinkedList<>();
                        }

                        return result;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Collections.emptyList();
                    }
                });
    }

    @Override
    public Single<List<Comment>> getComments(@NonNull SingleNetwork element) {
        return isConnected()
                .map(isConnected -> {
                    if (!isConnected) {
                        return Collections.emptyList();
                    }

                    // TODO: implement
                    return Collections.emptyList();
                });
    }

    @Override
    @CheckResult
    public Single<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        if (!(keyKeeper instanceof ly.loud.loudly.networks.VK.VKKeyKeeper))
            throw new AssertionError("KeyKeeper must be VkKeyKeeper");

        keysModel.setVKKeyKeeper((VKKeyKeeper) keyKeeper);
        return Single.just(true);
    }

    @Override
    @CheckResult
    public Single<Boolean> disconnect() {
        return keysModel.disconnectFromNetwork(Networks.VK);
    }

    @Override
    @CheckResult
    public Single<Boolean> isConnected() {
        // TODO: change this
        return Single.just(true);
    }

    @Override
    public int getId() {
        return Networks.VK;
    }

    private Query makeAPICall(String method) {
        Query query = new Query(MAIN_SERVER + method);
        query.addParameter("v", API_VERSION);
        return query;
    }

    private Query makeSignedAPICall(String method, KeyKeeper keyKeeper) {
        Query query = makeAPICall(method);
        query.addParameter(ACCESS_TOKEN, ((VKKeyKeeper) keyKeeper).getAccessToken());
        return query;
    }
}

