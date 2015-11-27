package VK;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import base.Networks;
import base.Post;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.IDInterval;
import util.Network;
import util.Query;
import util.TimeInterval;


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


    private Query makeAPIQuery(String method) {
        Query query = new Query(MAIN_SERVER + method);
        query.addParameter("v", API_VERSION);
        return query;
    }

    private Query makeSignedQuery(String method) {
        Query query = makeAPIQuery(method);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        query.addParameter(ACCESS_TOKEN, keys.getAccessToken());
        return query;
    }

    @Override
    public Query makePostQuery(Post post) {
        Query query = makeSignedQuery(POST_METHOD);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        if (post.getAttachments().size()>0) {
            Image image = (Image)post.getAttachments().get(0);
            String userID = ((VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID())).getUserId();
            query.addParameter("attachments", "photo"+userID + "_"+image.getLink(networkID()));
        }
        return query;
    }

    @Override
    public void parsePostResponse(Post post, String response) {
        JSONObject parser;
        try {
            parser = new JSONObject(response);
            String id = parser.getJSONObject("response").getString("post_id");
            post.setLink(NETWORK, id);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("VK", response);
        }
    }

    @Override
    public Query makeGetQueries(Post post) {
        Query query = makeAPIQuery(GET_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("posts", keys.getUserId() + "_" + post.getLink(NETWORK));
        return query;
    }

    @Override
    public void parseGetResponse(Post post, String response) {
        JSONObject parser;
        try {
            parser = new JSONObject(response)
                    .getJSONArray("response")
                    .getJSONObject(0);
            int like = parser.getJSONObject("likes").getInt("count");
            int repost = parser.getJSONObject("reposts").getInt("count");
            int comments = parser.getJSONObject("comments").getInt("count");

            post.setInfo(NETWORK, new Post.Info(like, repost, comments));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Query makeDeleteQuery(Post post) {
        Query query = makeAPIQuery(DELETE_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter(ACCESS_TOKEN, keys.getAccessToken());
        query.addParameter("owner_id", keys.getUserId());
        query.addParameter("post_id", post.getLink(NETWORK));
        return query;
    }

    @Override
    public void parseDeleteResponse(Post post, String response) {
        if (response.equals("1")) {
            post.detachFromNetwork(NETWORK);
        }
    }

    @Override
    public Query makeLoadPostsQuery(TimeInterval time) {
        Query query = makeAPIQuery(LOAD_POSTS_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        query.addParameter("owner_id", keys.getUserId());
        query.addParameter("filter", "owner");
        query.addParameter("count", "10");
        query.addParameter("offset", "" + Loudly.getContext().getOffset(networkID()));
        return query;
    }

    @Override
    public boolean parsePostsLoadedResponse(TimeInterval loadedTime, String response,
                                            Tasks.LoadCallback callback) {
        JSONObject parser;
        try {
            parser = new JSONObject(response);
            JSONArray postJ = parser.getJSONObject("response").getJSONArray("items");

            int offset = Loudly.getContext().getOffset(networkID());
            if (postJ.length() == 1) {
                return false;
            }
            TimeInterval oldTime = loadedTime.copy();
            for (int i = 0; i < postJ.length(); i++) {
                JSONObject post = postJ.getJSONObject(i);
                String id = post.getString("id");

                Post loudlyPost = callback.findLoudlyPost(id, networkID());
                if (loudlyPost != null) {
                    int likes = post.getJSONObject("likes").getInt("count");
                    int shares = post.getJSONObject("reposts").getInt("count");
                    int comments = post.getJSONObject("comments").getInt("count");
                    loudlyPost.setInfo(networkID(), new Post.Info(likes, shares, comments));
                    continue;
                }

                long date = post.getLong("date");

                if (Loudly.getContext().getPostInterval(networkID()) == null) {
                    Loudly.getContext().setPostInterval(networkID(), new IDInterval(id, id));
                }

                loadedTime.to = date;
                Loudly.getContext().getPostInterval(networkID()).from = id;

                if (oldTime.contains(date)) {
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

                    int likes = post.getJSONObject("likes").getInt("count");
                    int shares = post.getJSONObject("reposts").getInt("count");
                    int comments = post.getJSONObject("comments").getInt("count");
                    res.setInfo(networkID(), new Post.Info(likes, shares, comments));

                    callback.postLoaded(res);
                    offset++;
                } else {
                    Loudly.getContext().setOffset(networkID(), offset);
                    return false;
                }
            }
            Loudly.getContext().setOffset(networkID(), offset);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public Query makeUploadImageQuery() {
        Query getAddress = makeSignedQuery(PHOTO_UPLOAD_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        getAddress.addParameter("user_id", keys.getUserId());
        try {
            String response = Network.makeGetRequest(getAddress);

            JSONObject parser = new JSONObject(response).getJSONObject("response");

            Query query = new Query(parser.getString("upload_url"));
            return query;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String uploadImageTag() {
        return "photo";
    }

    @Override
    public void parseUploadImageResponse(Image image, String response) {
        Query getPhotoId = makeSignedQuery(SAVE_PHOTO_METHOD);
        try {
            JSONObject parser = new JSONObject(response);
            getPhotoId.addParameter("photo", parser.getString("photo"));
            getPhotoId.addParameter("server", parser.getString("server"));
            getPhotoId.addParameter("hash", parser.getString("hash"));
            getPhotoId.addParameter("user_id", ((VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID())).getUserId());
            response = Network.makeGetRequest(getPhotoId);
            parser = new JSONObject(response).getJSONArray("response").getJSONObject(0);
            String id = parser.getString("id");
            image.setLink(networkID(), id);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
