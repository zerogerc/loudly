package util;

import java.io.IOException;

public class InvalidTokenException extends IOException {
    public InvalidTokenException() {
        super("Invalid token");
    }
}
