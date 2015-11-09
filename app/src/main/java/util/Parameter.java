package util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Parameter implements Comparable<Parameter>{
    public String name, value;

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int compareTo(Parameter another) {
        return name.compareTo(another.name);
    }

    @Override
    public String toString() {
        String encodedValue;
        try {
            encodedValue = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedValue = "";
        }
        return name + '=' + encodedValue;
    }
}