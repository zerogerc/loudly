package ly.loud.loudly.networks.Facebook.entities;

import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

/**
 * @author Danil Kolikov
 */
public class FbPerson {
    public String id;

    @SerializedName("first_name")
    @Nullable
    public String firstName;

    @SerializedName("last_name")
    @Nullable
    public String lastName;

    @Nullable
    public Data<Element> picture;
}
