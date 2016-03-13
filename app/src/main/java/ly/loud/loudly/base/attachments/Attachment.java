package ly.loud.loudly.base.attachments;

import ly.loud.loudly.base.SingleNetwork;

public interface Attachment extends SingleNetwork {
    int IMAGE = 0;

    int getType();
    String getExtra();

//    static Attachment makeAttachment(int type, String extra, String[] links) {
//        switch (type) {
//            case IMAGE:
//                return new Image(extra, links);
//            default:
//                return null;
//        }
//    }
}
