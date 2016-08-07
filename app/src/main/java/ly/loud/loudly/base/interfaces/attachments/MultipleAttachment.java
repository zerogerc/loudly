package ly.loud.loudly.base.interfaces.attachments;

import ly.loud.loudly.base.interfaces.MultipleNetworkElement;

/**
 * Interface for attachment, that exists in many networks
 *
 * @author Danil Kolikov
 */
public interface MultipleAttachment extends Attachment, MultipleNetworkElement<SingleAttachment> {
}
