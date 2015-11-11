package Facebook.attachments;

import base.attachments.Image;
import util.Parameter;

public class FacebookImage extends Image {
    public FacebookImage(String initialLink) {
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
