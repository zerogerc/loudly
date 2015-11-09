package VK;

import base.Networks;
import base.Post;
import base.Wrap;
import base.attachments.Attachable;
import util.ListenerHolder;
import util.ParameterBundle;

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
        ParameterBundle parameters = new ParameterBundle();
        parameters.addParameter("access_token", keys.getAccessToken());
        if (post.getText().length() > 0) {
            parameters.addParameter("message", post.getText());
        }
        for (Attachable attachment : post.getAttachments()) {
            parameters.addParameter(attachment.toParameter());
        }
        return parameters.toString();
    }

    @Override
    public void processPostResponse(String response) {
        //TODO
        ListenerHolder.getListener(Networks.VK).onSuccess(response);
    }
}
