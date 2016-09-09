package ly.loud.loudly.networks.instagram.entities;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import ly.loud.loudly.base.entities.Person;

import static ly.loud.loudly.networks.Networks.INSTAGRAM;

/**
 * Person in Instagram
 */
public class InstagramPerson {
    public String username;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("profile_picture")
    public String profilePicture;

    @NonNull
    public Person toPerson() {
        return new Person(fullName, "", profilePicture, INSTAGRAM, username);
    }
}
