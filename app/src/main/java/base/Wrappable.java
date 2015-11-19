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
     * Parse response to post request from server and save PostID to Post object
     * post post post post
     * @param post Post for posting
     * @param response URL-response from server
     */
    void parsePostResponse(Post post, String response);

    /**
     * Make queries for getting likes, shares and reposts
     * @param post Post for getting info
     * @return array of queries
     */
    Query[] makeGetQuery(Post post);

    /**
     * Parse JSON-response from server (// TODO: 11/19/2015 should be remade)
     * @param post Post that should contain info
     * @param response responses from server
     */
    void parseGetResponse(Post post, String[] response);


}
