package ly.loud.loudly.networks.instagram.entities;

import android.support.annotation.Nullable;

public class Data<T> {
    @Nullable
    public T data;

    @Nullable
    public Pagination pagination;

    @Nullable
    public Meta meta;
}
