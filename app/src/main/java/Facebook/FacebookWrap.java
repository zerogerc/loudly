package Facebook;

import base.Networks;
import base.Post;
import base.Wrappable;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.Parameter;
import util.Query;

public class FacebookWrap implements Wrappable {
    private static final String POST_SERVER = "https://graph.facebook.com/me/feed";

    @Override
    public Query makePostQuery(Post post) {
        Query query = new Query(POST_SERVER);
        query.addParameter("message", post.getText());
        FacebookKeyKeeper keys = (FacebookKeyKeeper) Loudly.getContext().getKeyKeeper(Networks.FB);
        query.addParameter("access_token", keys.getAccessToken());
        return query;
    }

    @Override
    public Parameter uploadImage(Image image, BackgroundAction publish) {
        return null;
    }

    @Override
    public void parseResponse(Post post, String response) {
    }
}
