package ly.loud.loudly.new_base.interfaces.says;

import android.support.annotation.NonNull;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;

/**
 * @author Danil Kolikov
 */
public interface AbstractComment<A extends Attachment> extends AbstractSay<A> {
    @NonNull
    Person getPerson();
}
