package VK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import base.Networks;
import base.Post;
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
    private static final String POST_SERVER = "https://api.vk.com/method/wall.post";
    private static final String GET_SERVER = "https://api.vk.com/method/wall.getById";
    private static final String DELETE_SERVER = "https://api.vk.com/method/wall.delete";
    private static final String LOAD_POSTS_SERVER = "https://api.vk.com/method/wall.get";
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int networkID() {
        return NETWORK;
    }

    @Override
    public Query makePostQuery(Post post) {
        Query query = new Query(POST_SERVER);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter(ACCESS_TOKEN, keys.getAccessToken());
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
        }
    }

    @Override
    public Query makeGetQueries(Post post) {
        Query query = new Query(GET_SERVER);
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
        Query query = new Query(DELETE_SERVER);
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
        Query query = new Query(LOAD_POSTS_SERVER);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        query.addParameter("owner_id", keys.getUserId());
        query.addParameter("filter", "owner");
        query.addParameter("count", "10");
        query.addParameter("offset", "" + Loudly.getContext().getOffset(networkID()));
        return query;
    }

    @Override
    public boolean parsePostsLoadedResponse(LinkedList<Post> posts, TimeInterval loadedTime, String response) {
        JSONObject parser;
        try {
            parser = new JSONObject(response);
            JSONArray postJ = parser.getJSONArray("response");

            int offset = Loudly.getContext().getOffset(networkID());
            if (postJ.length() == 1) {
                return false;
            }
            TimeInterval oldTime = loadedTime.copy();
            for (int i = 1; i < postJ.length(); i++) {
                JSONObject post = postJ.getJSONObject(i);
                String id = post.getString("id");
                String text = post.getString("text");
                long date = post.getLong("date");

                if (Loudly.getContext().getPostInterval(networkID()) == null) {
                    Loudly.getContext().setPostInterval(networkID(), new IDInterval(id, id));
                }

                loadedTime.to = date;
                Loudly.getContext().getPostInterval(networkID()).from = id;

                if (oldTime.contains(date)) {
                    Post res = new Post(text);
                    res.setLink(NETWORK, id);
                    res.setDate(date);

                    int likes = post.getJSONObject("likes").getInt("count");
                    int shares = post.getJSONObject("reposts").getInt("count");
                    int comments = post.getJSONObject("comments").getInt("count");
                    res.setInfo(networkID(), new Post.Info(likes, shares, comments));

                    posts.add(res);
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
