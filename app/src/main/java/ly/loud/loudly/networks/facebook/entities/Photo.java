package ly.loud.loudly.networks.facebook.entities;

import android.graphics.Point;
import android.support.annotation.NonNull;

import ly.loud.loudly.base.single.SingleImage;

import static ly.loud.loudly.networks.Networks.FB;

/**
 * @author Danil Kolikov
 */
public class Photo {
    public String src;

    public int width, height;

    @NonNull
    public SingleImage toImage(@NonNull String id) {
        // ToDo: Fix strange ID
        return new SingleImage(src, new Point(width, height), FB, id);
    }

    @NonNull
    public SingleImage toImage() {
        return toImage(src);
    }
}
