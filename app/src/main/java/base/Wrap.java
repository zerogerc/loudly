package base;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;

import base.attachments.Attachment;
import base.attachments.Image;
import util.BackgroundAction;
import util.Counter;
import util.ListenerHolder;
import util.LongTask;
import util.Parameter;
import util.Query;
import util.Request;
import util.UIAction;

/**
 * Base class for all interactions with particular social network.
 * We need to fill field "keys" in authorizer to send requests through this class.
 *
 * @param <K> is a proper(for particular social network) KeyKeeper
 */

public abstract class Wrap<K extends KeyKeeper> {
    private final String TAG = "WRAP_TAG";

    protected K keys;

    protected abstract Query makePostQuery(Post post);

    /**
     * Make BackgroundAction, that can publish it's progress
     *
     * @param image
     * @param publish action, that can publish current progress to UI
     */
    protected abstract Parameter uploadImage(Image image, BackgroundAction publish);

    /**
     * Parse response from server and save PostID to Post object
     *
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
     * Post posts to one network
     *
     * @param post
     * @param publish Action for publishing result
     */
    public void post(Post post, final BackgroundAction publish) throws IOException {
        final Counter counter = post.getCounter();
        Integer k = 0;
        for (Attachment attachment : post.getAttachments()) {
            if (attachment instanceof Image) {
                k++;
                final double multiplier = k / (counter.getImageCount() + 1);
                uploadImage((Image) attachment, new BackgroundAction() {
                    @Override
                    public void execute(Object... params) {
                        // ToDo: do it later
                    }
                });
            }
        }
        String response = Request.makePOST(makePostQuery(post), new BackgroundAction() {
            @Override
            public void execute(Object... params) {
                publish.execute(params[0]);
            }
        });
        parseResponse(post, response);
    }

    /**
     * Makes LongTask, that uploads post to many network one by one
     *
     * @param wraps Wraps of social networks
     */
    public static LongTask<Object, Integer> makePostUploader(final UIAction onProgressUpdate, final Wrap... wraps) {
        return new LongTask<Object, Integer>() {
            @Override
            protected UIAction doInBackground(Object... params) {
                Post post = (Post) params[0];
                int k = 0;
                for (Wrap w : wraps) {
                    try {
                        k++;
                        w.post(post, new BackgroundAction() {
                            @Override
                            public void execute(Object... params) {
                                publishProgress((Integer) params[0]);
                            }
                        });
                        publishProgress(k, params.length);
                    } catch (IOException e) {
                        Log.e("TAG", "IOException");
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
                onProgressUpdate.execute(activity, values);
            }
        };
    }
}
