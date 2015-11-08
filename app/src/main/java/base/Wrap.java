package base;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class Wrap<K extends KeyKeeper> {
    private final String TAG = "WRAP_TAG";

    protected K keys;

    public abstract String getInitialPostURL();
    public abstract String getPostParameters(Post post);
    public abstract void processPostResponse(String response);

    public K getKeys() {
        return keys;
    }

    public Wrap() {}

    public Wrap(K keys) {
        this.keys = keys;
    }

    public AsyncTask<Object, Void, String> post(final Post post) {
        return new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                URL reqUrl;
                HttpURLConnection con;
                StringBuilder response = new StringBuilder();

                try {
                    reqUrl = new URL(getInitialPostURL());

                    con = (HttpURLConnection) reqUrl.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    String UrlParams = getPostParameters(post);

                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(UrlParams);
                    wr.flush();
                    wr.close();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Log.d(TAG, response.toString());
                } catch (MalformedURLException me) {
                    Log.e(TAG, "MalformedException");
                    return null;
                } catch (IOException ioe) {
                    Log.e(TAG, "IOException");
                    return null;
                }
                return response.toString();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                processPostResponse(s);
            }
        };
    }
}
