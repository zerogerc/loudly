package base;


import android.content.Context;

import java.io.IOException;

import ly.loud.loudly.Loudly;
import util.AttachableTask;
import util.BackgroundAction;
import util.LongTask;
import util.ResultListener;
import util.UIAction;
import util.database.DatabaseActions;
import util.database.DatabaseException;

/**
 * Class made for storing different asynchronous tasks
 */
public class Tasks {
    private static final String KEYS_FILE = "keys";
    private static final String POSTS_FILE = "posts";

    /**
     * Makes LongTask, that uploads post to many network one by one
     *
     * @param wraps Wraps of social networks
     */
    public static LongTask<Object, Integer> makePostUploader(
            final UIAction onProgressUpdate,
            final ResultListener listener,
            final Wrap... wraps) {

        return new LongTask<Object, Integer>() {
            @Override
            protected UIAction doInBackground(Object... params) {
                final Post post = (Post) params[0];
                int k = 0;
                for (Wrap w : wraps) {
                    try {
                        k++;
                        Interactions.post(w, post, new BackgroundAction() {
                            @Override
                            public void execute(Object... params) {
                                publishProgress((Integer) params[0]);
                            }
                        });
                        publishProgress(k, params.length);
                    } catch (Exception e) {
                        return new UIAction() {
                            @Override
                            public void execute(Context context, Object... params) {
                                listener.onFail(context, "Fail");
                            }
                        };
                    }
                }

                try {
                    DatabaseActions.savePost(post);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                    return new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            listener.onFail(context, "Failed due to database");
                        }
                    };
                }

                // TODO: I DON'T LIKE IT
                return new UIAction() {
                    @Override
                    public void execute(Context context, Object... params) {
                        Loudly.getContext().addPost(post);
                        listener.onSuccess(context, post);
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

    public static LongTask<Object, Integer> makePostInfoGetter(
            final UIAction onProgressUpdate,
            final ResultListener listener,
            final Wrap... wraps) {

        return new LongTask<Object, Integer>() {
            @Override
            protected UIAction doInBackground(Object... params) {
                int k = 0;
                final Post post = (Post) params[0];
                try {
                    for (Wrap w : wraps) {
                        k++;
                        Interactions.getInfo(w, post);
                        publishProgress(k);
                    }
                } catch (IOException e) {
                    return new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            listener.onFail(context, "IOException");
                        }
                    };
                }
                return new UIAction() {
                    @Override
                    public void execute(Context context, Object... params) {
                        listener.onSuccess(context, post);
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
            try {
                DatabaseActions.saveKeys();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
    }

    /**
     * Task for loading KeyKeepers from file
     */
    public abstract static class loadKeysTask extends AttachableTask<Object, Void, Integer> {
        public loadKeysTask(Context context) {
            super(context);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            try {
                DatabaseActions.loadKeys();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
    }

    public static abstract class savePostsTask extends AttachableTask<Post, Void, Integer> {
        public savePostsTask(Context context) {
            super(context);
        }

        @Override
        protected Integer doInBackground(Post... posts) {
            try {
                for (Post p : posts) {
                    DatabaseActions.savePost(p);
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
    }

    public static abstract class loadPostsTask extends AttachableTask<Object, Void, Integer> {
        public loadPostsTask(Context context) {
            super(context);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            try {
                DatabaseActions.loadPosts();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }
    }
}
