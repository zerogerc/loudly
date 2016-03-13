package ly.loud.loudly.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ly.loud.loudly.base.attachments.LocalFile;
import ly.loud.loudly.util.parsers.Parser;
import ly.loud.loudly.util.parsers.StringParser;


/**
 * Class that contains static methods for work with network
 */

public class Network {
    private static final String TAG = "REQUEST";
    private static final String CRLF = "\r\n";

    public static <T> T makeGetRequestAndParse(Query query, Parser<T> parser) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(query.toURL()).openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            return getResponse(conn, parser);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Make GET-request
     *
     * @param query request to server
     * @return response from server
     * @throws IOException if any IO-error occurs
     */
    public static String makeGetRequest(Query query) throws IOException {
        return makeGetRequestAndParse(query, new StringParser());
    }

    /**
     * Performs POST-request
     *
     * @param query request to server
     * @return response from server
     * @throws IOException if any IO-error occurs
     */
    public static String makePostRequest(Query query) throws IOException {
        return makePostRequest(query, null, null, null);
    }

    /**
     * Performs POST-request
     *
     * @param query            request to server
     * @param onProgressUpdate is called during making request
     * @param tag              - name of the photo field
     * @param file            - image that should be uploaded
     * @return response from server
     * @throws IOException if any IO-error occurs
     */

    public static String makePostRequest(Query query,
                                         BackgroundAction onProgressUpdate,
                                         String tag, LocalFile file) throws IOException {

        String boundary = "===" + System.currentTimeMillis() + "===";


        HttpURLConnection conn = null;
        OutputStream outputStream = null;
        PrintWriter pw = null;

        String response = "";
        try {
            URL reqUrl = new URL(query.getServerURL());

            // Declare POST request
            conn = (HttpURLConnection) reqUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            String requestFormat = (file == null) ? "application/x-www-form-urlencoded" : "multipart/form-data";
            conn.setRequestProperty("Content-type", requestFormat + "; boundary=" + boundary);

            outputStream = conn.getOutputStream();
            pw = new PrintWriter(outputStream);

            if (file == null) {
                // If we should send only text
                String request = query.parametersToString(); // Get parameters in proper format
                pw.append(request);
                pw.flush();
            } else {
                // If we should upload file
                // Add parameters in appropriate format
                for (Query.Parameter p : query.getParameters()) {
                    paramToPOST(pw, p, boundary);
                }

                String type = file.getMIMEType();

                // Append file as sequence of bytes
                // Appending file's info:
                pw.append("--").append(boundary).append(CRLF);
                pw.append("Content-Disposition: file; name=\"")
                        .append(tag)
                        .append("\"; filename=\"").append("source.").append(type.substring(type.indexOf('/') + 1))
                        .append("\"")
                        .append(CRLF);
                pw.append("Content-type: ")
                        .append(type)
                        .append(CRLF);
                pw.append("Content-Transfer-Encoding: binary").append(CRLF);
                pw.append(CRLF);
                pw.flush();

                // Appending file's data
                byte[] buffer = new byte[4096];
                int bytesRead = -1;

                long size = file.getFileSize();
                size = size == 0 ? Long.MAX_VALUE : size;
                long uploaded = 0;
                long progress = 0;
                InputStream content = null;
                try {
                    content = file.getContent();
                    while ((bytesRead = content.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        uploaded += bytesRead;
                        if (uploaded * 100 / size > progress + 2) {
                            progress = uploaded * 100 / size;
                            onProgressUpdate.execute((int) progress);
                        }
                        // ToDo: And here publish
                    }
                } finally {
                    Utils.closeQuietly(content);
                }
                outputStream.flush();
                pw.append(CRLF);
                pw.flush();

                // Finish POST-request
                pw.append(CRLF).flush();
                pw.append("--").append(boundary).append("--").append(CRLF);
                pw.flush();
            }

            // POST-request finished, let's get response
            response = getResponse(conn);

        } catch (MalformedURLException me) {
            Log.e(TAG, "MalformedException: " + me.getMessage());
            throw new IOException(me);
        } catch (IOException ioe) {
            Log.e(TAG, "IOException: " + ioe.getMessage());
            throw ioe;
        } finally {

            Log.e(TAG, response);
            if (pw != null) {
                pw.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
            Utils.closeQuietly(outputStream);
        }
        return response;
    }

    public static String makeDeleteRequest(Query query) throws IOException {
        HttpURLConnection conn = null;
        String response = null;
        try {
            conn = (HttpURLConnection) new URL(query.toURL()).openConnection();
            conn.setRequestMethod("DELETE");
            response = getResponse(conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }

    /**
     * Get response from server after sending request
     *
     * @param conn established connection to server
     * @return response from server
     * @throws IOException if getting response fails
     */
    private static String getResponse(HttpURLConnection conn) throws IOException {
        return getResponse(conn, new StringParser());
    }

    private static <T> T getResponse(HttpURLConnection conn, Parser<T> parser) throws IOException {
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            InputStream stream = null;
            try {
                stream = conn.getInputStream();
                return parser.parse(stream);
            } finally {
                Utils.closeQuietly(stream);
            }
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
    }

    /**
     * Writes text parameter as form-value in POST request
     */
    private static void paramToPOST(PrintWriter pw, Query.Parameter parameter, String boundary) {
        pw.append("--").append(boundary).append(CRLF);
        pw.append("Content-Disposition: form-data; name=\"")
                .append(parameter.name)
                .append("\"")
                .append(CRLF);
        pw.append(CRLF);
        pw.append(parameter.value).append(CRLF);
        pw.flush();
    }
}
