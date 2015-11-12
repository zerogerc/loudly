package Facebook;

import java.io.IOException;

import base.Post;
import base.Wrap;
import base.attachments.Image;
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
    protected Parameter uploadImage(Image im) throws IOException {
        return null;
    }

    @Override
    protected void parseResponse(String response) {
    }
}
