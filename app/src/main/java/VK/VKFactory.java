package VK;

import VK.attachments.VKImage;
import base.attachments.Image;

/**
 * Created by Данил on 11/9/2015.
 */
public class VKFactory {
    public static VKImage get(Image image) {
        return (VKImage)image;
    }
}
