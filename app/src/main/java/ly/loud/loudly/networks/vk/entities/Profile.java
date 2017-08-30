package ly.loud.loudly.networks.vk.entities;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import ly.loud.loudly.base.entities.Person;

import static ly.loud.loudly.networks.Networks.VK;

/**
 * User's profile in VK api
 *
 * @author Danil Kolikov
 */
public class Profile {
    public String id;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("photo_50")
    public String photo50;

    @NonNull
    public Person toPerson() {
        return new Person(firstName, lastName, photo50, VK, id);
    }
}
