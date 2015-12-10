package VK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import base.Networks;
import base.Person;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import base.says.Comment;
import base.says.Info;
import base.says.LoudlyPost;
import base.says.Post;
import base.says.SinglePost;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.IDInterval;
import util.Network;
import util.Query;
import util.TimeInterval;
import util.parsers.json.ArrayParser;
import util.parsers.json.ObjectParser;


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
    public int shouldUploadImage() {
        return Wrap.IMAGE_ONLY_UPLOAD;
    }

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
    public void uploadPost(LoudlyPost post) throws IOException {
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
            String url = parser.getString("photo_604");
            if (image.isLocal()) {
                image.setExternalLink(url);
            }
            int height = parser.getInt("height");
            int width = parser.getInt("width");
            image.setLink(networkID(), id);
            image.setHeight(height);
            image.setWidth(width);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePost(Post post) throws IOException {
        Query query = makeSignedAPICall(DELETE_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("owner_id", keys.getUserId());
        query.addParameter("post_id", post.getLink(NETWORK));

        String response = Network.makeGetRequest(query);

        // todo: check for delete
        post.detachFromNetwork(NETWORK);
    }

    @Override
    public void getPostsInfo(List<Post> posts) throws IOException {
        Query query = makeSignedAPICall(GET_METHOD);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        StringBuilder sb = new StringBuilder();
        for (Post post : posts) {
            if (post.existsIn(networkID())) {
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
                if (post.existsIn(networkID())) {
                    JSONObject current = parser.getJSONObject(k++);
                    Info info = getInfo(current);
                    if (post instanceof SinglePost) {
                        post.setInfo(info);
                    } else {
                        ((LoudlyPost) post).setInfo(NETWORK, getInfo(current));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Info getInfo(JSONObject object) throws JSONException {
        int like = object.getJSONObject("likes").getInt("count");
        int repost = object.getJSONObject("reposts").getInt("count");
        int comments = object.getJSONObject("comments").getInt("count");
        return new Info(like, repost, comments);
    }

    private Image getImageFromParser(ObjectParser photoParser) {
        String photoId = photoParser.getString();
        String link = photoParser.getString();
        int width = photoParser.getInt();
        int height = photoParser.getInt();

        Image image = new Image(link, false);
        image.setLink(networkID(), photoId);
        image.setWidth(width);
        image.setHeight(height);
        return image;
    }

    @Override
    public void loadPosts(TimeInterval timeInterval, Tasks.LoadCallback callback) throws IOException {
        int offset = Loudly.getContext().getOffset(networkID());

        ObjectParser photoParser = new ObjectParser()
                .parseString("id")
                .parseString("photo_604")
                .parseInt("width")
                .parseInt("height");

        ObjectParser attachmentParser = new ObjectParser()
                .parseString("type")
                .parseObject("photo", photoParser);

        ObjectParser postParser = new ObjectParser()
                .parseString("id")
                .parseLong("date")
                .parseString("text")
                .parseObject("likes", new ObjectParser().parseInt("count"))
                .parseObject("reposts", new ObjectParser().parseInt("count"))
                .parseObject("comments", new ObjectParser().parseInt("count"))
                .parseArray("attachments", new ArrayParser(-1, attachmentParser));

        ArrayParser itemsParser = new ArrayParser(-1, postParser);
        ObjectParser responseParser = new ObjectParser()
                .parseArray("items", itemsParser);

        ObjectParser parser = new ObjectParser()
                .parseObject("response", responseParser);
        long earliestPost = -1;
        do {
            Query query = makeAPICall(LOAD_POSTS_METHOD);
            VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
            query.addParameter("owner_id", keys.getUserId());
            query.addParameter("filter", "owner");
            query.addParameter("count", "10");
            query.addParameter("offset", offset);

            ObjectParser response = Network.makeGetRequestAndParse(query, parser);

            ArrayParser arrayParser = response.getObject().getArray();
            for (int i = 0; i < arrayParser.size(); i++) {
                postParser = arrayParser.getObject(i);
                String id = postParser.getString();
                long date = postParser.getLong();
                String text = postParser.getString();

                int likes = postParser.getObject().getInt();
                int shares = postParser.getObject().getInt();
                int comments = postParser.getObject().getInt();

                ArrayParser attachments = postParser.getArray();

                Info info = new Info(likes, shares, comments);

                LoudlyPost loudlyPost = callback.findLoudlyPost(id, networkID());
                if (loudlyPost != null) {
                    loudlyPost.setInfo(networkID(), info);
                    continue;
                }
                // TODO: 12/8/2015 move interval to wrap
                if (Loudly.getContext().getPostInterval(networkID()) == null) {
                    Loudly.getContext().setPostInterval(networkID(), new IDInterval(id, id));
                }

                if (timeInterval.contains(date)) {
                    SinglePost res = new SinglePost(text, date, null, networkID(), id);
                    res.setInfo(info);

                    for (int j = 0; j < attachments.size(); j++) {
                        attachmentParser = attachments.getObject(j);
                        String type = attachmentParser.getString();
                        if (type.equals("photo") || type.equals("posted_photo")) {
                            photoParser = attachmentParser.getObject();
                            res.addAttachment(getImageFromParser(photoParser));
                        }
                    }
                    callback.postLoaded(res);
                    offset++;
                }

            }
        } while (timeInterval.contains(earliestPost));
        Loudly.getContext().setOffset(networkID(), offset);
    }

    @Override
    public List<Comment> getComments(Post post) throws IOException {
        Query query = makeSignedAPICall("wall.getComments");
        VKKeyKeeper keyKeeper = ((VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID()));

        query.addParameter("owner_id", keyKeeper.getUserId());
        query.addParameter("post_id", post.getLink(networkID()));
        query.addParameter("need_likes", 1);
        query.addParameter("count", 20);
        query.addParameter("sort", "desc");
        query.addParameter("preview_length", 0);
        query.addParameter("extended", 1);

        ObjectParser photoParser = new ObjectParser()
                .parseString("id")
                .parseString("photo_604")
                .parseInt("width")
                .parseInt("height");

        ObjectParser attachmentParser = new ObjectParser()
                .parseString("type")
                .parseObject("photo", photoParser);

        ObjectParser commentParser = new ObjectParser()
                .parseString("from_id")
                .parseLong("date")
                .parseString("text")
                .parseObject("likes", new ObjectParser().parseInt("count"))
                .parseObject("reposts", new ObjectParser().parseInt("count"))
                .parseObject("comments", new ObjectParser().parseInt("count"))
                .parseArray("attachments", new ArrayParser(-1, attachmentParser));

        ObjectParser personParser = new ObjectParser()
                .parseString("id")
                .parseString("first_name")
                .parseString("last_name")
                .parseString("photo_50");

        ObjectParser parser = new ObjectParser()
                .parseArray("items", new ArrayParser(-1, commentParser))
                .parseArray("profiles", new ArrayParser(-1, personParser));

        ObjectParser response = Network.makeGetRequestAndParse(query,
                new ObjectParser().parseObject("response", parser))
                .getObject();

        ArrayParser posts = response.getArray();
        ArrayParser persons = response.getArray();


        LinkedList<Person> profiles = new LinkedList<>();
        for (int i = 0; i < persons.size(); i++) {
            ObjectParser person = persons.getObject(i);
            String id = person.getString();
            String firstName = person.getString();
            String lastName = person.getString();
            String photo = person.getString();
            Person p = new Person(firstName, lastName, photo, networkID());
            p.setId(id);
            profiles.add(p);
        }

        LinkedList<Comment> comments = new LinkedList<>();

        for (int i = 0; i < posts.size(); i++) {
            commentParser = posts.getObject(i);
            String userID = commentParser.getString();
            Person author = null;
            for (Person p : profiles) {
                if (p.getId().equals(userID)) {
                    author = p;
                    break;
                }
            }

            long date = commentParser.getLong();
            String text = commentParser.getString();

            int likes = commentParser.getObject().getInt();
            ArrayParser attachments = commentParser.getArray();

            Comment comment = new Comment(text, date, author, networkID());
            for (int j = 0; j < attachments.size(); j++) {
                attachmentParser = attachments.getObject(i);
                String type = attachmentParser.getString();
                if (type.equals("photo")) {
                    comment.addAttachment(getImageFromParser(attachmentParser.getObject()));
                }
            }
            comments.add(comment);
        }
        return comments;
    }

    @Override
    public LinkedList<Person> getPersons(int what, Post post) throws IOException {
        Query query = makeSignedAPICall("likes.getList");
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

        Query getPeopleQuery = makeSignedAPICall("users.get");

        JSONObject parser;
        try {
            parser = new JSONObject(response).getJSONObject("response");
            JSONArray likers = parser.getJSONArray("items");

            if (likers.length() == 0) {
                return new LinkedList<>();
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
            return null;
        }

        response = Network.makeGetRequest(query);

        LinkedList<Person> result = new LinkedList<>();

        JSONArray people;
        try {
            people = new JSONObject(response).getJSONObject("response").getJSONArray("items");
            for (int i = 0; i < people.length(); i++) {
                JSONObject person = people.getJSONObject(i);
                String firstName = person.getString("first_name");
                String lastName = person.getString("last_name");
                String photoURL = "";//person.getString("photo_50");
                result.add(new Person(firstName, lastName, photoURL, networkID()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}
