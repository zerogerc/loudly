package Facebook;

import base.Post;
import base.Wrap;
import base.attachments.Image;
import util.BackgroundAction;
import util.Parameter;
import util.ParameterBundle;
import util.Query;

public class FacebookWrap extends Wrap<FacebookKeyKeeper> {
    private static final String POST_SERVER = "https://graph.facebook.com/me/feed";
    public FacebookWrap(FacebookKeyKeeper keys) {
        super(keys);
    }

    @Override
    protected Query makePostQuery(Post post) {
        Query query = new Query(POST_SERVER);
        query.addParameter("message", post.getText());
        query.addParameter("access_token", keys.getAccessToken());
        return query;
    }

    @Override
    protected Parameter uploadImage(Image image, BackgroundAction publish) {
        return null;
    }

    @Override
    protected void parseResponse(Post post, String response) {
    }
}
