package VK;

import base.Post;
import base.Wrap;
import base.attachments.Image;
import util.BackgroundAction;
import util.Parameter;
import util.ParameterBundle;
import util.Query;

public class VKWrap extends Wrap<VKKeyKeeper> {
    private static final String TAG = "VK_WRAP_TAG";
    private static final String POST_SERVER = "https://api.vk.com/method/wall.post";

    public VKWrap(VKKeyKeeper keys) {
        super(keys);
    }

    @Override
    protected Query makePostQuery(Post post) {
        Query query = new Query(POST_SERVER);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        query.addParameter("access_token", keys.getAccessToken());
        return query;
    }

    @Override
    protected Parameter uploadImage(Image image, BackgroundAction publish) {
        return null;
    }

    @Override
    protected void parseResponse(Post post, String response) {
        // Parse response here
    }
}
