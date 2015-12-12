package base.says;

import base.Person;
import ly.loud.loudly.PeopleList.Item;

public class Comment extends Say implements Item {
    Person person;
    String link;

    public Comment() {
        super();
        person = new Person();
    }

    public Comment(String text, Person person, int network, String link) {
        super(text, network);
        this.person = person;
        this.link = link;
    }

    public Comment(String text, long date, Person person, int network, String link) {
        super(text, date, network);
        this.person = person;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
