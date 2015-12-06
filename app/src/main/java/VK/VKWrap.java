package VK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import base.Networks;
import base.Person;
import base.Post;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.IDInterval;
import util.Network;
import util.Query;
import util.TimeInterval;
import util.Utils;


public class VKWrap extends Wrap {
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

    @Override
    public int networkID() {
        return NETWORK;
    }

    @Override
    protected Query makeAPICall(String method) {
        Query query = new Query(MAIN_SERVER + method);
        query.addParameter("v", API_VERSION);
        return query;
    }

    @Override
    protected Query makeSignedAPICall(String method) {
        Query query = makeAPICall(method);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        query.addParameter(ACCESS_TOKEN, keys.getAccessToken());
        return query;
    }

    @Override
    public void uploadPost(Post post) throws IOException {
        Query query = makeSignedAPICall(POST_METHOD);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        if (post.getAttachments().size() > 0) {
            Image image = (Image) post.getAttachments().get(0);
            String userID = ((VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID())).getUserId();
            query.addParameter("attachments", "photo" + userID + "_" + image.getLink(networkID()));
        }

        String response = Network.makePostRequest(query);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            String id = parser.getJSONObject("response").getString("post_id");
            post.setLink(networkID(), id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Post.Info getInfo(JSONObject object) throws JSONException {
        int like = object.getJSONObject("likes").getInt("count");
        int repost = object.getJSONObject("reposts").getInt("count");
        int comments = object.getJSONObject("comments").getInt("count");
        return new Post.Info(like, repost, comments);
    }

    @Override
    public void uploadImage(Image image, BackgroundAction progress) throws IOException {
        Query getUploadServerAddress = makeSignedAPICall(PHOTO_UPLOAD_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        getUploadServerAddress.addParameter("user_id", keys.getUserId());

        String response = Network.makeGetRequest(getUploadServerAddress);

        String uploadURL;
        JSONObject parser;
        try {
            parser = new JSONObject(response).getJSONObject("response");

            uploadURL = parser.getString("upload_url");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Query imageUploadQuery = new Query(uploadURL);

        response = Network.makePostRequest(imageUploadQuery, progress, "photo", image);

        Query getPhotoId = makeSignedAPICall(SAVE_PHOTO_METHOD);

        try {
            parser = new JSONObject(response);
            getPhotoId.addParameter("photo", parser.getString("photo"));
            getPhotoId.addParameter("server", parser.getString("server"));
            getPhotoId.addParameter("hash", parser.getString("hash"));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        getPhotoId.addParameter("user_id", ((VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID())).getUserId());

        response = Network.makeGetRequest(getPhotoId);

        try {
            parser = new JSONObject(response).getJSONArray("response").getJSONObject(0);
            String id = parser.getString("id");
            image.setLink(networkID(), id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getPostsInfo(List<Post> posts) throws IOException {
        Query query = makeSignedAPICall(GET_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        StringBuilder sb = new StringBuilder();
        for (Post post : posts) {
            if (post.getLink(networkID()) != null) {
                sb.append(keys.getUserId());
                sb.append('_');
                sb.append(post.getLink(networkID()));
                sb.append(',');
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        query.addParameter("posts", sb);

        String response = Network.makeGetRequest(query);

        try {
            JSONArray parser = new JSONObject(response)
                    .getJSONArray("response");
            int k = 0;
            for (Post post : posts) {
                if (post.getLink(networkID()) != null) {
                    JSONObject current = parser.getJSONObject(k++);
                    post.setInfo(NETWORK, getInfo(current));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadPosts(TimeInterval timeInterval, Tasks.LoadCallback callback) throws IOException {
        int offset = Loudly.getContext().getOffset(networkID());

        long earliestPost = -1;
        do {
            Query query = makeAPICall(LOAD_POSTS_METHOD);
            VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
            query.addParameter("owner_id", keys.getUserId());
            query.addParameter("filter", "owner");
            query.addParameter("count", "10");
            query.addParameter("offset", offset);

            String response = Network.makeGetRequest(query);

            JSONObject parser;
            try {
                parser = new JSONObject(response);
                JSONArray postJ = parser.getJSONObject("response").getJSONArray("items");

                if (postJ.length() == 1) {
                    break;
                }
                for (int i = 0; i < postJ.length(); i++) {
                    JSONObject post = postJ.getJSONObject(i);
                    String id = post.getString("id");

                    Post loudlyPost = callback.findLoudlyPost(id, networkID());
                    if (loudlyPost != null) {
                        loudlyPost.setInfo(networkID(), getInfo(post));
                        continue;
                    }

                    long date = post.getLong("date");

                    if (Loudly.getContext().getPostInterval(networkID()) == null) {
                        Loudly.getContext().setPostInterval(networkID(), new IDInterval(id, id));
                    }

                    Loudly.getContext().getPostInterval(networkID()).from = id;
                    earliestPost = date;

                    if (timeInterval.contains(date)) {
                        String text = post.getString("text");

                        Post res = new Post(text);
                        res.setLink(NETWORK, id);
                        res.setDate(date);

                        if (post.has("attachments")) {
                            JSONArray attachments = post.getJSONArray("attachments");
                            for (int j = 0; j < attachments.length(); j++) {
                                JSONObject obj = attachments.getJSONObject(j);
                                String type = obj.getString("type");
                                if (type.equals("photo") || type.equals("posted_photo")) {
                                    JSONObject photo = obj.getJSONObject("photo");
                                    String photoID = photo.getString("id");
                                    String link;
                                    if (photo.has("photo_604")) {
                                        link = photo.getString("photo_604");
                                    } else {
                                        link = photo.getString("photo_130");
                                    }
                                    Image image = new Image(link, false);
                                    image.setLink(networkID(), photoID);
                                    res.addAttachment(image);
                                }
                            }
                        }

                        res.setInfo(networkID(), getInfo(post));

                        callback.postLoaded(res);
                        offset++;
                    } else {
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } while (timeInterval.contains(earliestPost));
        Loudly.getContext().setOffset(networkID(), offset);
    }

    @Override
    public LinkedList<Person> getPersons(int what, Post post) throws IOException {
        Query query = makeAPICall("like.getList");
        query.addParameter("type", "post");
        VKKeyKeeper keys = ((VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID()));
        query.addParameter("owner_id", keys.getUserId());
        query.addParameter("item_id", post.getLink(networkID()));
        String filter;
        switch (what) {
            case Tasks.LIKES:
                filter = "likes";
                break;
            case Tasks.SHARES:
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

        Query getPeopleQuery = makeAPICall("users.get");

        JSONObject parser;
        try {
            parser = new JSONObject(response).getJSONObject("response");
            JSONArray likers = parser.getJSONArray("items");

            if (likers.length() == 0) {
                return new LinkedList<>();
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < likers.length(); i++) {
                long id = likers.getLong(i);
                sb.append(id);
                sb.append(',');
            }
            sb.delete(sb.length() - 1, sb.length());

            getPeopleQuery.addParameter("user_ids", sb);
            getPeopleQuery.addParameter("fields", "photo_50");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        response = Network.makeGetRequest(query);

        LinkedList<Person> result = new LinkedList<>();

        JSONArray people;
        try {
            people = new JSONObject(response).getJSONArray("response");
            for (int i = 0; i < people.length(); i++) {
                JSONObject person = people.getJSONObject(i);
                String firstName = person.getString("first_name");
                String lastName = person.getString("last_name");
                String photoURL = person.getString("photo_50");
                result.add(new Person(firstName, lastName, photoURL, networkID()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public Query makeDeleteQuery(Post post) {
        Query query = makeSignedAPICall(DELETE_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("owner_id", keys.getUserId());
        query.addParameter("post_id", post.getLink(NETWORK));
        return query;
    }

    public void parseDeleteResponse(Post post, String response) {
        if (response.equals("1")) {
            post.detachFromNetwork(NETWORK);
        }
    }
}
