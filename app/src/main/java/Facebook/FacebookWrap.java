package Facebook;

import base.Post;
import base.Wrap;

public class FacebookWrap extends Wrap<FacebookKeyKeeper> {
    public FacebookWrap(FacebookKeyKeeper keys) {
        super(keys);
    }

    @Override
    public String getInitialPostURL() {
        return null;
    }

    @Override
    public String getPostParameters(Post post) {
        return null;
    }

    @Override
    public void processPostResponse(String response) {

    }
}
