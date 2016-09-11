package ly.loud.loudly.networks.facebook.entities;

import android.support.annotation.Nullable;

import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;

/**
 * @author Danil Kolikov
 */
public class FbAttachment {
    public String type;

    public Media media;

    @Nullable
    public SingleAttachment toAttachment() {
        if (media.image != null) {
            return media.image.toImage();
        }
        return null;
    }
}
