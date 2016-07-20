package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
import rx.Observable;

import static ly.loud.loudly.application.models.PeopleGetterModel.LIKES;
import static ly.loud.loudly.application.models.PeopleGetterModel.RequestType;
import static ly.loud.loudly.application.models.PeopleGetterModel.SHARES;

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

    @Nullable
    private VKKeyKeeper keyKeeper;

    public VKModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
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
    public long upload(Image image) {
        return 0;
    }

    @Override
    @CheckResult
    public long upload(Post post) {
        return 0;
    }

    @Override
    @CheckResult
    public void delete(Post post) {
    }

    @Override
    @CheckResult
    public Observable<List<Post>> loadPosts(TimeInterval timeInterval) {
        return null;
    }

    @Override
    @CheckResult
    public List<Person> getPersons(@NonNull SingleNetwork element, @RequestType int requestType) {
        if (!isConnected()) {
            return Collections.emptyList();
        }
        // Here KeyKeeper is already non null because network is connected
        assert keyKeeper != null;

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
    }

    @Override
    @CheckResult
    public boolean connect(@NonNull KeyKeeper keyKeeper) {
        if (!(keyKeeper instanceof ly.loud.loudly.networks.VK.VKKeyKeeper))
            throw new AssertionError("KeyKeeper must be VkKeyKeeper");

        this.keyKeeper = ((VKKeyKeeper) keyKeeper);
        return true;
    }

    @Override
    @CheckResult
    public boolean disconnect() {
        deleteKeyKeeperFromDB();
        this.keyKeeper = null;
        return true;
    }

    @Override
    @CheckResult
    public boolean isConnected() {
        return keyKeeper != null;
    }

    @Override
    public int getId() {
        return Networks.VK;
    }

    private void deleteKeyKeeperFromDB() {
        // TODO: implement
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

