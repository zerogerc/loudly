package VK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import base.Networks;
import base.Post;
import base.Wrap;
import base.attachments.Attachment;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.Parameter;
import util.Query;


public class VKWrap extends Wrap {
    private static final int NETWORK = Networks.VK;
    private static final String TAG = "VK_WRAP_TAG";
    private static final String POST_SERVER = "https://api.vk.com/method/wall.post";
    private static final String GET_SERVER = "https://api.vk.com/method/wall.getById";
    private static final String DELETE_SERVER = "https://api.vk.com/method/wall.delete";
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
    public Query makeLoadPostsQuery(long sinceID, long beforeID, long sinceTime, long beforeTime) {
        return null;
    }

    @Override
    public long parsePostsLoadedResponse(LinkedList<Post> posts, long sinceTime, long beforeTime, String response) {
        return 0;
    }
}
