package VK;

import org.json.JSONException;
import org.json.JSONObject;

import base.Networks;
import base.Post;
import base.PostInfo;
import base.Wrappable;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.Parameter;
import util.Query;


public class VKWrap implements Wrappable {
    private static final int NETWORK = Networks.VK;
    private static final String TAG = "VK_WRAP_TAG";
    private static final String POST_SERVER = "https://api.vk.com/method/wall.post";
    private static final String GET_SERVER = "https://api.vk.com/method/wall.getById";

    @Override
    public Query makePostQuery(Post post) {
        Query query = new Query(POST_SERVER);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
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
            String id = parser.getJSONObject("response").getString("post_id");
            post.setInfo(Networks.VK, new PostInfo(id));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Query[] makeGetQuery(Post post) {
        Query query = new Query(GET_SERVER);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("posts", keys.getUserId() + "_" + post.getInfo(NETWORK).link);
        return new Query[] {query};
    }

    @Override
    public void parseGetResponse(Post post, String[] response) {
        JSONObject parser;
        try {
            parser = new JSONObject(response[0])
                    .getJSONArray("response")
                    .getJSONObject(0);
            int like = parser.getJSONObject("likes").getInt("count");
            int repost = parser.getJSONObject("reposts").getInt("count");
            int comments = parser.getJSONObject("comments").getInt("count");
            PostInfo info = post.getInfo(NETWORK);
            info.addInfo(like, repost, comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
