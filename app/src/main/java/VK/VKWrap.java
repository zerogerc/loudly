package VK;

import java.io.IOException;

import base.Post;
import base.Wrap;
import base.attachments.Image;
import util.Parameter;
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
    protected ParameterBundle getInitialPostParams(Post post) {
        ParameterBundle parameters = new ParameterBundle();
        parameters.addParameter("access_token", keys.getAccessToken());
        if (post.getText().length() > 0) {
            parameters.addParameter("message", post.getText());
        }
        return parameters;
    }

    @Override
    protected Parameter uploadImage(Image im) throws IOException {
        return null;
    }

    @Override
    protected void parseResponse(String response) {
        // Parse response here
    }
}
