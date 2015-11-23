package base;


import android.content.Intent;

import java.io.IOException;

import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.BroadcastSendingTask;
import util.database.DatabaseActions;
import util.database.DatabaseException;

/**
 * Class made for storing different asynchronous tasks
 */
public class Tasks {
    public static abstract class SocialNetworkTask extends BroadcastSendingTask<Post> {
        Wrap[] wraps;

        public SocialNetworkTask(Wrap... wraps) {
            this.wraps = wraps;
        }
    }

    public static class PostUploader extends SocialNetworkTask {
        public PostUploader(Wrap... wraps) {
            super(wraps);
        }

        @Override
        protected Intent doInBackground(Post... params) {
            final Post post = params[0];
            int k = 0;
            try {
                DatabaseActions.savePost(post);
            } catch (DatabaseException e) {
                return makeError(Loudly.POST_UPLOAD_FINISHED, -1, e.getMessage());
            }

            Loudly.getContext().addPost(post);

            publishProgress(makeMessage(Loudly.POST_UPLOAD_STARTED, post.getLocalId()));

            try {
                for (Wrap w : wraps) {
                    k++;
                    Interactions.post(w, post, new BackgroundAction() {
                        @Override
                        public void execute(Object... params) {
                            // Do sth here, plz
                        }
                    });

                    Intent message = makeMessage(Loudly.POST_UPLOAD_PROGRESS, post.getLocalId());
                    message.putExtra(BroadcastSendingTask.PROGRESS_FIELD, k);

                    DatabaseActions.updatePostLinks(w.networkID(), post);
                    publishProgress(message);
                }

            } catch (Exception e) {
                return makeError(Loudly.POST_UPLOAD_FINISHED, post.getLocalId(), e.getMessage());
            }


            return makeSuccess(Loudly.POST_UPLOAD_FINISHED, post.getLocalId());
        }
    }

    public static class InfoGetter extends SocialNetworkTask {
        public InfoGetter(Wrap... wraps) {
            super(wraps);
        }

        @Override
        protected Intent doInBackground(Post... posts) {
            for (Post post : posts) {
                int k = 0;
                try {
                    for (Wrap w : wraps) {
                        k++;
                        Interactions.getInfo(w, post);
                        Intent message = makeMessage(Loudly.POST_GET_INFO_PROGRESS, post.getLocalId());
                        message.putExtra(BroadcastSendingTask.PROGRESS_FIELD, k);
                        publishProgress(message);
                    }
                } catch (IOException e) {
                    publishProgress(makeError(Loudly.POST_GET_INFO_FINISHED, post.getLocalId(), e.getMessage()));
                }
            }

            return makeSuccess(Loudly.POST_UPLOAD_FINISHED, -1);
        }
    }


    /*
    // Experimental features

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
    */

    /**
     * Task for saving KeyKeepers to file
     */
    public static class SaveKeysTask extends BroadcastSendingTask<Object> {
        @Override
        protected Intent doInBackground(Object... params) {
            try {
                DatabaseActions.saveKeys();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Loudly.SAVED_KEYS, -1, e.getMessage());
            }
            return makeSuccess(Loudly.SAVED_KEYS, -1);
        }
    }

    /**
     * Task for loading KeyKeepers from file
     */
    public static class LoadKeysTask extends BroadcastSendingTask<Object> {
        @Override
        protected Intent doInBackground(Object... params) {
            try {
                DatabaseActions.loadKeys();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Loudly.LOADED_KEYS, -1, e.getMessage());
            }
            return makeSuccess(Loudly.LOADED_KEYS, -1);
        }
    }

    public static class LoadPostsTask extends BroadcastSendingTask<Object> {
        @Override
        protected Intent doInBackground(Object... params) {
            try {
                DatabaseActions.loadPosts();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Loudly.LOADED_POSTS, -1, e.getMessage());
            }
            Loudly.getContext().postsLoaded = true; // TODO: 11/23/2015 Remove crutch
            return makeSuccess(Loudly.LOADED_POSTS, -1);
        }
    }
}
