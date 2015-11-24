package base;

import java.util.LinkedList;

import Facebook.FacebookWrap;
import VK.VKWrap;
import base.attachments.Image;
import util.BackgroundAction;
import util.Parameter;
import util.Query;

/**
 * Base interface for all interactions with particular social network.
 * Uses KeyKeepers, stored in Loudly Application
 */

public abstract class Wrap {
    /**
     * @return ID of the network (from Networks class)
     */
    public abstract int networkID();

    public abstract Query makePostQuery(Post post);

    /**
     * Make BackgroundAction, that can publish it's progress
     *
     * @param image Image that shoud be published
     * @param publish action, that can publish current progress to UI
     */
    public abstract Parameter uploadImage(Image image, BackgroundAction publish);

    /**
     * Parse response to post request from server and save PostID to Post object
     * post post post post
     * @param post Post for posting
     * @param response URL-response from server
     */
    public abstract void parsePostResponse(Post post, String response);

    /**
     * Make query for getting likes, shares and reposts
     * @param post Post for getting info
     * @return array of queries
     */
    public abstract Query makeGetQueries(Post post);

    /**
     * Parse JSON-response from server (// TODO: 11/19/2015 should be remade)
     * @param post Post that should contain info
     * @param response responses from server
     */
    public abstract void parseGetResponse(Post post, String response);

    public static Wrap makeWrap(int network) {
        switch (network) {
            case Networks.FB:
                return new FacebookWrap();
            case Networks.VK:
                return  new VKWrap();
            default:
                return null;
        }
    }

    public abstract Query makeDeleteQuery(Post post);

    public abstract void parseDeleteResponse(Post post, String response);

    public abstract Query makeLoadPostsQuery(long sinceID, long beforeID, long sinceTime, long beforeTime);
    public abstract long parsePostsLoadedResponse(LinkedList<Post> posts, long sinceTime, long beforeTime,
                                                  String response);
}
