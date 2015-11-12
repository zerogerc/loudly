package base;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import base.attachments.Attachment;
import base.attachments.Image;
import util.Action;
import util.ListenerHolder;
import util.LongTask;
import util.Parameter;
import util.ParameterBundle;

/**
 * base class for all interactions with particular social network
 * we need to fill field "keys" in authorizer to send requests through this class
 *
 * @param <K> - is a proper(for particular social network) KeyKeeper
 */

public abstract class Wrap<K extends KeyKeeper> {
    private final String TAG = "WRAP_TAG";

    protected K keys;

    protected abstract String getInitialPostURL();

    protected abstract ParameterBundle getInitialPostParams(Post post);

    protected abstract Parameter uploadImage(Image im) throws IOException;

    protected abstract void parseResponse(String response);

    public K getKeys() {
        return keys;
    }

    public Wrap() {
    }

    public Wrap(K keys) {
        this.keys = keys;
    }

    protected void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception while closing: " + e.getMessage());
            }
        }
    }

    public void post(final Post post) throws IOException {
        URL reqUrl;
        HttpURLConnection con = null;
        DataOutputStream wr = null;
        BufferedReader in = null;

        StringBuilder response = new StringBuilder();
        try {
            reqUrl = new URL(getInitialPostURL());

            con = (HttpURLConnection) reqUrl.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            ParameterBundle params = getInitialPostParams(post);

            for (Attachment attachment : post.getAttachments()) {
                if (attachment instanceof Image) {
                    params.addParameter(uploadImage((Image) attachment));
                }
            }

            String urlParams = params.toString();

            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParams);
            wr.flush();

            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            Log.d(TAG, response.toString());
        } catch (MalformedURLException me) {
            Log.e(TAG, "MalformedException");
            throw new IOException(me);
        } catch (IOException ioe) {
            Log.e(TAG, "IOException");
            throw new IOException(ioe);
        } finally {
            if (con != null) {
                con.disconnect();
            }
            closeQuietly(wr);
            closeQuietly(in);
        }
        parseResponse(response.toString());
    }

    public static LongTask<Object, Integer> makePostUploader(final Post post, final Wrap... wraps) {
        return new LongTask<Object, Integer>() {
            @Override
            protected Action doInBackground(Object... params) {
                int k = 0;
                for (Wrap w : wraps) {
                    try {
                        w.post(post);
                        k++;
                    } catch (IOException e) {
                        Log.e("WRAP", "Failed to upload post");
                        // Do something
                    } catch (NullPointerException e) {
                        Log.e("WRAP", "NullPtrException");
                    } catch (Exception e) {
                        return new Action() {
                            @Override
                            public void execute(Activity activity) {
                                ListenerHolder.getListener(0).onFail(activity, "Fail");
                            }
                        };
                    }
                    publishProgress(k, params.length);
                }
                return new Action() {
                    @Override
                    public void execute(Activity activity) {
                        ListenerHolder.getListener(0).onSuccess(activity, "Success");
                    }
                };
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                Log.e("PROGRESS", Integer.toString(values[0]));
                // Update here
            }
        };
    }
}
