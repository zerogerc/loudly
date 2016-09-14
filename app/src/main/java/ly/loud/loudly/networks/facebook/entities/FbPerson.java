package ly.loud.loudly.networks.facebook.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import ly.loud.loudly.base.entities.Person;

import static ly.loud.loudly.networks.Networks.FB;

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

    @NonNull
    public Person toPerson() {
        Data<Element> photo = picture;
        String url;
        if (photo == null) {
            url = null;
        } else {
            //noinspection ConstantConditions Photo has data
            url = photo.data.url;
        }
        String firstName = this.firstName == null ? "" : this.firstName;
        String lastName = this.lastName == null ? "" : this.lastName;
        return new Person(firstName, lastName, url, FB, id);
    }
}
