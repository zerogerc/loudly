package util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Class, imitating work with text files, using IO streams.
 * Can be either Input or Output.
 * What's more, it's possible to make encoding and decoding files, using it
 */

public class FileWrap {
    PrintWriter writer;
    InputStreamReader reader;

    /**
     * Specify FileWrap for output
     * @param output output stream
     */
    public FileWrap(OutputStream output) {
        this.writer = new PrintWriter(output);
        reader = null;
    }

    /**
     * Specify FileWrap for input
     * @param input input stream
     */
    public FileWrap(InputStream input) {
        this.reader = new InputStreamReader(input);
        writer = null;
    }

    /**
     * Write string to the FileWrap. It should be open in Output mode
     */
    public void writeString(String s) {
        if (writer == null) {
            return;
        }
        // Here encode
        writer.print(s);
        writer.append('&');
    }

    /**
     * Read string from the FileWrap. It should be open in Input mode
     */
    public String readString() {
        StringBuilder builder = new StringBuilder();
        int c;
        try {
            do {
                c = reader.read();
                if (c != '&' && c != -1) {
                    builder.append((char)c);
                }
            } while (c != '&' && c != -1);
        } catch (IOException e) {
            return null;
        }
        if (builder.length() == 0) {
            return null;
        }
        // Here decode
        return builder.toString();
    }

    /**
     * Safely save data and close FileWrap
     */
    public void close() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
        if (reader != null) {
            Network.closeQuietly(reader);
        }
    }
}
