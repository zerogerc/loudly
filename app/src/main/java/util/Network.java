package util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class that contains static methods for work with network
 */

public class Network {
    private static final String TAG = "REQUEST";
    private static final String CRLF = "\r\n";

    /**
     * Make GET-request
     * @param query request to server
     * @return response from server
     * @throws IOException if any IO-error occurs
     */
    public static String makeGetRequest(Query query) throws IOException {
        HttpURLConnection conn = null;
        String response = null;
        try {
            conn = (HttpURLConnection) new URL(query.getServerURL()).openConnection();
            conn.setRequestMethod("GET");
            response = getResponse(conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }

    /**
     * Performs POST-request
     * @param query request to server
     * @param onProgressUpdate is called during making request
     * @return response from server
     * @throws IOException if any IO-error occurs
     */
    public static String makePostRequest(Query query,
                                         BackgroundAction onProgressUpdate) throws IOException {
        return makePostRequest(query, onProgressUpdate, null, null);
    }

    /**
     * Performs POST-request
     * @param query request to server
     * @param onProgressUpdate is called during making request
     * @param fileParamName name, representing uploaded file in POST-request
     * @param file file, that must be uploaded
     * @return response from server
     * @throws IOException if any IO-error occurs
     */

    public static String makePostRequest(Query query,
                                         BackgroundAction onProgressUpdate,
                                         String fileParamName, File file) throws IOException {

        String boundary = "===" + System.currentTimeMillis() + "===";


        HttpURLConnection conn = null;
        OutputStream outputStream = null;
        PrintWriter pw = null;

        String response = null;
        try {
            URL reqUrl = new URL(query.getServerURL());

            // Declare POST request
            conn = (HttpURLConnection) reqUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            String requestFormat = (file == null) ? "application/x-www-form-urlencoded" : "multipart/form-data";
            conn.setRequestProperty("Content-type", requestFormat + "; boundary=" + boundary);

            outputStream = conn.getOutputStream();
            pw = new PrintWriter(outputStream);

            if (file == null) {
                // If we should send only text
                String request = query.getParameters().toString(); // Get parameters in proper format
                pw.append(request);
                pw.flush();
            } else {
                // If we should upload file
                // Add parameters in appropriate format
                for (Parameter p : query.getParameters().asList()) {
                    paramToPOST(pw, p, boundary);
                }

                // Append file as sequence of bytes
                // Appending file's info:
                String filename = file.getName();
                pw.append("--").append(boundary).append(CRLF);
                pw.append("Content-Disposition: file; name=\"")
                        .append(fileParamName)
                        .append("\"; filename=\"")
                        .append(filename)
                        .append("\"")
                        .append(CRLF);
                pw.append("Content-type: ")
                        .append(URLConnection.guessContentTypeFromName(filename))
                        .append(CRLF);
                pw.append("Content-Transfer-Encoding: binary").append(CRLF);
                pw.append(CRLF);
                pw.flush();

                // Appending file's data
                FileInputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    // ToDo: And here publish
                }
                outputStream.flush();
                inputStream.close();
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
            closeQuietly(outputStream);
        }
        return response;
    }

    /**
     * Get response from server after sending request
     * @param conn established connection to server
     * @return response from server
     * @throws IOException if getting response fails
     */
    private static String getResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append('\n').append(line);
                }
            } finally {
                closeQuietly(reader);
            }
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return response.toString();
    }

    /**
     * Writes text parameter as form-value in POST request
     */
    private static void paramToPOST(PrintWriter pw, Parameter parameter, String boundary) {
        pw.append("--").append(boundary).append(CRLF);
        pw.append("Content-Disposition: form-data; name=\"")
                .append(parameter.name)
                .append("\"")
                .append(CRLF);
        pw.append(CRLF);
        pw.append(parameter.value).append(CRLF);
        pw.flush();
    }

    /**
     * Close instance of Closeable without throwing exception
     */
    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception while closing: " + e.getMessage());
            }
        }
    }
}
