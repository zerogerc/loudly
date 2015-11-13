package MailRu;

import base.Post;
import base.Wrap;
import util.BackgroundAction;
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
    protected BackgroundAction<Parameter> uploadImage(BackgroundAction publish) {
        return null;
    }

    @Override
    protected void parseResponse(Post post, String response) {

    }
}
