package VK;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import base.KeyKeeper;
import base.Networks;
import base.Post;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.IDInterval;
import util.Parameter;
import util.Query;
import util.TimeInterval;


public class VKWrap extends Wrap {
    private static final int NETWORK = Networks.VK;
    private static final String TAG = "VK_WRAP_TAG";
    private static final String API_VERSION = "5.40";
    private static final String POST_SERVER = "https://api.vk.com/method/wall.post";
    private static final String GET_SERVER = "https://api.vk.com/method/wall.getById";
    private static final String DELETE_SERVER = "https://api.vk.com/method/wall.delete";
    private static final String LOAD_POSTS_SERVER = "https://api.vk.com/method/wall.get";
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int networkID() {
        return NETWORK;
    }


    private Query makeAPIQuery(String URL) {
        Query query = new Query(URL);
        query.addParameter("v", API_VERSION);
        return query;
    }

    private Query makeSignedQuery(String URL) {
        Query query = makeAPIQuery(URL);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        query.addParameter(ACCESS_TOKEN, keys.getAccessToken());
        return query;
    }

    @Override
    public Query makePostQuery(Post post) {
        Query query = makeSignedQuery(POST_SERVER);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        return query;
    }

    @Override
    public Parameter uploadImage(Image image, BackgroundAction publish) {
        return null;
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
        Query query = makeAPIQuery(GET_SERVER);
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
        Query query = makeAPIQuery(DELETE_SERVER);
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
        Query query = makeAPIQuery(LOAD_POSTS_SERVER);
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
}
