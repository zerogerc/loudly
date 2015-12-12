package util.parsers.json;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import util.parsers.Parser;

/** Parser of JSON arrays <br>
 */
public class ArrayParser implements Parser<ArrayParser>, JsonParser {
    JsonParser[] parsers;
    int parseOnly;
    ArrayList<JsonParser> result;

    /**
     * Default constructor
     * @param parseOnly Parse only N first objects. If parseOnly == -1, parse everything
     * @param parsers List of parsers. First parses first element, second - second and so on.
     *                If the number of element is greater than the amount of parsers,
     *                last parser will be used
     */
    public ArrayParser(int parseOnly, JsonParser... parsers) {
        this.parseOnly = parseOnly == -1 ? Integer.MAX_VALUE : parseOnly;
        this.parsers = parsers;
        result = new ArrayList<>();
    }

    /**
     * Constructor for arrays consisting of elements of one type
     * @param parseOnly Parse only N first objects. If parseOnly == -1, parse everything
     * @param type type of elements from JsonParser class
     */
    public ArrayParser(int parseOnly, int type) {
        this.parseOnly = parseOnly == -1 ? Integer.MAX_VALUE : parseOnly;
        this.parsers = new JsonParser[] {new FieldParser(type)};
        result = new ArrayList<>();
    }

    @Override
    public void parse(JsonReader reader) throws IOException {
        reader.beginArray();
        int i = 0;
        while (reader.hasNext() && i < parseOnly) {
            if (reader.peek() == JsonToken.END_ARRAY) {
                break;
            }
            JsonParser parser;
            if (i < parsers.length) {
                parser = parsers[i].copyStructure();
            } else {
                parser = parsers[parsers.length - 1].copyStructure();
            }
            parser.parse(reader);
            result.add(parser);
            i++;
        }
        reader.endArray();
    }

    @Override
    public ArrayParser parse(InputStream stream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        parse(reader);
        return this;
    }

    private JsonParser get(int i) {
        return result.get(i);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int type, int i, T defaultValue) {
        JsonParser parser = get(i);
        if (parser instanceof FieldParser) {
            return ((FieldParser) parser).get(type, defaultValue);
        }
        return defaultValue;
    }

    public int size() {
        return result.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public String getString(int i, String defaultValue) {
        return get(STRING, i, defaultValue);
    }

    // Make more safe
    public Integer getInt(int i, Integer defaultValue) {
        return get(INT, i, defaultValue);
    }

    public Long getLong(int i, Long defaultValue) {
        return get(LONG, i, defaultValue);
    }

    public Double getDouble(int i, Double defaultValue) {
        return get(DOUBLE, i, defaultValue);
    }

    public Boolean getBoolean(int i, Boolean defaultValue) {
        return get(BOOLEAN, i, defaultValue);
    }

    public ObjectParser getObject(int i) {
        JsonParser parser = get(i);
        if (parser instanceof ObjectParser) {
            return (ObjectParser) parser;
        }
        return null;
    }

    public ArrayParser getArray(int i) {
        JsonParser parser = get(i);
        if (parser instanceof ArrayParser) {
            return (ArrayParser) parser;
        }
        return null;
    }

    @Override
    public JsonParser copyStructure() {
        JsonParser[] temp = new JsonParser[parsers.length];
        for (int i = 0; i < parsers.length; i++) {
            temp[i] = parsers[i].copyStructure();
        }
        return new ArrayParser(parseOnly, temp);
    }
}
