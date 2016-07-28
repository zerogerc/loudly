package ly.loud.loudly.new_base.interfaces.says;

import ly.loud.loudly.new_base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.MultipleAttachment;

/**
 * @author Danil Kolikov
 */
public interface MultiplePost
        extends AbstractPost<MultipleAttachment>, MultipleNetworkElement<SinglePost> {
}
