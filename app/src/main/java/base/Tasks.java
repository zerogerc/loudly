package base;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.LinkedList;

import ly.loud.loudly.Loudly;
import util.AttachableTask;
import util.BackgroundAction;
import util.ResultListener;
import util.TaskWithProgress;
import util.UIAction;
import util.database.DatabaseActions;
import util.database.DatabaseException;

/**
 * Class made for storing different asynchronous tasks
 */
public class Tasks {

    public static abstract class SocialNetworkTask extends AsyncTask<Post, Integer, Intent> {
        public static String PROGRESS_NAME = "progress";
        String progressAction;
        Wrap[] wraps;

        public SocialNetworkTask(String progressAction, Wrap... wraps) {
            this.progressAction = progressAction;
            this.wraps = wraps;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            LocalBroadcastManager.getInstance(Loudly.getContext()).sendBroadcast(intent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Intent message = new Intent(progressAction);
            message.putExtra(PROGRESS_NAME, values[0]);
            LocalBroadcastManager.getInstance(Loudly.getContext()).sendBroadcast(message);
        }
    }

    /**
     * TaskWithProgress for uploading posts into networks
     */

//    public static class PostUploader1 extends TaskWithProgress<Post, Integer> {
//        public PostUploader(UIAction onProgressUpdate, ResultListener onFinish, Wrap... wraps) {
//            super(onProgressUpdate, onFinish, wraps);
//        }
//
//        @Override
//        protected UIAction doInBackground(Post... params) {
//            final Post post = params[0];
//            int k = 0;
//            for (Wrap w : wraps) {
//                try {
//                    k++;
//                    Interactions.post(w, post, new BackgroundAction() {
//                        @Override
//                        public void execute(Object... params) {
//                            publishProgress((Integer) params[0]);
//                        }
//                    });
//                    publishProgress(k, params.length);
//                } catch (Exception e) {
//                    return new UIAction() {
//                        @Override
//                        public void execute(Context context, Object... params) {
//                            onFinish.onFail(context, "Fail");
//                        }
//                    };
//                }
//            }
//
//            try {
//                DatabaseActions.savePost(post);
//            } catch (DatabaseException e) {
//                e.printStackTrace();
//                return new UIAction() {
//                    @Override
//                    public void execute(Context context, Object... params) {
//                        onFinish.onFail(context, "Failed due to database");
//                    }
//                };
//            }
//
//            // TODO: I DON'T LIKE IT
//            return new UIAction() {
//                @Override
//                public void execute(Context context, Object... params) {
//                    Loudly.getContext().addPost(post);
//                    onFinish.onSuccess(context, post);
//                }
//            };
//        }
//    }

    public static class PostUploader extends SocialNetworkTask {
        public PostUploader(String progressAction, Wrap... wraps) {
            super(progressAction, wraps);
        }

        @Override
        protected Intent doInBackground(Post... params) {
            final Post post = params[0];
            int k = 0;
            try {
                for (Wrap w : wraps) {
                    k++;
                    Interactions.post(w, post, new BackgroundAction() {
                        @Override
                        public void execute(Object... params) {
                            publishProgress((Integer) params[0]);
                        }
                    });
                    publishProgress(k, params.length);
                }
                DatabaseActions.savePost(post);
            } catch (Exception e) {
                Intent message = new Intent(Loudly.POST_UPLOAD_FINISHED);
                message.putExtra("success", false);
                message.putExtra("error", e.getMessage());
                return message;
            }

            Loudly.getContext().addPost(post);
            Intent message = new Intent(Loudly.POST_UPLOAD_FINISHED);
            message.putExtra("success", true);
            return message;
        }
    }

    public static class InfoGetter extends TaskWithProgress<Post, Integer> {
        public InfoGetter(UIAction onProgressUpdate, ResultListener onFinish, Wrap... wraps) {
            super(onProgressUpdate, onFinish, wraps);
        }

        @Override
        protected UIAction doInBackground(Post... params) {
            int k = 0;
            final Post post = params[0];
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
                        onFinish.onFail(context, "IOException");
                    }
                };
            }
            return new UIAction() {
                @Override
                public void execute(Context context, Object... params) {
                    onFinish.onSuccess(context, post);
                }
            };
        }
    }

    public static class PostDeleter extends TaskWithProgress<Post, Integer> {
        public PostDeleter(UIAction onProgressUpdate, ResultListener onFinish, Wrap... wraps) {
            super(onProgressUpdate, onFinish, wraps);
        }

        @Override
        protected UIAction doInBackground(Post... params) {
            int k = 0;
            final Post post = params[0];
            try {
                for (Wrap w : Loudly.getContext().getWraps()) {
                    k++;
                    Interactions.deletePost(w, post);
                    publishProgress(k);
                }
            } catch (IOException e) {
                return new UIAction() {
                    @Override
                    public void execute(Context context, Object... params) {
                        onFinish.onFail(context, "IOException");
                    }
                };
            }
            boolean dead = true;
            for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                if (post.getLink(i) != null) {
                    dead = false;
                    break;
                }
            }
            if (dead) {
                try {
                    DatabaseActions.deletePost(post);
                } catch (final DatabaseException e) {
                    return new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            onFinish.onFail(context, "Database error: " + e.getMessage());
                        }
                    };
                }
                Loudly.getContext().getPosts().remove(post);
            }
            return new UIAction() {
                @Override
                public void execute(Context context, Object... params) {
                    onFinish.onSuccess(context, post);
                }
            };
        }
    }


    public static class PostsLoader extends TaskWithProgress<Long, Integer> {
        public PostsLoader(UIAction onProgressUpdate, ResultListener onFinish, Wrap... wraps) {
            super(onProgressUpdate, onFinish, wraps);
        }

        @Override
        protected UIAction doInBackground(Long... params) {
            long since = params[0];
            long before = params[1];
            int k = 0;
            try {
                for (Wrap w : wraps) {
                    k++;
                    LinkedList<Post> posts = Interactions.loadPosts(w, since, before);
                    // ToDo: Merge with posts in Loudly
                    publishProgress(k);
                }
            } catch (final IOException e) {
                return new UIAction() {
                    @Override
                    public void execute(Context context, Object... params) {
                        onFinish.onFail(context, e.getMessage());
                    }
                };
            }
            return new UIAction() {
                @Override
                public void execute(Context context, Object... params) {
                    onFinish.onSuccess(context, params);
                }
            };
        }
    }


    /**
     * Task for saving KeyKeepers to file
     */
    public static class SaveKeysTask extends AttachableTask<Object, Void, Integer> {
        public SaveKeysTask(Context context) {
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

        @Override
        public void ExecuteInUI(Context context, Integer integer) {
        }
    }

    /**
     * Task for loading KeyKeepers from file
     */
    public abstract static class LoadKeysTask extends AttachableTask<Object, Void, Integer> {
        public LoadKeysTask(Context context) {
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

    public static abstract class SavePostsTask extends AttachableTask<Post, Void, Integer> {
        public SavePostsTask(Context context) {
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

    public static abstract class LoadPostsTask extends AttachableTask<Object, Void, Integer> {
        public LoadPostsTask(Context context) {
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
