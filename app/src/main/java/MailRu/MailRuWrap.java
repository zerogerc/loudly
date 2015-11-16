package MailRu;

import base.Post;
import base.Wrappable;
import base.attachments.Image;
import util.BackgroundAction;
import util.Parameter;
import util.Query;

public class MailRuWrap implements Wrappable {

    @Override
    public Query makePostQuery(Post post) {
        return null;
    }

    @Override
    public Parameter uploadImage(Image image, BackgroundAction publish) {
        return null;
    }

    @Override
    public void parsePostResponse(Post post, String response) {

    }

    @Override
    public Query[] makeGetQuery(Post post) {
        return new Query[0];
    }

    @Override
    public void parseGetResponse(Post post, String[] response) {

    }
}
