package ly.loud.loudly.base.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;
import ly.loud.loudly.ui.adapters.holders.ListItem;

import static ly.loud.loudly.ui.adapters.holders.ItemTypes.PERSON;

public class Person implements ListItem, Parcelable {
    @NonNull
    private final String firstName;

    @NonNull
    private final String lastName;

    @Nullable
    private final String photoUrl;

    @Network
    private final int network;

    @NonNull
    private final String id;

    public Person(@NonNull String firstName,
                  @NonNull String lastName,
                  @Nullable String photoUrl,
                  @Network int network,
                  @NonNull String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.network = network;
        this.id = id;
    }

    public Person(Parcel source) {
        firstName = source.readString();
        lastName = source.readString();
        photoUrl = source.readString();
        network = source.readInt();
        id = source.readString();
    }

    @NonNull
    public String getId() {
        return id;
    }


    @NonNull
    public String getFirstName() {
        return firstName;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    @Nullable
    public String getPhotoUrl() {
        return photoUrl;
    }

    @Network
    public int getNetwork() {
        return network;
    }

    @Override
    @ItemType
    public int getType() {
        return PERSON;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(photoUrl);
        dest.writeInt(network);
        dest.writeString(id);
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}
