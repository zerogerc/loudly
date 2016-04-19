package ly.loud.loudly.networks.Instagram;

import android.graphics.Point;
import android.util.Pair;
import ly.loud.loudly.base.*;
import ly.loud.loudly.base.attachments.Image;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Danil Kolikov
 */
public class InstagramWrap extends Wrap {
    private static final String MAIN_SERVER = "https://api.instagram.com/v1";
    private static int minId = 0;
    private static int maxId = 0;

    @Override
    public void resetState() {
        // Instagram have no state at least now
    }

    @Override
    public int networkID() {
        return Networks.INSTAGRAM;
    }

    @Override
    public int shouldUploadImage() {
        return Wrap.UPLOAD_LAST;
    }

    @Override
    public String checkPost(LoudlyPost post) {
        return null;
    }

    @Override
    protected Query makeAPICall(String endpoint) {
        return new Query(MAIN_SERVER + endpoint);
    }

    @Override
    protected Query makeSignedAPICall(String endpoint, KeyKeeper keyKeeper) {
        Query query = makeAPICall(endpoint);
        query.addParameter("access_token", ((InstagramKeyKeeper)keyKeeper).getAccessToken());
        return query;
    }

    @Override
    protected void upload(Post post, KeyKeeper keyKeeper) throws IOException {
        // ToDo: Post upload
    }

    @Override
    protected void upload(Image image, BackgroundAction progress, KeyKeeper keyKeeper) throws IOException {
        // ToDo: Image upload
    }

    @Override
    protected void delete(Post post, KeyKeeper keyKeeper) throws IOException {
        // ToDo: Post deletion
    }

    @Override
    protected List<Post> loadPosts(TimeInterval timeInterval, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall("/users/self/media/recent", keyKeeper);
        query.addParameter("count", 10);
        // ToDo: load more then 10 posts
        ObjectParser captionParser = new ObjectParser()
                .parseLong("created_time")
                .parseString("text");
        ObjectParser countParser = new ObjectParser()
                .parseInt("count");
        ObjectParser imageParser = new ObjectParser()
                .parseString("url")
                .parseInt("width")
                .parseInt("height");
        ObjectParser postParser = new ObjectParser()
                .parseObject("caption", captionParser)
                .parseString("id")
                .parseObject("comments", (ObjectParser)countParser.copyStructure())
                .parseObject("likes", (ObjectParser)countParser.copyStructure())
                .parseObject("images", new ObjectParser().parseObject("standard_resolution", imageParser));
        ObjectParser parser = new ObjectParser()
                .parseArray("data", postParser);

        parser = Network.makeGetRequestAndParse(query, parser);

        List<Post> result = new ArrayList<>();

        ArrayParser posts = parser.getArray();
        for (int i = 0; i < posts.size(); i++) {
            postParser = posts.getObject(i);
            captionParser = postParser.getObject();
            long time = captionParser.getLong(0L);
            String text = captionParser.getString("");
            String id = postParser.getString("");
            int comments = postParser.getObject().getInt(0);
            int likes = postParser.getObject().getInt(0);

            Info info = new Info(likes, 0, comments);
            Post post = new Post(text, time, null, networkID(), new Link(id));
            post.setInfo(info);

            imageParser = postParser.getObject();
            if (imageParser != null) {
                imageParser = imageParser.getObject();
                String url = imageParser.getString("");
                int width = imageParser.getInt(0);
                int height = imageParser.getInt(0);
                Image image = new Image(url, networkID(), new Link(url));
                image.setWidth(width);
                image.setHeight(height);
                post.addAttachment(image);
            }
            result.add(post);
        }
        return result;
    }

    @Override
    protected List<Pair<Post, Info>> getPostsInfo(List<Post> posts, KeyKeeper keyKeeper) throws IOException {
        // ToDo: Anyhow get info about posts
        return Collections.emptyList();
    }

    @Override
    protected List<Person> getPersons(int what, SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        // In Instagram there is no reposts
        Query query = makeSignedAPICall("/media/" + element.getLink().get() + "/likes", keyKeeper);
        ObjectParser personParser = new ObjectParser()
                .parseString("username")
                .parseString("full_name")
                .parseString("profile_picture");
        ObjectParser parser = new ObjectParser()
                .parseArray("data", personParser);
        parser = Network.makeGetRequestAndParse(query, parser);
        ArrayParser likes = parser.getArray();
        ArrayList<Person> result = new ArrayList<>();

        for (int i = 0; i < likes.size(); i++) {
            personParser = likes.getObject(i);
            String username = personParser.getString("");
            String name = personParser.getString("");
            String url = personParser.getString("");
            result.add(new Person(name, "", url, networkID()));
        }
        return result;
    }

    @Override
    protected List<Comment> getComments(SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        Query query = makeSignedAPICall("/media/" + element.getLink().get() + "/comments", keyKeeper);
        ObjectParser userParser = new ObjectParser()
                .parseString("full_name")
                .parseString("username")
                .parseString("profile_picture");
        ObjectParser commentParser = new ObjectParser()
                .parseLong("created_time")
                .parseString("text")
                .parseObject("from", userParser)
                .parseString("id");
        ObjectParser parser = new ObjectParser()
                .parseArray("data", commentParser);

        parser = Network.makeGetRequestAndParse(query, parser);
        ArrayParser comments = parser.getArray();
        List<Comment> result = new ArrayList<>();

        for (int i = 0; i < comments.size(); i++) {
            commentParser = comments.getObject(i);
            long time = commentParser.getLong(0L);
            String text = commentParser.getString("");
            userParser = commentParser.getObject();
            String name = userParser.getString("");
            String username = userParser.getString("");
            String picture = userParser.getString("");
            String id = commentParser.getString("");


            Comment comment = new Comment(text, time, new Person(name, "", picture, networkID()),
                    networkID(), new Link(id));
            result.add(comment);
        }
        return result;
    }

    @Override
    protected void getImageInfo(List<Image> images, KeyKeeper keyKeeper) throws IOException {
        // Not implemented
    }
}
