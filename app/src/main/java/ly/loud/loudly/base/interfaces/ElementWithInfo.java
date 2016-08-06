package ly.loud.loudly.base.interfaces;

import android.support.annotation.NonNull;
import ly.loud.loudly.base.entities.Info;

/**
 * Interface for elements with information
 *
 * @author Danil Kolikov
 */
public interface ElementWithInfo {
    /**
     * Get information about this element - likes, shares, comments
     *
     * @return Information
     * @see Info
     */
    @NonNull
    Info getInfo();
}
