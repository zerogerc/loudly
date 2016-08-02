package ly.loud.loudly.new_base.interfaces;

import android.support.annotation.NonNull;
import ly.loud.loudly.new_base.Info;

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
