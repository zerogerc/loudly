package base;

import base.attachments.Image;
import util.BackgroundAction;
import util.Parameter;
import util.Query;

/**
 * Base interface for all interactions with particular social network.
 * Uses KeyKeepers, stored in Loudly Application
 */

public interface Wrappable {
    Query makePostQuery(Post post);

    /**
     * Make BackgroundAction, that can publish it's progress
     *
     * @param image Image that shoud be published
     * @param publish action, that can publish current progress to UI
     */
    Parameter uploadImage(Image image, BackgroundAction publish);

    /**
     * Parse response from server and save PostID to Post object
     *
     * @param post Post for posting
     * @param response URL-response from server
     */
    void parseResponse(Post post, String response);
}
