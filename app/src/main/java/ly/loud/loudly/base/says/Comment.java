package ly.loud.loudly.base.says;

import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.ui.adapter.Item;

public class Comment extends Say implements Item {
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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int getType() {
        return Item.COMMENT;
    }
}
