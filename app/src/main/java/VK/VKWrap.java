package VK;

import VK.attachments.VKImage;
import base.Networks;
import base.Post;
import base.Wrap;
import base.attachments.Attachment;
import base.attachments.Image;
import base.attachments.Video;
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
        for (Attachment attachment : post.getAttachments()) {
            if (attachment instanceof Image) {
                VKImage vkImage = (VKImage) attachment;
                parameters.addParameter(vkImage.toParameter());
            }
            if (attachment instanceof Video) {
            }
        }
        return parameters.toString();
    }
}
