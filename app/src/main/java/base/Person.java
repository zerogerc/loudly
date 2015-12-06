package base;

import android.graphics.Bitmap;

public class Person {
    private String firstName, lastName;
    private String photoUrl;
    private int network;

    private Bitmap littlePhoto;

    public Person(String firstName, String lastName, String photoUrl, int network) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.network = network;
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

    public Bitmap getLittlePhoto() {
        return littlePhoto;
    }

    public void setLittlePhoto(Bitmap littlePhoto) {
        this.littlePhoto = littlePhoto;
    }
}
