package MailRu;

import base.Post;
import base.Wrap;
import base.attachments.Image;
import util.BackgroundAction;
import util.Parameter;
import util.ParameterBundle;
import util.Query;

public class MailRuWrap extends Wrap<MailRuKeyKeeper> {
    public MailRuWrap(MailRuKeyKeeper keys) {
        super(keys);
    }

    @Override
    protected Query makePostQuery(Post post) {
        return null;
    }

    @Override
    protected Parameter uploadImage(Image image, BackgroundAction publish) {
        return null;
    }

    @Override
    protected void parseResponse(Post post, String response) {

    }
}
