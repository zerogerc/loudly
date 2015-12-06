package util;

public class Broadcasts {
    // TODO: 11/30/2015 Make them int

    /** Broadcast names **/
    public static final String KEYS = "keys";
    public static final String KEYS_SAVED = "keys.saved";
    public static final String KEYS_LOADED = "keys.loaded";

    public static final String AUTHORIZATION = "auth";

    public static final String POST_LOAD = "post.load";

    public static final String POST_UPLOAD = "post.upload";

    public static final String POST_GET_INFO = "post.info";

    public static final String POST_GET_PERSONS = "person";

    public static final String POST_DELETE = "delete";

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
