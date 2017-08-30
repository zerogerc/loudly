package ly.loud.loudly.networks.instagram.entities;

import android.support.annotation.Nullable;

/**
 * Response from Instagram Api. Contains Data, Meta with error and Pagination
 * @param <T>
 */
public class Data<T> {
    @Nullable
    public T data;

    @Nullable
    public Pagination pagination;

    @Nullable
    public Meta meta;

    public boolean isError() {
        return meta != null && meta.code != 200;
    }
}
