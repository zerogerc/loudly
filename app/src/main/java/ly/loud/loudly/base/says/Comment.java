package ly.loud.loudly.base.says;

import android.os.Parcel;

import ly.loud.loudly.new_base.Link;
import ly.loud.loudly.new_base.Person;

public class Comment extends Say{
    Person person;

    public Comment() {
        super();
        person = new Person();
    }

    public Comment(String text, Person person, int network, Link link) {
        super(text, network, link);
        this.person = person;
    }

    public Comment(String text, long date, Person person, int network, Link link) {
        super(text, date, network, link);
        this.person = person;
    }

    public Comment(Parcel source) {
        super(source);
        person = source.readParcelable(Person.class.getClassLoader());
    }


    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(person, flags);
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
