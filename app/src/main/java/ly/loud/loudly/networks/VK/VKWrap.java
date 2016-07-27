package ly.loud.loudly.networks.VK;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.NetworkDescription;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.TokenExpiredException;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.attachments.LocalFile;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.util.BackgroundAction;
import ly.loud.loudly.util.Network;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.parsers.StringParser;
import ly.loud.loudly.util.parsers.json.ArrayParser;
import ly.loud.loudly.util.parsers.json.ObjectParser;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.SHARES;

// ToDo: Use my cool parsers
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

    private static int offset = 0;

    @Override
    public int shouldUploadImage() {
        return Wrap.IMAGE_ONLY_UPLOAD;
    }

    @Override
    public void resetState() {
        offset = 0;
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
    protected Query makeAPICall(String method) {
        Query query = new Query(MAIN_SERVER + method);
        query.addParameter("v", API_VERSION);
        return query;
    }

    @Override
    protected Query makeSignedAPICall(String method, KeyKeeper keyKeeper) {
        Query query = makeAPICall(method);
        query.addParameter(ACCESS_TOKEN, ((VKKeyKeeper) keyKeeper).getAccessToken());
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
                .parseInt("error_code")
                .parseString("error_msg");
        ObjectParser parser = new ObjectParser()
                .parseObject("error", errorParser);
        parser.parse(stream);
        errorParser = parser.getObject();
        int code = errorParser.getInt(0);
        String message = errorParser.getString("");
        handleErrorCodes(code);

        return message;
    }

    private void handleErrorCodes(int code) throws TokenExpiredException {
        // See https://vk.com/dev/errors
        if (code == 14) {
            // Bad, bad captcha
            // ToDo: Captcha
        }

        if (code == 5) {
            throw new TokenExpiredException();
        }
    }

    @Override
    protected void upload(Post post, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall(POST_METHOD, keyKeeper);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        if (post.getAttachments().size() > 0) {
            Image image = (Image) post.getAttachments().get(0);
            String userID = ((VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID())).getUserId();
            query.addParameter("attachments", "photo" + userID + "_" + image.getLink());
        }

        String response = Network.makePostRequest(query);

        JSONObject parser;
        try {
            parser = new JSONObject(response);
            String id = parser.getJSONObject("response").getString("post_id");
            post.setLink(new Link(id));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void upload(Image image, BackgroundAction progress, KeyKeeper keyKeeper) throws IOException {
        Query getUploadServerAddress = makeSignedAPICall(PHOTO_UPLOAD_METHOD, keyKeeper);
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

        // In VK we can only upload images as files
        response = Network.makePostRequest(imageUploadQuery, progress, "photo", (LocalFile) image);

        Query getPhotoId = makeSignedAPICall(SAVE_PHOTO_METHOD, keyKeeper);

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
            if (image.getExternalLink() == null) {
                image.setExternalLink(url);
            }
            int height = parser.getInt("height");
            int width = parser.getInt("width");
            image.setLink(new Link(id));
            image.setHeight(height);
            image.setWidth(width);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void getImageInfo(List<Image> images, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall("photos.getById", keyKeeper);
        StringBuilder sb = new StringBuilder();
        for (Image image : images) {
            sb.append(((VKKeyKeeper) keyKeeper).getUserId());
            sb.append('_');
            sb.append(image.getLink());
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        query.addParameter("photos", sb);

        ObjectParser photoParser = makePhotoParser();
        ObjectParser parser = new ObjectParser()
                .parseArray("response", photoParser);

        ArrayParser response = Network.makeGetRequestAndParse(query, parser, this).getArray();
        if (response.size() != images.size()) {
            throw new IOException("Can't find image in network " + networkID());
        }
        int ind = 0;
        for (Image image : images) {
            fillImageFromParser(image, response.getObject(ind++));
        }
    }

    @Override
    protected void delete(Post post, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall(DELETE_METHOD, keyKeeper);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(NETWORK);
        query.addParameter("owner_id", keys.getUserId());
        query.addParameter("post_id", post.getLink());

        String response = Network.makeGetRequestAndParse(query, new StringParser(), this);
        try {
            JSONObject object = new JSONObject(response);
            if (object.has("error")) {
                object = object.getJSONObject("error");
                int code = object.getInt("error_code");
                String message = object.getString("error_msg");
                handleErrorCodes(code);
                throw new IOException(message);
            } else {
                post.cleanIds();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Pair<Post, Info>> getPostsInfo(List<Post> posts, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall(GET_METHOD, keyKeeper);
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(networkID());
        StringBuilder sb = new StringBuilder();
        for (Post post : posts) {
            if (post.existsIn(networkID())) {
                sb.append(keys.getUserId());
                sb.append('_');
                sb.append(post.getLink());
                sb.append(',');
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        query.addParameter("posts", sb);

        ObjectParser infoParser = new ObjectParser()
                .parseInt("count");

        ObjectParser postParser = new ObjectParser()
                .parseString("id")
                .parseObject("likes", (ObjectParser) infoParser.copyStructure())
                .parseObject("comments", (ObjectParser) infoParser.copyStructure())
                .parseObject("shares", (ObjectParser) infoParser.copyStructure());

        ObjectParser responseParser = new ObjectParser()
                .parseArray("response", postParser);

        ArrayParser response = Network.makeGetRequestAndParse(query, responseParser, this)
                .getArray();

        Iterator<Post> iterator = posts.listIterator();
        ArrayList<Pair<Post, Info>> result = new ArrayList<>();
        for (int i = 0; i < response.size(); i++) {
            postParser = response.getObject(i);
            String id = postParser.getString("");
            int likes = postParser.getObject().getInt(0);
            int comments = postParser.getObject().getInt(0);
            int shares = postParser.getObject().getInt(0);

            while (iterator.hasNext()) {
                Post post = iterator.next();

                // Todo: (sic!)
                if (post.getLink() != null) {
                    if (post.getLink().equals(id)) {
                        Info info = new Info(likes, shares, comments);

                        if (!post.getInfo().equals(info)) {
                            result.add(new Pair<>(post, info));
                        }
                        break;

                    } else {
                        result.add(new Pair<Post, Info>(post, null));
                        post.getLink().setValid(false);
                    }
                }
            }
        }
        return result;
    }


    private ObjectParser makePhotoParser() {
        return new ObjectParser()
                .parseString("id")
                .parseString("photo_604")
                .parseInt("width")
                .parseInt("height");
    }

    private void fillImageFromParser(Image image, ObjectParser photoParser) {
        String photoId = photoParser.getString("");
        String link = photoParser.getString("");
        int width = photoParser.getInt(0);
        int height = photoParser.getInt(0);

        image.setExternalLink(link);
        image.setLink(new Link(photoId));
        image.setWidth(width);
        image.setHeight(height);
    }

    @Override
    protected List<Post> loadPosts(TimeInterval timeInterval, KeyKeeper keyKeeper) throws IOException {
        offset = Math.max(0, offset - 5);


        long earliestPost = -1;
        LinkedList<Post> posts = new LinkedList<>();
        do {
            Query query = makeAPICall(LOAD_POSTS_METHOD);
            query.addParameter("owner_id", ((VKKeyKeeper) keyKeeper).getUserId());
            query.addParameter("filter", "owner");
            query.addParameter("count", "10");
            query.addParameter("offset", offset);

            ObjectParser photoParser = makePhotoParser();

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
                    .parseArray("attachments", attachmentParser);

            ObjectParser responseParser = new ObjectParser()
                    .parseArray("items", postParser);

            ObjectParser parser = new ObjectParser()
                    .parseObject("response", responseParser);

            ObjectParser response = Network.makeGetRequestAndParse(query, parser, this);

            ArrayParser arrayParser = response.getObject().getArray();
            if (arrayParser.size() == 0) {
                break;
            }

            for (int i = 0; i < arrayParser.size(); i++) {
                postParser = arrayParser.getObject(i);
                String id = postParser.getString("");
                long date = postParser.getLong(0L);
                String text = postParser.getString("");

                int likes = postParser.getObject().getInt(0);
                int shares = postParser.getObject().getInt(0);
                int comments = postParser.getObject().getInt(0);

                ArrayParser attachments = postParser.getArray();

                Info info = new Info(likes, shares, comments);

                if (timeInterval.contains(date)) {
                    Post res = new Post(text, date, null, networkID(), new Link(id));
                    res.setInfo(info);

                    for (int j = 0; j < attachments.size(); j++) {
                        attachmentParser = attachments.getObject(j);
                        String type = attachmentParser.getString("");
                        if (type.equals("photo") || type.equals("posted_photo")) {
                            photoParser = attachmentParser.getObject();
                            Image image = new Image();
                            fillImageFromParser(image, photoParser);
                            res.addAttachment(image);
                        }
                    }
                    posts.add(res);
                    offset++;
                }
                earliestPost = date;
            }
        } while (timeInterval.contains(earliestPost));
        return posts;
    }

    @Override
    protected List<Comment> getComments(SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        if (!(element instanceof Post)) {
            return new LinkedList<>(); // TODO: 12/12/2015  
        }
        Query query = makeSignedAPICall("wall.getComments", keyKeeper);

        query.addParameter("owner_id", ((VKKeyKeeper) keyKeeper).getUserId());
        query.addParameter("post_id", element.getLink());
        query.addParameter("need_likes", 1);
        query.addParameter("count", 20);
        query.addParameter("sort", "asc");
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
                .parseString("id")
                .parseString("from_id")
                .parseLong("date")
                .parseString("text")
                .parseObject("likes", new ObjectParser().parseInt("count"))
                .parseObject("reposts", new ObjectParser().parseInt("count"))
                .parseObject("comments", new ObjectParser().parseInt("count"))
                .parseArray("attachments", attachmentParser);

        ObjectParser personParser = new ObjectParser()
                .parseString("id")
                .parseString("first_name")
                .parseString("last_name")
                .parseString("photo_50");

        ObjectParser parser = new ObjectParser()
                .parseArray("items", commentParser)
                .parseArray("profiles", personParser);

        ObjectParser response = Network.makeGetRequestAndParse(query,
                new ObjectParser().parseObject("response", parser), this)
                .getObject();

        ArrayParser posts = response.getArray();
        ArrayParser persons = response.getArray();

        LinkedList<Person> profiles = new LinkedList<>();
        for (int i = 0; i < persons.size(); i++) {
            ObjectParser person = persons.getObject(i);
            String id = person.getString("");
            String firstName = person.getString("");
            String lastName = person.getString("");
            String photo = person.getString("");
            Person p = new Person(firstName, lastName, photo, networkID());
            p.setId(id);
            profiles.add(p);
        }

        LinkedList<Comment> comments = new LinkedList<>();

        for (int i = 0; i < posts.size(); i++) {
            commentParser = posts.getObject(i);
            String id = commentParser.getString("");
            String userID = commentParser.getString("");
            Person author = null;
            for (Person p : profiles) {
                if (p.getId().equals(userID)) {
                    author = p;
                    break;
                }
            }

            long date = commentParser.getLong(0L);
            String text = commentParser.getString("");

            int likes = commentParser.getObject().getInt(0);
            ArrayParser attachments = commentParser.getArray();

            Comment comment = new Comment(text, date, author, networkID(), new Link(id));
            if (attachments != null) {
                for (int j = 0; j < attachments.size(); j++) {
                    attachmentParser = attachments.getObject(i);
                    String type = attachmentParser.getString("");
                    if (type.equals("photo")) {
                        Image image = new Image();
                        fillImageFromParser(image, attachmentParser.getObject());
                        comment.addAttachment(image);
                    }
                }
            }
            comment.setInfo(new Info(likes, 0, 0));
            comments.add(comment);
        }
        return comments;
    }

    @Override
    protected LinkedList<Person> getPersons(int what, SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall("likes.getList", keyKeeper);
        String type;
        if (element instanceof Post) {
            type = "post";
        } else if (element instanceof Image) {
            type = "photo";
        } else if (element instanceof Comment) {
            type = "comment";
        } else {
            return new LinkedList<>();
        }

        query.addParameter("type", type);
        query.addParameter("owner_id", ((VKKeyKeeper) keyKeeper).getUserId());
        query.addParameter("item_id", element.getLink());
        String filter;
        switch (what) {
            case LIKES:
                filter = "likes";
                break;
            case SHARES:
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

        Query getPeopleQuery = makeSignedAPICall("users.get", keyKeeper);

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
            return new LinkedList<>();
        }

        response = Network.makeGetRequest(getPeopleQuery);

        LinkedList<Person> result = new LinkedList<>();

        JSONArray people;
        try {
            people = new JSONObject(response).getJSONArray("response");
            for (int i = 0; i < people.length(); i++) {
                JSONObject person = people.getJSONObject(i);
                String id = person.getString("id");
                String firstName = person.getString("first_name");
                String lastName = person.getString("last_name");
                String photoURL = person.getString("photo_50");

                Person person1 = new Person(firstName, lastName, photoURL, networkID());
                person1.setId(id);
                result.add(person1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return new LinkedList<>();
        }

        return result;
    }
}
