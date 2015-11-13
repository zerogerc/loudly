package Facebook;

import base.Post;
import base.Wrap;
import util.BackgroundAction;
import util.Parameter;
import util.ParameterBundle;

public class FacebookWrap extends Wrap<FacebookKeyKeeper> {
    public FacebookWrap(FacebookKeyKeeper keys) {
        super(keys);
    }

    @Override
    public String getInitialPostURL() {
        return "https://graph.facebook.com/me/feed";
    }

    @Override
    protected ParameterBundle getInitialPostParams(Post post) {
        ParameterBundle bundle = new ParameterBundle();
        bundle.addParameter("message", post.getText());
        bundle.addParameter("access_token", keys.getAccessToken());
        return bundle;
    }

    @Override
    protected BackgroundAction<Parameter> uploadImage(BackgroundAction publish) {
        return null;
    }

    @Override
    protected void parseResponse(Post post, String response) {
    }
}
