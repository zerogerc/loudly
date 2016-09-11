package ly.loud.loudly.networks.facebook.entities;

import android.graphics.Point;
import android.support.annotation.NonNull;

import ly.loud.loudly.base.single.SingleImage;

import static ly.loud.loudly.networks.Networks.FB;

/**
 * @author Danil Kolikov
 */
public class Picture {
    public String link;

    public int width, height;

    @NonNull
    public SingleImage toImage(@NonNull String id) {
        return new SingleImage(link, new Point(width, height), FB, id);
    }
}
