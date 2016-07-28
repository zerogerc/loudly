package ly.loud.loudly.new_base.interfaces.says;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;

import java.util.ArrayList;

/**
 * @author Danil Kolikov
 */
public interface AbstractSay<A extends Attachment> {
    @Nullable
    String getText();

    long getDate();

    @NonNull
    ArrayList<A> getAttachments();
}
