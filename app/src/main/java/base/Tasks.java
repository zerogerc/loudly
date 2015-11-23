package base;


import android.content.Intent;

import java.io.IOException;
import java.util.LinkedList;

import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.BroadcastSendingTask;
import util.database.DatabaseActions;
import util.database.DatabaseException;

/**
 * Class made for storing different asynchronous tasks
 */
public class Tasks {
    /**
     * Main class for tasks that use wraps
     */
    public static abstract class SocialNetworkTask extends BroadcastSendingTask<Post> {
        Wrap[] wraps;

        public SocialNetworkTask(Wrap... wraps) {
            this.wraps = wraps;
        }
    }

    /**
     * BroadcastReceivingTask for uploading post to network
     * <p/>
     * When post is added to DB, it sends Loudly.POST_UPLOAD_STARTED broadcast with
     * localId stored in field BroadcastSendingTask.ID_FIELD
     * <p/>
     * Then during upload it sends Loudly.POST_UPLOAD_PROGRESS broadcast
     * with localId of post in BroadcastSendingTask.ID_FIELD and progress stored in
     * BroadcastSendingTask.PROGRESS_FIELD
     * <p/>
     * When upload is successfully finished, it sends
     * Loudly.POST_UPLOAD_FINISHED with BroadcastSendingTask.SUCCESS_FIELD = true
     * <p/>
     * If an error occurred, it sends Loudly.POST_UPLOAD_FINISHED with
     * BroadcastSendingTask.SUCCESS_FIELD = false and BroadcastSendingTask.ERROR_FIELD = error
     * description
     */
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

    /**
     * BroadcastReceivingTask for getting post's likes, shares and comments number
     * <p/>
     * During getting info for every network, passed in constructor, it sends
     * Loudly.POST_GET_INFO_PROGRESS broadcast
     * with localId of post in BroadcastSendingTask.ID_FIELD and ID of network stored in
     * BroadcastSendingTask.NETWORK_FIELD
     * <p/>
     * When getting info is successfully finished, it sends
     * Loudly.POST_GET_INFO_FINISHED with BroadcastSendingTask.SUCCESS_FIELD = true
     * <p/>
     * If an error occurred, it sends Loudly.POST_GET_INFO_FINISHED with
     * BroadcastSendingTask.SUCCESS_FIELD = false and BroadcastSendingTask.ERROR_FIELD = error
     * description
     */
    public static class InfoGetter extends SocialNetworkTask {
        public InfoGetter(Wrap... wraps) {
            super(wraps);
        }

        @Override
        protected Intent doInBackground(Post... posts) {
            for (Post post : posts) {
                try {
                    for (Wrap w : wraps) {
                        Interactions.getInfo(w, post);
                        Intent message = makeMessage(Loudly.POST_GET_INFO_PROGRESS, post.getLocalId());
                        message.putExtra(BroadcastSendingTask.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    }
                } catch (IOException e) {
                    publishProgress(makeError(Loudly.POST_GET_INFO_FINISHED, post.getLocalId(), e.getMessage()));
                }
            }

            return makeSuccess(Loudly.POST_UPLOAD_FINISHED, -1);
        }
    }

    /**
     * BroadcastSendingTask for saving KeyKeepers to DB.
     * <p/>
     * After successful saving it sends Loudly.SAVED_KEYS broadcast with
     * BroadcastSendingTask.ID_FIELD = -1 and BroadcastSendingTask.SUCCESS_FIELD = true
     * <p/>
     * If an error occurred, it sends Loudly.SAVED_KEYS broadcast with
     * BroadcastSendingTask.SUCCESS_FIELD = false and BroadcastSendingTask.ERROR_FIELD = error
     * description
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
     * BroadcastSendingTask for loading KeyKeepers from DB.
     * <p/>
     * After successful loading it sends Loudly.LOADED_KEYS broadcast with
     * BroadcastSendingTask.ID_FIELD = -1 and BroadcastSendingTask.SUCCESS_FIELD = true
     * <p/>
     * If an error occurred, it sends Loudly.LOADED_KEYS broadcast with
     * BroadcastSendingTask.SUCCESS_FIELD = false and BroadcastSendingTask.ERROR_FIELD = error
     * description
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

    /**
     * BroadcastSendingTask for loading Posts from DB
     * <p/>
     * After successful loading it sends Loudly.LOADED_POSTS broadcast with
     * BroadcastSendingTask.ID_FIELD = -1 and BroadcastSendingTask.SUCCESS_FIELD = true
     * <p/>
     * If an error occurred, it sends Loudly.LOADED_POSTS broadcast with
     * BroadcastSendingTask.SUCCESS_FIELD = false and BroadcastSendingTask.ERROR_FIELD = error
     * description
     */

    public static class LoadPostsTask extends SocialNetworkTask {
        long beforeID, sinceTime;

        public LoadPostsTask(long beforeID, long sinceTime, Wrap... wraps) {
            super(wraps);
            this.beforeID = beforeID;
            this.sinceTime = sinceTime;
        }

        @Override
        protected Intent doInBackground(Post... params) {
            LinkedList<Post> resultList;
            //TODO: we could do it faster
            try {
                resultList = DatabaseActions.loadPosts(beforeID, sinceTime);
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Loudly.LOADED_POSTS, -1, e.getMessage());
            }

            for (Wrap w : wraps) {
                try {
                    LinkedList<Post> temp = new LinkedList<>();
                    LinkedList<Post> currentList = Interactions.loadPosts(w, beforeID, sinceTime);

                    while (resultList.size() != 0 || currentList.size() != 0) {
                        if (resultList.size() == 0) {
                            temp.add(currentList.removeFirst());
                            continue;
                        }
                        if (currentList.size() == 0) {
                            temp.add(resultList.removeFirst());
                            continue;
                        }
                        if (resultList.getFirst().getDate() <= currentList.getFirst().getDate()) {
                            temp.add(resultList.removeFirst());
                        } else {
                            temp.add(currentList.removeFirst());
                        }
                    }
                    resultList = temp;

                } catch (IOException e) {
                    publishProgress(makeError(Loudly.LOADED_POSTS, -1, e.getMessage()));
                }
            }

            Loudly.getContext().addPosts(resultList);
            return makeSuccess(Loudly.LOADED_POSTS, -1);
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


}
