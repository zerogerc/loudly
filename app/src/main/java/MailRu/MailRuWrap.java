package MailRu;

import java.io.IOException;

import base.Post;
import base.Wrap;
import base.attachments.Image;
import util.Parameter;
import util.ParameterBundle;

public class MailRuWrap extends Wrap<MailRuKeyKeeper> {
    public MailRuWrap(MailRuKeyKeeper keys) {
        super(keys);
    }

    @Override
    public String getInitialPostURL() {
        return null;
    }

    @Override
    protected ParameterBundle getInitialPostParams(Post post) {
        return null;
    }

    @Override
    protected Parameter uploadImage(Image im) throws IOException {
        return null;
    }

    @Override
    protected void parseResponse(String response) {

    }
}
