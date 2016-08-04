package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.os.Parcelable;

import ly.loud.loudly.ui.brand_new.ItemTypeAnnotation.ItemType;
import ly.loud.loudly.ui.brand_new.adapter.ListItem;

import static ly.loud.loudly.ui.brand_new.ItemTypeAnnotation.PERSON;

public class Person implements ListItem, Parcelable {
    private String firstName, lastName;
    private String photoUrl;
    private int network;
    private String id;

    public Person() {
        this.firstName = null;
        this.lastName = null;
        this.photoUrl = null;
        this.network = -1;
    }

    public Person(String firstName, String lastName, String photoUrl, int network) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.network = network;
    }

    public Person(Parcel source) {
        firstName = source.readString();
        lastName = source.readString();
        photoUrl = source.readString();
        network = source.readInt();
        id = source.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

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
