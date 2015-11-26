package base;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.IOException;
import java.util.LinkedList;

import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.BroadcastSendingTask;
import util.TimeInterval;
import util.Utils;
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
     * <p>
     * When post is added to DB, it sends Loudly.POST_UPLOAD_STARTED broadcast with
     * localId stored in field BroadcastSendingTask.ID_FIELD
     * <p>
     * Then during upload it sends Loudly.POST_UPLOAD_PROGRESS broadcast
     * with localId of post in BroadcastSendingTask.ID_FIELD and progress stored in
     * BroadcastSendingTask.PROGRESS_FIELD
     * <p>
     * When upload is successfully finished, it sends
     * Loudly.POST_UPLOAD_FINISHED with BroadcastSendingTask.SUCCESS_FIELD = true
     * <p>
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
     * <p>
     * During getting info for every network, passed in constructor, it sends
     * Loudly.POST_GET_INFO_PROGRESS broadcast
     * with localId of post in BroadcastSendingTask.ID_FIELD and ID of network stored in
     * BroadcastSendingTask.NETWORK_FIELD
     * <p>
     * When getting info is successfully finished, it sends
     * Loudly.POST_GET_INFO_FINISHED with BroadcastSendingTask.SUCCESS_FIELD = true
     * <p>
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
     * <p>
     * After successful saving it sends Loudly.SAVED_KEYS broadcast with
     * BroadcastSendingTask.ID_FIELD = -1 and BroadcastSendingTask.SUCCESS_FIELD = true
     * <p>
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
     * <p>
     * After successful loading it sends Loudly.LOADED_KEYS broadcast with
     * BroadcastSendingTask.ID_FIELD = -1 and BroadcastSendingTask.SUCCESS_FIELD = true
     * <p>
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

    public interface LoadCallback {
        /**
         * Is post stored in DB
         *
         * @param postID  Post
         * @param network network
         * @return Link to post, if it exists, or null, if not
         */
        Post findLoudlyPost(String postID, int network);

        void postLoaded(Post post);
    }

    /**
     * BroadcastSendingTask for loading Posts from DB
     * <p>
     * After loading from DB it sends Loudly.POST_LOAD_STARTED broadcast
     * <p>
     * After loading of every network it sends Loudly.POST_LOAD_PROGRESS broadcast with
     * ID of the network in BroadcastSendingTask.NETWORK_FIELD.
     * <p>
     * After successful loading it sends Loudly.POST_LOAD_FINISHED broadcast with
     * BroadcastSendingTask.ID_FIELD = -1 and BroadcastSendingTask.SUCCESS_FIELD = true
     * <p>
     * If an error occurred, it sends Loudly.POST_LOAD_FINISHED broadcast with
     * BroadcastSendingTask.SUCCESS_FIELD = false and BroadcastSendingTask.ERROR_FIELD = error
     * description
     */

    public static class LoadPostsTask extends SocialNetworkTask implements LoadCallback {
        private TimeInterval time;
        private LinkedList<Post> loudlyPosts;
        private LinkedList<Post> currentPosts;

        /**
         * Loads posts from every network
         *
         * @param time  Load posts with date int interval
         * @param wraps Networks, from which load posts
         */
        public LoadPostsTask(TimeInterval time,
                             Wrap... wraps) {
            super(wraps);
            this.time = time;
        }

        @Override
        public Post findLoudlyPost(String postID, int network) {
            for (Post lPost : loudlyPosts) {
                if (lPost.getLink(network) != null && lPost.getLink(network).equals(postID)) {
                    lPost.setExistence(network);
                    return lPost;
                }
            }
            return null;
        }

        @Override
        public void postLoaded(Post post) {
            currentPosts.add(post);
            // loadImage here
        }

        private LinkedList<Post> merge(LinkedList<Post> oldPosts, LinkedList<Post> newPosts) {

            LinkedList<Post> temp = new LinkedList<>();
            while (oldPosts.size() != 0 || newPosts.size() != 0) {
                if (oldPosts.size() == 0) {
                    temp.add(newPosts.removeFirst());
                    continue;
                }
                if (newPosts.size() == 0) {
                    temp.add(oldPosts.removeFirst());
                    continue;
                }
                if (oldPosts.getFirst().getDate() <= newPosts.getFirst().getDate()) {
                    temp.add(newPosts.removeFirst());
                } else {
                    temp.add(oldPosts.removeFirst());
                }
            }
            return temp;
        }

        @Override
        protected Intent doInBackground(Post... params) {
            LinkedList<Post> resultList = new LinkedList<>();
            //TODO: we could do it faster
            try {
                loudlyPosts = DatabaseActions.loadPosts(time);
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Loudly.POST_LOAD_FINISHED, -1, e.getMessage());
            }
            publishProgress(makeMessage(Loudly.POST_LOAD_STARTED, -1));

            boolean[] successfulLoading = new boolean[Networks.NETWORK_COUNT];

            for (Wrap w : wraps) {
                try {
                    currentPosts = new LinkedList<>();
                    Interactions.loadPosts(w, time, this);
                    resultList = merge(resultList, currentPosts);

                    Intent message = makeSuccess(Loudly.POST_LOAD_PROGRESS, -1);
                    message.putExtra(BroadcastSendingTask.NETWORK_FIELD, w.networkID());
                    publishProgress(message);

                    successfulLoading[w.networkID()] = true;
                } catch (IOException e) {
                    Intent message = makeError(Loudly.POST_LOAD_PROGRESS, -1, e.getMessage());
                    message.putExtra(BroadcastSendingTask.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                    successfulLoading[w.networkID()] = false;
                }
            }

            LinkedList<Post> cleaned = new LinkedList<>();

            for (Post p : loudlyPosts) {
                for (Wrap w : wraps) {
                    if (successfulLoading[w.networkID()] && !p.existsIn(w.networkID())) {
                        p.removeOutdatedLinks(w.networkID());
                    }
                }
                if (p.exists()) {
                    cleaned.add(p);
                }
//                if (!p.exists()) {
//                    loudlyPosts.remove(p);
//                    // remove from DB here
//                }
            }

            resultList = merge(resultList, cleaned);

            for (Post post : resultList) {
                if (post.getAttachments().size() != 0) {
                    Image image = (Image) post.getAttachments().get(0);
                    try {
                        Bitmap bitmap;
                        if (image.isLocal()) {
                            Uri uri = Uri.parse(image.getExtra());
                            bitmap = Utils.loadBitmap(uri,
                                    Utils.getDefaultScreenWidth(), Utils.getDefaultScreenWidth());
                        } else {
                            bitmap = Utils.downloadBitmap(image.getExtra(),
                                    Utils.getDefaultScreenWidth(), Utils.getDefaultScreenWidth());
                        }
                        image.setBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Loudly.getContext().addPosts(resultList);
            return makeSuccess(Loudly.POST_LOAD_FINISHED, -1);
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
