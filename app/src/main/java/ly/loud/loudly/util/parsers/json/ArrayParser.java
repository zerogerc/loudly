package ly.loud.loudly.util.parsers.json;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ly.loud.loudly.util.parsers.Parser;

/** Parser of JSON arrays <br>
 */
public class ArrayParser implements Parser<ArrayParser>, JsonParser {
    private JsonParser parser;
    private int parseOnly;
    private ArrayList<JsonParser> result;

    /**
     * Default constructor
     * @param parseOnly Parse only N first objects. If parseOnly == -1, parse everything
     */
    public ArrayParser(JsonParser parser, int parseOnly) {
        this.parseOnly = parseOnly;
        this.parser = parser;
        result = new ArrayList<>();
    }

    /**
     * Constructor for arrays consisting of elements of one type
     * @param parseOnly Parse only N first objects. If parseOnly == -1, parse everything
     * @param type type of elements from JsonParser class
     */
    public ArrayParser(int type, int parseOnly) {
        this.parseOnly = parseOnly;
        this.parser = new FieldParser(type);
        result = new ArrayList<>();
    }

    public ArrayParser(JsonParser parser) {
        this(parser, Integer.MAX_VALUE);
    }

    @Override
    public void parse(JsonReader reader) throws IOException {
        reader.beginArray();
        int i = 0;
        while (reader.hasNext() && i < parseOnly) {
            if (reader.peek() == JsonToken.END_ARRAY) {
                break;
            }
            JsonParser parser = this.parser.copyStructure();
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

    public int getInt(int i, Integer defaultValue) {
        return get(INT, i, defaultValue);
    }

    public long getLong(int i, Long defaultValue) {
        return get(LONG, i, defaultValue);
    }

    public double getDouble(int i, Double defaultValue) {
        return get(DOUBLE, i, defaultValue);
    }

    public boolean getBoolean(int i, Boolean defaultValue) {
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
        return new ArrayParser(parser.copyStructure(), parseOnly);
    }
}
