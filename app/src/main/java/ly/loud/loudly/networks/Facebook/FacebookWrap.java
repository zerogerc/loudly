package ly.loud.loudly.networks.Facebook;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.NetworkDescription;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.TokenExpiredException;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.attachments.LocalFile;
import ly.loud.loudly.base.attachments.LoudlyImage;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.util.BackgroundAction;
import ly.loud.loudly.util.Network;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.parsers.json.ArrayParser;
import ly.loud.loudly.util.parsers.json.ObjectParser;

import static ly.loud.loudly.application.models.PeopleGetterModel.LIKES;
import static ly.loud.loudly.application.models.PeopleGetterModel.SHARES;

// ToDo: Use my cool parsers
public class FacebookWrap extends Wrap {
    private static final int NETWORK = Networks.FB;
    private static final String MAIN_SERVER = "https://graph.facebook.com/v2.5/";
    private static final String POST_NODE = "me/feed";
    private static final String PHOTO_NODE = "me/photos";
    private static final NetworkDescription DESCRIPTION = new NetworkDescription() {
        @Override
        public boolean canPost() {
            return true;
        }

        @Override
        public boolean canDelete() {
            return true;
        }
    };

    @Override
    public int shouldUploadImage() {
        return Wrap.IMAGE_UPLOAD_OR_LINK;
    }

    @Override
    public void resetState() {
        // Have no state to reset
    }

    @Override
    public int networkID() {
        return NETWORK;
    }

    @Override
    public NetworkDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected Query makeAPICall(String node) {
        return new Query(MAIN_SERVER + node);
    }

    @Override
    protected Query makeSignedAPICall(String node, KeyKeeper keyKeeper) {
        Query query = makeAPICall(node);
        query.addParameter("access_token", ((FacebookKeyKeeper) keyKeeper).getAccessToken());
        return query;
    }

    @Override
    public String checkPost(LoudlyPost post) {
        if (post.getText().isEmpty() && post.getAttachments().isEmpty()) {
            return "Either text or image should be on post";
        }
        if (post.getAttachments().size() > 1) {
            return "Sorry, we can upload only 1 image";
        }
        return null;
    }

    @Override
    public String handleError(InputStream stream) throws IOException {
        ObjectParser errorParser = new ObjectParser()
                .parseInt("code")
                .parseString("error_user_msg");
        ObjectParser parser = new ObjectParser()
                .parseObject("error", errorParser);
        parser.parse(stream);

        errorParser = parser.getObject();
        int code = errorParser.getInt(0);
        String message = errorParser.getString("");

        // See https://developers.facebook.com/docs/graph-api/using-graph-api/
        if (code == 102 || code == 467) {
            throw new TokenExpiredException();
        }
        return message;
    }

