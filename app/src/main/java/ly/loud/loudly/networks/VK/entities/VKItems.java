package ly.loud.loudly.networks.VK.entities;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Item container for VK api
 *
 * @author Danil Kolikov
 */
public class VKItems<T> {
    public List<T> items;

    @Nullable
    public List<Profile> profiles;
}
