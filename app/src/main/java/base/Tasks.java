package base;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ly.loud.loudly.Loudly;
import util.AttachableTask;
import util.BackgroundAction;
import util.FileWrap;
import util.ListenerHolder;
import util.LongTask;
import util.Network;
import util.UIAction;

/**
 * Class made for storing different asynchronous tasks
 */
public class Tasks {
    private static final String KEYS_FILE = "keys";

    /**
     * Makes LongTask, that uploads post to many network one by one
     *
     * @param wraps Wraps of social networks
     */
    public static LongTask<Object, Integer> makePostUploader(final UIAction onProgressUpdate, final Wrappable... wraps) {
        return new LongTask<Object, Integer>() {
            @Override
            protected UIAction doInBackground(Object... params) {
                Post post = (Post) params[0];
                int k = 0;
                for (Wrappable w : wraps) {
                    try {
                        k++;
                        Interactions.post(w, post, new BackgroundAction() {
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
                            public void execute(Context context, Object... params) {
                                ListenerHolder.getListener(0).onFail(context, "Fail");
                            }
                        };
                    }
                }
                return new UIAction() {
                    @Override
                    public void execute(Context context, Object... params) {
                        ListenerHolder.getListener(0).onSuccess(context, "Success");
                    }
                };
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                onProgressUpdate.execute(context, values);
            }
        };
    }

    /**
     * Task for saving KeyKeepers to file
     */
    public abstract static class saveKeysTask extends AttachableTask<Object, Void, Integer> {
        public saveKeysTask(Context context) {
            super(context);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            FileWrap file = null;
            OutputStream output = null;
            try {

                output = context.openFileOutput(KEYS_FILE, Context.MODE_PRIVATE);

                file = new FileWrap(output);
                for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                    KeyKeeper keyKeeper = Loudly.getContext().getKeyKeeper(i);
                    if (keyKeeper != null) {
                        file.writeString(Integer.toString(i));
                        keyKeeper.writeToFile(file);
                    }
                }
            } catch (FileNotFoundException e) {
                return -1;
            } finally {
                if (file != null) {
                    file.close();
                }
                Network.closeQuietly(output);
            }
            return 0;
        }
    }

    /**
     * Task for loading KeyKeepers from file
     */
    public abstract static class loadKeysTask extends AttachableTask<Object, Void, Integer> {
        public loadKeysTask(Activity activity) {
            super(activity);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            InputStream input = null;
            FileWrap file = null;
            try {
                input = context.openFileInput(KEYS_FILE);
                file = new FileWrap(input);
                while (true) {
                    String networkS = file.readString();
                    if (networkS == null) {
                        break;
                    }
                    int network = Integer.parseInt(networkS);
                    KeyKeeper k = KeyKeeper.makeKeyKeeper(network);
                    if (k != null) {
                        k.readFromFile(file);
                    } else {
                        throw new IOException("Fail :(");
                    }
                    Loudly.getContext().setKeyKeeper(network, k);
                }
            } catch (IOException e) {
                // Something terrible
                return -1;
            } finally {
                if (file != null) {
                    file.close();
                }
                Network.closeQuietly(input);
            }
            return 0;
        }
    }
}