    @Override
    protected void upload(Post post, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall(POST_NODE, keyKeeper);
        query.addParameter("message", post.getText());
        if (post.getAttachments().size() > 0) {
            for (Attachment attachment : post.getAttachments()) {
                Image image = (Image) attachment;
                query.addParameter("object_attachment", image.getLink());
            }
        }

        String response = Network.makePostRequest(query);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            String id = parser.getString("id");
            post.setLink(new Link(id));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void upload(Image image, BackgroundAction progress, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall(PHOTO_NODE, keyKeeper);
        query.addParameter("published", true);
        query.addParameter("no_story", true);

        String response;
        if (image instanceof LocalFile) {
            response = Network.makePostRequest(query, progress, "source",
                    ((LocalFile) image));
        } else {
            query.addParameter("url", image.getExternalLink());
            response = Network.makePostRequest(query);
        }

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            String id = parser.getString("id");

            image.setLink(new Link(id));
            if (image instanceof LoudlyImage && ((LoudlyImage) image).isLocal()) {
                ArrayList<Image> temp = new ArrayList<>();
                temp.add(image);
                getImageInfo(temp, keyKeeper);
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
    protected void getImageInfo(List<Image> images, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall("", keyKeeper);

        ObjectParser photoParser = new ObjectParser()
                .parseString("link")
                .parseInt("width")
                .parseInt("height");

        ObjectParser responseParser = new ObjectParser();

        StringBuilder sb = new StringBuilder();
        for (Image image : images) {
            if (image.existsIn(networkID())) {
                sb.append(image.getLink());
                sb.append(',');
                responseParser.parseObject(image.getLink().get(),
                        (ObjectParser) photoParser.copyStructure());
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        query.addParameter("ids", sb);
        query.addParameter("fields", "link,width,height");

        ObjectParser response = Network.makeGetRequestAndParse(query, responseParser, this);
        for (Image image : images) {
            if (image.existsIn(networkID())) {
                fillImageFromParser(image, response.getObject());
            }
        }
    }

    @Override
    protected void delete(Post post, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall(post.getLink().get(), keyKeeper);

        String response = Network.makeDeleteRequest(query);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            if (parser.getString("success").equals("true")) {
                post.cleanIds();
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
    protected List<Post> loadPosts(TimeInterval timeInterval, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall(POST_NODE, keyKeeper);
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

        ObjectParser attachmentParser = new ObjectParser()
                        .parseObject("media",
                                new ObjectParser().parseObject("image", imageParser));

        ObjectParser postParser = new ObjectParser()
                .parseString("message")
                .parseInt("created_time")
                .parseString("id")
                .parseObject("shares", makeSharesParser())
                .parseObject("likes", makeLikesOrCommentParser())
                .parseObject("comments", makeLikesOrCommentParser())
                .parseObject("attachments", new ObjectParser()
                        .parseArray("data", attachmentParser));

        ObjectParser responseParser = new ObjectParser()
                .parseArray("data", postParser);

        ArrayParser response = Network.makeGetRequestAndParse(query, responseParser, this)
                .getArray();

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < response.size(); i++) {
            postParser = response.getObject(i);
            String text = postParser.getString("");
            int date = postParser.getInt(0);
            String id = postParser.getString("");
            int shares = postParser.getObject().getInt(0);
            int likes = postParser.getObject().getObject().getInt(0);
            int comments = postParser.getObject().getObject().getInt(0);
            ArrayParser attachmentsParser = postParser.getObject().getArray();

            Info info = new Info(likes, shares, comments);

            Post post = new Post(text, date, null, networkID(), new Link(id));
            post.setInfo(info);

            if (attachmentsParser != null) {
                for (int j = 0; j < attachmentsParser.size(); j++) {
                    imageParser = attachmentsParser.getObject(j).getObject().getObject();
                    Image image = new Image();
                    fillImageFromParser(image, imageParser);
                    post.addAttachment(image);
                }
            }
            posts.add(post);
        }
        return posts;
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
            shares = object.getJSONObject("shares").getInt("count");
        }

        return new Info(likes, shares, comments);
    }

    @Override
    protected List<Pair<Post, Info>> getPostsInfo(List<Post> posts, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall("", keyKeeper);
        StringBuilder sb = new StringBuilder();
        for (Post post : posts) {
            sb.append(post.getLink());
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        query.addParameter("ids", sb);

        query.addParameter("fields", "likes.limit(0).summary(true),comments.limit(0).summary(true),shares");

        String response = Network.makeGetRequest(query);

        // ToDo: Use here fast parser
        JSONObject parser;

        ArrayList<Pair<Post, Info>> result = new ArrayList<>();

        try {
            parser = new JSONObject(response);
            for (Post post : posts) {
                if (post.existsIn(networkID())) {
                    if (parser.has(post.getLink().get())) {
                        JSONObject p = parser.getJSONObject(post.getLink().get());
                        Info info = getInfo(p);
                        if (!post.getInfo().equals(info)) {
                            result.add(new Pair<>(post, info));
                        }
                    } else {
                        result.add(new Pair<Post, Info>(post, null));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    // TODO: 12/11/2015 and it
    @Override
    protected List<Comment> getComments(SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        // todo Check for other types of elements
        Query query = makeSignedAPICall(element.getLink() + "/comments", keyKeeper);
        query.addParameter("date_format", "U");
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
                .parseArray("data", commentParser);

        ArrayParser response = Network.makeGetRequestAndParse(query, responseParser, this)
                .getArray();

        LinkedList<Comment> comments = new LinkedList<>();
        LinkedList<Person> persons;
        ArrayList<String> ownerIds = new ArrayList<>();
        TreeSet<String> owners = new TreeSet<>();

        for (int i = 0; i < response.size(); i++) {
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

            Comment comment = new Comment(text, created, null, networkID(), new Link(id));
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
        persons = getPersonsInfo(owners, keyKeeper);
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

    private LinkedList<Person> getPersonsInfo(Collection<String> personsID, KeyKeeper keyKeeper) throws IOException {
        if (personsID.isEmpty()) {
            return new LinkedList<>();
        }

        Query query = makeSignedAPICall("", keyKeeper);
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
            responseParser.parseObject(id, (ObjectParser) personParser.copyStructure());
        }
        sb.delete(sb.length() - 1, sb.length());

        query.addParameter("ids", sb);
        query.addParameter("fields", "first_name,last_name,picture");

        ObjectParser response = Network.makeGetRequestAndParse(query, responseParser, this);

        LinkedList<Person> result = new LinkedList<>();
        for (int i = 0; i < personsID.size(); i++) {
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
    protected LinkedList<Person> getPersons(int what, SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        String node;
        switch (what) {
            case LIKES:
                node = "/likes";
                break;
            case SHARES:
                node = "/sharedposts";
                break;
            default:
                node = "";
                break;
        }
        Query query = makeSignedAPICall(element.getLink() + node, keyKeeper);
        String response = Network.makeGetRequest(query);
        JSONObject parser;

        try {
            parser = new JSONObject(response);
            JSONArray likers = parser.getJSONArray("data");

            if (likers.length() == 0) {
                return new LinkedList<>();
            }

            ArrayList<String> ids = new ArrayList<>();
            for (int i = 0; i < likers.length(); i++) {
                ids.add(likers.getJSONObject(i).getString("id"));
            }
            return getPersonsInfo(ids, keyKeeper);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
