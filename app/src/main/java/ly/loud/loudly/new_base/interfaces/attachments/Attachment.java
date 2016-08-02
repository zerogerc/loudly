package ly.loud.loudly.new_base.interfaces.attachments;

import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Interface for attachment
 *
 * @author Danil Kolikov
 */
public interface Attachment extends Parcelable {
    int TYPE_IMAGE = 0;

    /**
     * Type of attachment (for storing in database)
     *
     * @return One of constants, specified in this interface
     */
    int getType();

    /**
     * Get extra for saving in database
     *
     * @return Strings that can be saved into database
     */
    @Nullable
    String getExtra();
}
