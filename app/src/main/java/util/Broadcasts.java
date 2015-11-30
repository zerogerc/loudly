package util;

public class Broadcasts {
    // TODO: 11/30/2015 Make them int

    /** Broadcast names **/
    public static final String KEYS = "ly.loud.loudly.keys";
    public static final String KEYS_SAVED = "ly.load.loudly.keys.saved";
    public static final String KEYS_LOADED = "ly.loud.loudly.keys.loaded";

    public static final String AUTHORIZATION = "ly.loud.loudly.auth";

    public static final String POST_LOAD = "ly.loud.loudly.post.load";

    public static final String POST_UPLOAD = "ly.loud.loudly.post.upload";

    public static final String POST_GET_INFO = "ly.loud.loudly.post.info";

    /** Fields in broadcasts **/

    public static final String ID_FIELD = "id";
    public static final String PROGRESS_FIELD = "progress";
    public static final String ERROR_FIELD = "error";
    public static final String NETWORK_FIELD = "network";
    public static final String IMAGE_FIELD = "image";
    public static final String STATUS_FIELD = "status";

    /** Broadcast parameters **/
    public static final String STARTED = "started";
    public static final String PROGRESS = "progress";
    public static final String FINISHED = "finished";

    public static final String IMAGE = "image";
    public static final String IMAGE_FINISHED = "imgfin";
    public static final String ERROR = "error";

    public static final String SAVED = "saved";
    public static final String LOADED = "loaded";

    public static final String ERROR_KIND = "eknd";
    public static final String DATABASE_ERROR = "db";
    public static final String NETWORK_ERROR = "network";
    public static final String AUTH_FAIL = "auth";
}
