package ly.loud.loudly.new_base.interfaces.says;

import android.support.annotation.Nullable;
import ly.loud.loudly.base.Location;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;

/**
 * @author Danil Kolikov
 */
public interface AbstractPost<A extends Attachment> extends AbstractSay<A> {
    @Nullable
    Location getLocation();
}
