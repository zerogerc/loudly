package Facebook;

import base.Post;
import base.Wrap;

public class FacebookWrap extends Wrap<FacebookKeyKeeper> {
    public FacebookWrap(FacebookKeyKeeper keys) {
        super(keys);
    }

    @Override
    public String getInitialPostURL() {
        return "https://graph.facebook.com/me/feed";
    }

    @Override
    public String getPostParameters(Post post) {
        return "message=" + post.getText()+ "&access_token=" + keys.getAccessToken();
    }
}
