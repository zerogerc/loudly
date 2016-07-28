package ly.loud.loudly.new_base.interfaces.attachments;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Interface for image, that exist in one network
 * 
 * @author Danil Kolikov
 */
public interface SingleImage extends SingleAttachment {
    @Nullable
    String getUrl();

    @NonNull
    Point getSize();
}
