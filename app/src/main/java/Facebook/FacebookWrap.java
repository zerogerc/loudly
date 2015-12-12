package Facebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import base.Networks;
import base.Person;
import base.Tasks;
import base.Wrap;
import base.attachments.Attachment;
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

public class FacebookWrap extends Wrap {
    private static final int NETWORK = Networks.FB;
    private static final String MAIN_SERVER = "https://graph.facebook.com/v2.5/";
    private static final String POST_NODE = "me/feed";
    private static final String PHOTO_NODE = "me/photos";

    @Override
    public int shouldUploadImage() {
        return Wrap.IMAGE_UPLOAD_OR_LINK;
    }

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
            for (Attachment attachment : post.getAttachments()) {
                Image image = ((Image) attachment);
                query.addParameter("object_attachment", image.getLink(networkID()));
            }
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

        String response;
        if (image.isLocal()) {
            response = Network.makePostRequest(query, progress, "source",
                    image);
        } else {
            query.addParameter("url", image.getExternalLink());
            response = Network.makePostRequest(query);
        }

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            String id = parser.getString("id");

            image.setLink(networkID(), id);
            if (image.isLocal()) {
                ArrayList<Image> temp = new ArrayList<>();
                temp.add(image);
                getImageInfo(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ObjectParser makePhotoParser() {
        return new ObjectParser()
                .parseString("src")
                .parseInt("width")
                .parseInt("height");
    }

    private void fillImageFromParser(Image image, ObjectParser photoParser) {
        String link = photoParser.getString("");
        int width = photoParser.getInt(0);
        int height = photoParser.getInt(0);
        image.setExternalLink(link);
        image.setWidth(width);
        image.setHeight(height);
    }

    @Override
    public void getImageInfo(List<Image> images) throws IOException {
        Query query = makeSignedAPICall("");

        ObjectParser photoParser = new ObjectParser()
                .parseString("link")
                .parseInt("width")
                .parseInt("height");

        ObjectParser responseParser = new ObjectParser();

        StringBuilder sb = new StringBuilder();
        for (Image image : images) {
            if (image.existsIn(networkID())) {
                sb.append(image.getLink(networkID()));
                sb.append(',');
                responseParser.parseObject(image.getLink(networkID()),
                        (ObjectParser)photoParser.copyStructure());
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        query.addParameter("ids", sb);
        query.addParameter("fields", "link,width,height");

        ObjectParser response = Network.makeGetRequestAndParse(query, responseParser);
        for (Image image : images) {
            if (image.existsIn(networkID())) {
                fillImageFromParser(image, response.getObject());
            }
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

    private ObjectParser makeSharesParser() {
        return new ObjectParser()
                .parseInt("count");
    }

    private ObjectParser makeLikesOrCommentParser() {
        return new ObjectParser()
                .parseObject("summary", new ObjectParser()
                        .parseInt("total_count"));
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
                "message,created_time,id,likes.limit(0).summary(true),shares," +
                        "comments.limit(0).summary(true),attachments{media}");

        ObjectParser imageParser = makePhotoParser();

        ArrayParser attachmentsParser = new ArrayParser(-1,
                new ObjectParser()
                        .parseObject("media",
                                new ObjectParser().parseObject("image", imageParser)));

        ObjectParser postParser = new ObjectParser()
                .parseString("message")
                .parseInt("created_time")
                .parseString("id")
                .parseObject("shares", makeSharesParser())
                .parseObject("likes", makeLikesOrCommentParser())
                .parseObject("comments", makeLikesOrCommentParser())
                .parseObject("attachments", new ObjectParser()
                    .parseArray("data", attachmentsParser));

        ObjectParser responseParser = new ObjectParser()
                .parseArray("data", new ArrayParser(-1, postParser));

        ArrayParser response = Network.makeGetRequestAndParse(query, responseParser)
                .getArray();

        for (int i = 0; i < response.size(); i++) {
            postParser = response.getObject(i);
            String text = postParser.getString("");
            int date = postParser.getInt(0);
            String id = postParser.getString("");
            int shares = postParser.getObject().getInt(0);
            int likes = postParser.getObject().getObject().getInt(0);
            int comments = postParser.getObject().getObject().getInt(0);
            attachmentsParser = postParser.getObject().getArray();

            Info info = new Info(likes, shares, comments);

            LoudlyPost loudlyPost = callback.findLoudlyPost(id, networkID());
            if (loudlyPost != null) {
                loudlyPost.setInfo(networkID(), info);
                continue;
            }

            SinglePost post = new SinglePost(text, date, null, networkID(), id);
            post.setInfo(info);

            if (attachmentsParser != null) {
                for (int j = 0; j < attachmentsParser.size(); j++) {
                    imageParser = attachmentsParser.getObject(j).getObject().getObject();
                    Image image = new Image();
                    fillImageFromParser(image, imageParser);
                    post.addAttachment(image);
                }
            }
            callback.postLoaded(post);
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

    // TODO: 12/11/2015 and it
    @Override
    public List<Comment> getComments(Post post) throws IOException {
        Query query = makeSignedAPICall(post.getLink(networkID()) + "/comments");
        query.addParameter("fields", "message,from{id},created_time,id,comment_count,like_count,attachment");

        ObjectParser photoParser = makePhotoParser();
        ObjectParser attachmentParser = new ObjectParser()
                .parseString("type")
                .parseObject("media", new ObjectParser()
                        .parseObject("image", photoParser));
        ObjectParser commentParser = new ObjectParser()
                .parseString("id")
                .parseObject("from", new ObjectParser().parseString("id"))
                .parseString("message")
                .parseInt("created_time")
                .parseInt("comment_count")
                .parseInt("like_count")
                .parseObject("attachment", attachmentParser);

        ObjectParser responseParser = new ObjectParser()
                .parseArray("data", new ArrayParser(-1, commentParser));

        ArrayParser response = Network.makeGetRequestAndParse(query, responseParser)
                .getArray();

        LinkedList<Comment> comments = new LinkedList<>();
        LinkedList<Person> persons;
        ArrayList<String> ownerIds = new ArrayList<>();
        TreeSet<String> owners = new TreeSet<>();

        for (int i = 0; i < comments.size(); i++) {
            commentParser = response.getObject(i);
            String id = commentParser.getString("");
            String owner_id = commentParser.getObject().getString("");
            owners.add(owner_id);
            ownerIds.add(owner_id);
            String text = commentParser.getString("");
            int created = commentParser.getInt(0);
            int coms = commentParser.getInt(0);
            int like = commentParser.getInt(0);
            attachmentParser = commentParser.getObject();

            Comment comment = new Comment(text, created, null, networkID(), id);
            comment.setInfo(new Info(like, 0, coms));
            if (attachmentParser != null) {
                String type = attachmentParser.getString("");
                if (type.equals("photo")) {
                    photoParser = attachmentParser.getObject().getObject();
                    Image image = new Image();
                    fillImageFromParser(image, photoParser);
                    comment.addAttachment(image);
                }
            }
            comments.add(comment);
        }
        persons = getPersonsInfo(owners.toArray(new String[0]));
        int ind = 0;
        for (Comment comment : comments) {
            for (Person person : persons) {
                if (ownerIds.get(ind).equals(person.getId())) {
                    comment.setPerson(person);
                    break;
                }
            }
            ind++;
        }
        return comments;
    }

    private LinkedList<Person> getPersonsInfo(String... personsID) throws IOException {
        if (personsID.length == 0) {
            return new LinkedList<>();
        }

        Query query = makeSignedAPICall("");
        StringBuilder sb = new StringBuilder();
        ObjectParser personParser = new ObjectParser()
                .parseString("id")
                .parseString("first_name")
                .parseString("last_name")
                .parseObject("picture", new ObjectParser()
                        .parseObject("data", new ObjectParser()
                                .parseString("url")));

        ObjectParser responseParser = new ObjectParser();
        for (String id : personsID) {
            if (id.indexOf('_') != -1) id = id.substring(0, id.indexOf("_"));
            sb.append(id);
            sb.append(',');
            responseParser.parseObject(id, (ObjectParser)personParser.copyStructure());
        }
        sb.delete(sb.length() - 1, sb.length());

        query.addParameter("ids", sb);
        query.addParameter("fields", "first_name,last_name,picture");

        ObjectParser response = Network.makeGetRequestAndParse(query, responseParser);

        LinkedList<Person> result = new LinkedList<>();
        for (String string : personsID) {
            ObjectParser pParser = response.getObject();
            String id = pParser.getString("");
            String firstName = pParser.getString("");
            String lastName = pParser.getString("");
            String picture = pParser.getObject().getObject().getString("");

            Person person = new Person(firstName, lastName, picture, networkID());
            person.setId(id);
            result.add(person);
        }

        return result;
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

        try {
            parser = new JSONObject(response);
            JSONArray likers = parser.getJSONArray("data");

            if (likers.length() == 0) {
                return new LinkedList<>();
            }

            String[] ids = new String[likers.length()];
            for (int i = 0; i < likers.length(); i++) {
                String id = likers.getJSONObject(i).getString("id");
                ids[i] = id;
            }
            return getPersonsInfo(ids);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
