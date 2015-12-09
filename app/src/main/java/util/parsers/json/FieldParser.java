package util.parsers.json;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import util.parsers.Parser;

/**
 * Parser for JSON file consist of only one token. May be used in ArrayParsers
 */
public class FieldParser implements Parser<FieldParser>, JsonParser {

    int type;
    Object value;

    public FieldParser(int type) {
        this.type = type;
    }

    @Override
    public FieldParser parse(InputStream stream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        parse(reader);
        return this;
    }

    @Override
    public void parse(JsonReader reader) throws IOException {
        switch (type) {
            case INT:
                value = reader.nextInt();
                break;
            case LONG:
                value = reader.nextLong();
                break;
            case DOUBLE:
                value = reader.nextDouble();
                break;
            case STRING:
                value = reader.nextString();
                break;
            case BOOLEAN:
                value = reader.nextBoolean();
                break;
            default:
                value = null;
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int type) {
        if (type == this.type) {
            return ((T) value);
        }
        return null;
    }

    @Override
    public JsonParser copyStructure() {
        return new FieldParser(type);
    }
}
