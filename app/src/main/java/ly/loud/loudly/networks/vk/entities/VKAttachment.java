package ly.loud.loudly.networks.vk.entities;

import android.graphics.Point;
import android.support.annotation.Nullable;

import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.single.SingleImage;

import static ly.loud.loudly.networks.Networks.VK;

/**
 * Attachment for VK api
 *
 * @author Danil Kolikov
 */
public class VKAttachment {
    public String type;

    @Nullable
    public Photo photo;

    @Nullable
    public SingleAttachment toAttachment() {
        if (photo != null) {
            return new SingleImage(photo.photo604, new Point(photo.widthPx, photo.heightPx),
                    VK, photo.id);
        }
        return null;
    }
}
