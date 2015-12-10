package Facebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import base.Networks;
import base.Person;
import base.says.Comment;
import base.says.Info;
import base.says.LoudlyPost;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import base.says.Post;
import base.says.SinglePost;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.IDInterval;
import util.Network;
import util.Query;
import util.TimeInterval;

public class FacebookWrap extends Wrap {
    private static final int NETWORK = Networks.FB;
    private static final String MAIN_SERVER = "https://graph.facebook.com/v2.5/";
    private static final String POST_NODE = "me/feed";
    private static final String PHOTO_NODE = "me/photos";
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int networkID() {
        return NETWORK;
    }

    @Override
    protected Query makeAPICall(String node) {
        return new Query(MAIN_SERVER + node);
    }

    @Override
    protected Query makeSignedAPICall(String node) {
        Query query = makeAPICall(node);
        FacebookKeyKeeper keys = (FacebookKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("access_token", keys.getAccessToken());
        return query;
    }

    @Override
    public void uploadPost(LoudlyPost post) throws IOException {
        Query query = makeSignedAPICall(POST_NODE);
        query.addParameter("message", post.getText());
        if (post.getAttachments().size() > 0) {
            query.addParameter("object_attachment", post.getAttachments().get(0).getLink(networkID()));
        }

        String response = Network.makePostRequest(query);

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
    public void uploadImage(Image image, BackgroundAction progress) throws IOException {
        Query query = makeSignedAPICall(PHOTO_NODE);
        query.addParameter("published", true);
        query.addParameter("no_story", true);

        String response = Network.makePostRequest(query, progress, "source",
                image);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            image.setLink(networkID(), parser.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePost(Post post) throws IOException {
        Query query = makeSignedAPICall(post.getLink(networkID()));

        String response = Network.makeDeleteRequest(query);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            if (parser.getString("success").equals("true")) {
                post.detachFromNetwork(NETWORK);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadPosts(TimeInterval timeInterval, Tasks.LoadCallback callback) throws IOException {
        Query query = makeSignedAPICall(POST_NODE);
        if (timeInterval.from != -1) {
            query.addParameter("since", (timeInterval.from + 1) + "");
        }
        if (timeInterval.to != -1) {
            query.addParameter("until", (timeInterval.to - 1) + "");
        }
        query.addParameter("date_format", "U");
        query.addParameter("fields",
                "message,created_time,id,likes.limit(0).summary(true),shares,comments.limit(0).summary(true),attachments");

        String response = Network.makeGetRequest(query);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            JSONArray postsJ = parser.getJSONArray("data");
            if (postsJ.length() == 0) {
                return;
            }

            for (int i = 0; i < postsJ.length(); i++) {
                JSONObject obj = postsJ.getJSONObject(i);
                String id = obj.getString("id");
                LoudlyPost loudlyPost = callback.findLoudlyPost(id, networkID());

                if (loudlyPost != null) {
                    loudlyPost.setInfo(networkID(), getInfo(obj));
                    continue;
                }

                long postTime = obj.getLong("created_time");

                if (Loudly.getContext().getPostInterval(networkID()) == null) {
                    Loudly.getContext().setPostInterval(networkID(), new IDInterval(id, id));
                }

                Loudly.getContext().getPostInterval(networkID()).from = id;

                String text = (obj.has("message")) ? obj.getString("message") : "";

                SinglePost post = new SinglePost(text, postTime, null, networkID(), id);

                if (obj.has("attachments")) {
                    JSONObject attachment = obj.getJSONObject("attachments").
                            getJSONArray("data").
                            getJSONObject(0).getJSONObject("media").getJSONObject("image");
                    String link = attachment.getString("src");
                    int height = attachment.getInt("height");
                    int width = attachment.getInt("width");
                    Image image = new Image(link, false);
                    image.setHeight(height);
                    image.setWidth(width);
                    post.addAttachment(image);

                }

                post.setInfo(getInfo(obj));
                callback.postLoaded(post);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Info getInfo(JSONObject object) throws JSONException {
        int likes = 0;
        if (object.has("likes")) {
            likes = object.getJSONObject("likes").getJSONObject("summary").getInt("total_count");
        }
        int comments = 0;
        if (object.has("comments")) {
            comments = object.getJSONObject("comments").getJSONObject("summary").getInt("total_count");
        }

        int shares = 0;
        if (object.has("shares")) {
            comments = object.getJSONObject("shares").getInt("count");
        }

        return new Info(likes, shares, comments);
    }

    @Override
    public void getPostsInfo(List<Post> posts) throws IOException {
        Query query = makeSignedAPICall("");
        StringBuilder sb = new StringBuilder();
        for (Post post : posts) {
            if (post.existsIn(networkID())) {
                sb.append(post.getLink(networkID()));
                sb.append(',');
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        query.addParameter("ids", sb);

        query.addParameter("fields", "likes.limit(0).summary(true),comments.limit(0).summary(true),shares");

        String response = Network.makeGetRequest(query);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            for (Post post : posts) {
                if (post.existsIn(networkID())) {
                    JSONObject p = parser.getJSONObject(post.getLink(networkID()));
                    Info info = getInfo(p);
                    if (post instanceof SinglePost) {
                        post.setInfo(info);
                    } else {
                        ((LoudlyPost) post).setInfo(NETWORK, info);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Comment> getComments(Post post) throws IOException {
        return null;
    }

    @Override
    public LinkedList<Person> getPersons(int what, Post post) throws IOException {
        String node;
        switch (what) {
            case Tasks.LIKES:
                node = "/likes";
                break;
            case Tasks.SHARES:
                node = "/sharedposts";
                break;
            default:
                node = "";
                break;
        }
        Query query = makeSignedAPICall(post.getLink(networkID()) + node);
        String response = Network.makeGetRequest(query);
        JSONObject parser;

        Query getPeopleQuery = makeSignedAPICall("");
        StringBuilder sb = new StringBuilder();
        ArrayList<String> ids = new ArrayList<>();

        try {
            parser = new JSONObject(response);
            JSONArray likers = parser.getJSONArray("data");

            if (likers.length() == 0) {
                return new LinkedList<>();
            }

            for (int i = 0; i < likers.length(); i++) {
                String id = likers.getJSONObject(i).getString("id");

                if (id.indexOf('_') != -1) id = id.substring(0, id.indexOf("_"));
                ids.add(id);
                sb.append(id);
                sb.append(',');
            }
            sb.delete(sb.length() - 1, sb.length());

            getPeopleQuery.addParameter("ids", sb);
            getPeopleQuery.addParameter("fields", "first_name,last_name,picture");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        response = Network.makeGetRequest(getPeopleQuery);

        LinkedList<Person> result = new LinkedList<>();
        try {
            parser = new JSONObject(response);
            for (String id :ids) {
                JSONObject person = parser.getJSONObject(id);
                String firstName = person.getString("first_name");
                String lastName = person.getString("last_name");
                String pictureUrl = person.getJSONObject("picture").getJSONObject("data").getString("url");
                result.add(new Person(firstName, lastName, pictureUrl, networkID()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
