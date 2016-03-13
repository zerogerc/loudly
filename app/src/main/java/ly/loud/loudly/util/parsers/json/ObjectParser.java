package ly.loud.loudly.util.parsers.json;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ly.loud.loudly.util.parsers.Parser;

/**
 * JSON Objects parser. <br>
 * Firstly you should mention which fields do you need using "parseSomething" methods<br>
 * After parsing you can get parameters in the same order as you added them to the parser
 */
public class ObjectParser implements Parser<ObjectParser>, JsonParser {
    private ArrayList<Field> fields;
    private int index;

    public ObjectParser() {
        fields = new ArrayList<>();
        index = 0;
    }

    @Override
    public ObjectParser parse(InputStream stream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        parse(reader);
        return this;
    }

    @Override
    public void parse(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            final String field = reader.nextName();
            if (field == null) {
                reader.skipValue();
            }

            boolean found = false;
            for (int i = 0; i < fields.size(); i++) {
                if (fields.get(i).name.equals(field)) {
                    fields.get(i).parser.parse(reader);
                    found = true;
                    break;
                }
            }
            if (!found) {
                reader.skipValue();
            }
        }
        reader.endObject();
        index = 0;
    }

    public ObjectParser parseString(String name) {
        fields.add(new Field(name, JsonParser.STRING));
        return this;
    }

    public ObjectParser parseInt(String name) {
        fields.add(new Field(name, JsonParser.INT));
        return this;
    }

    public ObjectParser parseLong(String name) {
        fields.add(new Field(name, JsonParser.LONG));
        return this;
    }

    public ObjectParser parseDouble(String name) {
        fields.add(new Field(name, JsonParser.DOUBLE));
        return this;
    }

    public ObjectParser parseBoolean(String name) {
        fields.add(new Field(name, JsonParser.BOOLEAN));
        return this;
    }

    public ObjectParser parseObject(String name, ObjectParser parser) {
        fields.add(new Field(name, parser));
        return this;
    }

    public ObjectParser parseArray(String name, ArrayParser parser) {
        fields.add(new Field(name, parser));
        return this;
    }

    private <T> T get(int type, T defaultValue) {
        Field field = fields.get(index);
        if (field.parser instanceof FieldParser) {
            index++;
            return ((FieldParser) field.parser).get(type, defaultValue);
        }
        return defaultValue;
    }

    public String getString(String defaultValue) {
        return get(STRING, defaultValue);
    }

    public Integer getInt(Integer defaultValue) {
        return get(INT, defaultValue);
    }

    public Long getLong(Long defaultValue) {
        return get(LONG, defaultValue);
    }

    public Double getDouble(Double defaultValue) {
        return get(LONG, defaultValue);
    }

    public Boolean getBoolean(Boolean defaultValue) {
        return get(BOOLEAN, defaultValue);
    }

    public ObjectParser getObject() {
        Field field = fields.get(index);
        if (field.parser instanceof ObjectParser) {
            index++;
            return (ObjectParser) field.parser;
        }
        return null;
    }

    public ArrayParser getArray() {
        Field field = fields.get(index);
        if (field.parser instanceof ArrayParser) {
            index++;
            return (ArrayParser) field.parser;
        }
        return null;
    }

    @Override
    public JsonParser copyStructure() {
        ObjectParser copy = new ObjectParser();
        for (Field f : fields) {
            copy.fields.add(new Field(f.name, f.parser.copyStructure()));
        }
        return copy;
    }

    private class Field {
        String name;
        JsonParser parser;

        public Field(String name, JsonParser parser) {
            this.name = name;
            this.parser = parser;
        }

        public Field(String name, int type) {
            this.name = name;
            this.parser = new FieldParser(type);
        }
    }

}
