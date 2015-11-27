package Facebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class FacebookWrap extends Wrap {
    private static final int NETWORK = Networks.FB;
    private static final String MAIN_SERVER = "https://graph.facebook.com/v2.5/";
    private static final String POST_SERVER = "https://graph.facebook.com/me/feed";
    private static final String IMAGE_SERVER = "https://graph.facebook.com/me/photos";
    private static final String ACCESS_TOKEN = "access_token";

    protected Query makeSignedRequest(String server) {
        Query query = new Query(server);
        FacebookKeyKeeper keys = (FacebookKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("access_token", keys.getAccessToken());
        return query;
    }

    @Override
    public int networkID() {
        return NETWORK;
    }

    @Override
    public Query makePostQuery(Post post) {
        Query query = makeSignedRequest(POST_SERVER);
        query.addParameter("message", post.getText());
        if (post.getAttachments().size() > 0) {
            query.addParameter("object_attachment", post.getAttachments().get(0).getLink(networkID()));
        }
        return query;
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
    public Query makeLoadPostsQuery(TimeInterval time) {
        Query query = new Query(POST_SERVER);
        if (time.from != -1) {
            query.addParameter("since", (time.from + 1) + "");
        }
        if (time.to != -1) {
            query.addParameter("until", (time.to - 1) + "");
        }
        FacebookKeyKeeper keys = (FacebookKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter(ACCESS_TOKEN, keys.getAccessToken());
        query.addParameter("date_format", "U");
        query.addParameter("fields",
                "message,created_time,id,likes.limit(0).summary(true),shares,comments.limit(0).summary(true),attachments");
        return query;
    }

    @Override
    public boolean parsePostsLoadedResponse(TimeInterval time, String response,
                                            Tasks.LoadCallback callback) {
        JSONObject parser;
        try {
            parser = new JSONObject(response);
            JSONArray postsJ = parser.getJSONArray("data");
            if (postsJ.length() == 0) {
                return false;
            }

            TimeInterval oldTime = time.copy();

            for (int i = 0; i < postsJ.length(); i++) {
                JSONObject obj = postsJ.getJSONObject(i);
                String id = obj.getString("id");
                Post loudlyPost = callback.findLoudlyPost(id, networkID());
                if (loudlyPost != null) {
                    int likes = 0;
                    if (obj.has("likes")) {
                        likes = obj.getJSONObject("likes").getJSONObject("summary").getInt("total_count");
                    }
                    int shares = 0;
                    if (obj.has("shares")) {
                        shares = obj.getJSONObject("shares").getInt("count");
                    }
                    int comments = 0;
                    if (obj.has("comments")) {
                        comments = obj.getJSONObject("comments").getJSONObject("summary").getInt("total_count");
                    }
                    loudlyPost.setInfo(networkID(), new Post.Info(likes, shares, comments));
                    continue;
                }

                long postTime = obj.getLong("created_time");

                if (Loudly.getContext().getPostInterval(networkID()) == null) {
                    Loudly.getContext().setPostInterval(networkID(), new IDInterval(id, id));
                }

                if (i == 0) {
                    time.from = postTime;
                }

                time.to = postTime;
                Loudly.getContext().getPostInterval(networkID()).from = id;

                if (oldTime.contains(postTime)) {
                    String text = obj.getString("message");
                    Post post = new Post(text);
                    post.setLink(NETWORK, id);
                    post.setDate(postTime);

                    if (obj.has("attachments")) {
                        JSONObject attachment = obj.getJSONObject("attachments").
                                getJSONArray("data").
                                getJSONObject(0).getJSONObject("media").getJSONObject("image");
                        String link = attachment.getString("src");
                        Image image = new Image(link, false);
                        post.addAttachment(image);

                    }
                    int likes = 0;
                    if (obj.has("likes")) {
                        likes = obj.getJSONObject("likes").getJSONObject("summary").getInt("total_count");
                    }
                    int shares = 0;
                    if (obj.has("shares")) {
                        shares = obj.getJSONObject("shares").getInt("count");
                    }
                    int comments = 0;
                    if (obj.has("comments")) {
                        comments = obj.getJSONObject("comments").getJSONObject("summary").getInt("total_count");
                    }
                    post.setInfo(networkID(), new Post.Info(likes, shares, comments));
                    callback.postLoaded(post);
                } else {
                    return false;
                }
            }

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Query makeUploadImageQuery() {
        Query query = makeSignedRequest(IMAGE_SERVER);
        query.addParameter("published", "true");
        query.addParameter("no_story", "true");
        return query;
    }

    @Override
    public String uploadImageTag() {
        return "source";
    }

    @Override
    public void parseUploadImageResponse(Image image, String response) {
        JSONObject parser;
        try {
            parser = new JSONObject(response);
            image.setLink(networkID(), parser.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
