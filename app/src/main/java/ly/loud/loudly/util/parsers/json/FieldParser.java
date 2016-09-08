package ly.loud.loudly.util.parsers.json;

import android.util.JsonReader;
import ly.loud.loudly.util.parsers.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    public <T> T get(int type, T defaultValue) {
        if (type == this.type) {
            if (value == null) {
                return defaultValue;
            }
            return ((T) value);
        }
        return defaultValue;
    }

    public int getInt(int defaultValue) {
        return get(INT, defaultValue);
    }

    public String getString(String defaultValue) {
        return get(STRING, defaultValue);
    }

    public double getDouble(double defaultValue) {
        return get(DOUBLE, defaultValue);
    }

    public boolean getInt(boolean defaultValue) {
        return get(BOOLEAN, defaultValue);
    }

    public long getLong(long defaultValue) {
        return get(LONG, defaultValue);
    }

    @Override
    public JsonParser copyStructure() {
        return new FieldParser(type);
    }
}
