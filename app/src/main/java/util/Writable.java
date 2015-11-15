package util;

import java.io.IOException;

/**
 * Interface for classes, that can be written to file and restored from it
 */
public interface Writable {
    void writeToFile(FileWrap file);
    void readFromFile(FileWrap file) throws IOException;
}
