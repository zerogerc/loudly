package base.attachments;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import base.Attachable;

public class Text implements Attachable {
    private String text;

    public Text(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toURI() {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null; // Impossible case
        }
    }
}
