package VK;

import base.Networks;
import base.Post;
import base.Wrap;
import util.ListenerHolder;

public class VKWrap extends Wrap<VKKeyKeeper> {
    private final String TAG = "VK_WRAP_TAG";
    public VKWrap(VKKeyKeeper keys) {
        super(keys);
    }

    @Override
    public final String getInitialPostURL() {
        return "https://api.vk.com/method/wall.post";
    }

    @Override
    public final String getPostParameters(Post post) {
        return "message=" + post.getText() + "&access_token=" + this.keys.getAccessToken();
    }

    @Override
    public void processPostResponse(String response) {
        //TODO
        ListenerHolder.getListener(Networks.VK).onSuccess(response);
    }
}
