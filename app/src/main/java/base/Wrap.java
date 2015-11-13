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
import util.BackgroundAction;
import util.UIAction;
import util.ListenerHolder;
import util.LongTask;
import util.Parameter;
import util.ParameterBundle;

/**
 * Base class for all interactions with particular social network.
 * We need to fill field "keys" in authorizer to send requests through this class.
 *
 * @param <K> is a proper(for particular social network) KeyKeeper
 */

public abstract class Wrap<K extends KeyKeeper> {
    private final String TAG = "WRAP_TAG";

    protected K keys;

    /**
     * @return URL of posting server
     */
    protected abstract String getInitialPostURL();

    /**
     * Get parameters for post. Must be done quickly, without publishing progress
     * @param post Post, that should be published
     * @return Bundle of parameters
     */
    protected abstract ParameterBundle getInitialPostParams(Post post);

    /**
     * Make BackgroundAction, that can publish it's progress
     * @param publish action, that can publish current progress to UI
     */
    protected abstract BackgroundAction<Parameter> uploadImage(BackgroundAction publish);

    /**
     * Parse response from server and save PostID to Post object
     * @param post
     * @param response URL-response from server
     */
    protected abstract void parseResponse(Post post, String response);

    public K getKeys() {
        return keys;
    }

    public Wrap() {
    }

    public Wrap(K keys) {
        this.keys = keys;
    }

    /**
     * Close instance of Closeable without throwing exception
     */
    protected void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception while closing: " + e.getMessage());
            }
        }
    }

    /**
     * Make BackgroundAction, that posts Post to network
     * @param publish Action for publishing result
     */
    public BackgroundAction post(final BackgroundAction publish) {
        return new BackgroundAction() {
            @Override
            public Object execute(Object... params) {
                Post post = (Post) params[0];
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

                    ParameterBundle parameters = getInitialPostParams(post);

                    for (Attachment attachment : post.getAttachments()) {
                        if (attachment instanceof Image) {

                            Parameter image = uploadImage(new BackgroundAction() {
                                @Override
                                public Object execute(Object... params) {
                                    // ToDo: make good progress
                                    publish.execute(params);
                                    return null;
                                }
                            }).execute();

                            if (image != null)
                                parameters.addParameter(image);
                        }
                    }

                    String urlParams = parameters.toString();

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
                    return null;
                } catch (IOException ioe) {
                    Log.e(TAG, "IOException");
                    return null;
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    closeQuietly(wr);
                    closeQuietly(in);
                }
                parseResponse(post, response.toString());
                return null;
            }
        };
    }

    /**
     * Makes LongTask, that uploads post to many network one by one
     * @param wraps Wraps of social networks
     */
    public static LongTask<Object, Integer> makePostUploader(final Wrap... wraps) {
        return new LongTask<Object, Integer>() {
            @Override
            protected UIAction doInBackground(Object... params) {
                Post post = (Post) params[0];
                int k = 0;
                for (Wrap w : wraps) {
                    try {
                        k++;
                        w.post(new BackgroundAction() {
                            @Override
                            public Object execute(Object... params) {
                                publishProgress((Integer)params[0]);
                                return null;
                            }
                        }).execute(post);
                        publishProgress(k, params.length);
                    } catch (NullPointerException e) {
                        Log.e("WRAP", "NullPtrException");
                    } catch (Exception e) {
                        return new UIAction() {
                            @Override
                            public void execute(Activity activity, Object... params) {
                                ListenerHolder.getListener(0).onFail(activity, "Fail");
                            }
                        };
                    }
                }
                return new UIAction() {
                    @Override
                    public void execute(Activity activity, Object... params) {
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
