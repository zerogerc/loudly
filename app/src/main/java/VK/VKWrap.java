package VK;

import base.Networks;
import base.Post;
import base.Wrappable;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.Parameter;
import util.Query;

public class VKWrap implements Wrappable {
    private static final String TAG = "VK_WRAP_TAG";
    private static final String POST_SERVER = "https://api.vk.com/method/wall.post";

    @Override
    public Query makePostQuery(Post post) {
        Query query = new Query(POST_SERVER);
        if (post.getText().length() > 0) {
            query.addParameter("message", post.getText());
        }
        VKKeyKeeper keys = (VKKeyKeeper) Loudly.getContext().getKeyKeeper(Networks.VK);
        query.addParameter("access_token", keys.getAccessToken());
        return query;
    }

    @Override
    public Parameter uploadImage(Image image, BackgroundAction publish) {
        return null;
    }

    @Override
    public void parseResponse(Post post, String response) {
        // Parse response here
    }
}
