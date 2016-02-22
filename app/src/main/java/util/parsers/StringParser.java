package util.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import util.Utils;

/**
 * Default parser. Transform InputStream to String
 */
public class StringParser implements Parser<String> {
    @Override
    public String parse(InputStream stream) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append('\n').append(line);
            }
            return response.toString();
        } finally {
            Utils.closeQuietly(reader);
        }
    }
}
