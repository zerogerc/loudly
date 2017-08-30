package ly.loud.loudly.networks.facebook.entities;


import android.support.annotation.Nullable;

/**
 * @author Danil Kolikov
 */
public class Data<T> {
    @Nullable
    public T data;

    @Nullable
    public Paging paging;

    @Nullable
    public Error error;
}
