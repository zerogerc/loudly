package base.says;

import base.Person;
import ly.loud.loudly.PeopleList.Item;

public class Comment extends Say implements Item {
    Person person;

    public Comment() {
        super();
        person = new Person("","","",-1);
    }

    public Comment(String text, Person person, int network) {
        super(text, network);
        this.person = person;
    }

    public Comment(String text, long date, Person person, int network) {
        super(text, date, network);
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
