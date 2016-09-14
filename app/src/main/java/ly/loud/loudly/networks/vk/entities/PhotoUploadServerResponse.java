package ly.loud.loudly.networks.vk.entities;

import android.support.annotation.Nullable;

/**
 * Response from photo upload server
 *
 * @author Danil Kolikov
 */
public class PhotoUploadServerResponse {
    public String server, photo, hash;

    @Nullable
    public Error error;
}
