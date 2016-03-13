package ly.loud.loudly.util.parsers.json;

import android.util.JsonReader;

import java.io.IOException;

/**
 * Interface for JSON parsers
 */
public interface JsonParser {
    int INT = 0;
    int LONG = 1;
    int DOUBLE = 2;
    int STRING = 3;
    int BOOLEAN = 4;

    void parse(JsonReader reader) throws IOException;
    JsonParser copyStructure();
}
