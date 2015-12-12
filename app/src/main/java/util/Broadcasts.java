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
    public static final String ERROR_KIND = "eknd";

    /** Broadcast parameters **/
    public static final int STARTED = 0;
    public static final int PROGRESS = 1;
    public static final int FINISHED = 2;

    public static final int IMAGE = 3;
    public static final int IMAGE_FINISHED = 4;
    public static final int ERROR = 5;

    public static final int SAVED = 6;
    public static final int LOADED = 7;

    public static final int DATABASE_ERROR = 8;
    public static final int NETWORK_ERROR = 9;
    public static final int AUTH_FAIL = 10;
    public static final int INVALID_TOKEN = 11;
}
