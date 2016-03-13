package ly.loud.loudly.base.attachments;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public interface LocalFile {
    Uri getUri();
    String getMIMEType();
    InputStream getContent() throws IOException;
    long getFileSize() throws IOException;
}
