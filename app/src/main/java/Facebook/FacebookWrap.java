package Facebook;

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
import util.Parameter;
import util.Query;

public class FacebookWrap extends Wrap {
    private static final int NETWORK = Networks.FB;
    private static final String MAIN_SERVER = "https://graph.facebook.com/v2.5/";
    private static final String POST_SERVER = "https://graph.facebook.com/me/feed";
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int networkID() {
        return NETWORK;
    }

    @Override
    public Query makePostQuery(Post post) {
        Query query = new Query(POST_SERVER);
        query.addParameter("message", post.getText());
        FacebookKeyKeeper keys = (FacebookKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("access_token", keys.getAccessToken());
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
            String id = parser.getString("id");
            post.setLink(NETWORK, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Query makeGetQueries(Post post) {
        Query query = new Query(MAIN_SERVER + post.getLink(NETWORK));
        query.addParameter("fields", "likes.limit(0).summary(true),comments.limit(0).summary(true),shares");
        FacebookKeyKeeper keyKeeper = (FacebookKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter(ACCESS_TOKEN, keyKeeper.getAccessToken());
        return query;
    }

    @Override
    public void parseGetResponse(Post post, String response) {
        JSONObject parser;
        try {
            parser = new JSONObject(response);
            int likes = 0;
            if (parser.has("likes")) {
                likes = parser.getJSONObject("likes").getJSONObject("summary").getInt("total_count");
            }
            int comments = 0;
            if (parser.has("comments")) {
                comments = parser.getJSONObject("comments").getJSONObject("summary").getInt("total_count");
            }

            int shares = 0;
            if (parser.has("shares")) {
                comments = parser.getJSONObject("shares").getInt("count");
            }

            post.setInfo(NETWORK, new Post.Info(likes, shares, comments));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Query makeDeleteQuery(Post post) {
        return new Query(MAIN_SERVER + post.getLink(NETWORK));
    }

    @Override
    public void parseDeleteResponse(Post post, String response) {
        try {
            JSONObject parse = new JSONObject(response);
            if (parse.getString("success").equals("true")) {
                post.detachFromNetwork(NETWORK);
            }
        } catch (JSONException e) {
            // ToDo: tell about fails
        }
    }

    @Override
    public Query makeLoadPostsQuery(long sinceID, long beforeID, long sinceTime, long beforeTime) {
        Query query = new Query(POST_SERVER);
        if (sinceTime != -1) {
            query.addParameter("since", (sinceTime / 1000) + "");
        }
        if (beforeTime != -1) {
            query.addParameter("until", (beforeTime / 1000) + "");
        }
        FacebookKeyKeeper keys = (FacebookKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter(ACCESS_TOKEN, keys.getAccessToken());
        query.addParameter("date_format", "U");
        return query;
    }

    @Override
    public long parsePostsLoadedResponse(LinkedList<Post> posts, long sinceTime, long beforeTime, String response) {
        JSONObject parser;
        try {
            long lastTime = -1;
            parser = new JSONObject(response);
            JSONArray postsJ = parser.getJSONArray("data");
            for (int i = 0; i < postsJ.length(); i++) {
                JSONObject obj = postsJ.getJSONObject(i);
                String text = obj.getString("message");
                String id = obj.getString("id");
                lastTime = obj.getLong("created_time") * 1000;
                Post post = new Post(text);
                post.setLink(NETWORK, id);
                post.setDate(lastTime);
                posts.add(post);
            }
            return lastTime;
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
