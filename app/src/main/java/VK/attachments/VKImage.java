package VK.attachments;

import base.attachments.Image;
import util.Parameter;

public class VKImage extends Image {
    public VKImage(String initialLink) {
        super(initialLink);
    }

    @Override
    public void upload() {

    }

    @Override
    public Parameter toParameter() {
        return null;
    }
}
