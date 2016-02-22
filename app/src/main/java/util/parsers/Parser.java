package util.parsers;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for parsers of HTTP responses
 * @param <T> result of parsing
 */
public interface Parser<T> {
    T parse(InputStream stream) throws IOException;
}
