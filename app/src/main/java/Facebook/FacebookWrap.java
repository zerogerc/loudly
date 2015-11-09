package Facebook;

import base.Networks;
import base.Post;
import base.Wrap;
import util.ListenerHolder;

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
//        return "fields=id&access_token=" + keys.getAccessToken();
    }

    @Override
    public void processPostResponse(String response) {
        //TODO
        ListenerHolder.getListener(Networks.FB).onSuccess(response);
    }
}
