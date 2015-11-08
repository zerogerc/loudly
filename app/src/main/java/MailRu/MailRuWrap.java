package MailRu;

import base.Post;
import base.Wrap;

public class MailRuWrap extends Wrap<MailRuKeyKeeper> {
    public MailRuWrap(MailRuKeyKeeper keys) {
        super(keys);
    }

    @Override
    public String getInitialPostURL() {
        return null;
    }

    @Override
    public String getPostParameters(Post post) {
        return null;
    }

    @Override
    public void processPostResponse(String response) {

    }
}
