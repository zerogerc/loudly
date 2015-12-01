package base;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.IOException;
import java.util.LinkedList;

import base.attachments.Attachment;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.BroadcastSendingTask;
import util.Broadcasts;
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
     * BroadcastReceivingTask for uploading post to network.
     * It sends Broadcasts.POST_UPLOAD broadcast with parameters:
     * <p>
     * When post save to DB:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.Started </li>
     * <li>Broadcast.ID_FIELD = localID of the post</li>
     * </ol>
     * </p>
     * <p>
     * During uploading image to some network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.IMAGE</li>
     * <li>Broadcasts.ID_FIELD = localID of the post</li>
     * <li>Broadcasts.IMAGE_FIELD = localID of the image</li>
     * <li>Broadcasts.PROGRESS_FIELD = progress</li>
     * <li>Broadcasts.NETWORK_ID = id of the network</li>
     * </ol>
     * After uploading image to network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.IMAGE_FINISHED</li>
     * <li>Broadcast.ID_FIELD = localID of the post</li>
     * <li>Broadcasts.IMAGE_FIELD = localID of an image</li>
     * <li>Broadcasts.POST_ID = localID of the post</li>
     * <li>Broadcasts.NETWORK_ID = id of the network</li>
     * </ol>
     * </p>

     * After uploading post to some network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.PROGRESS</li>
     * <li>Broadcast.ID_FIELD = localID of the post</li>
     * <li>Broadcasts.NETWORK_ID = id of the network</li>
     * </ol>
     * <p>
     * When loading is successfully finished:
     * <ol>
     * <li>Broadcast.STATUS_FIELD = Broadcasts.FINISHED </li>
     * <li>Broadcast.ID_FIELD = localID of the post</li>
     * </ol>
     * </p>
     * <p>
     * If an error occurred:
     * <ol>
     * <li>Broadcasts.STATUS = Broadcasts.ERROR</li>
     * <li>Broadcasts.ERROR_KIND = kind of an error</li>
     * <li>Broadcasts.ERROR_FIELD = description of an error</li>
     * </ol>
     * </p>
     */

    public static class PostUploader extends SocialNetworkTask {
        public PostUploader(Wrap... wraps) {
            super(wraps);
        }

        @Override
        protected Intent doInBackground(Post... params) {
            final Post post = params[0];
            try {
                DatabaseActions.savePost(post);
            } catch (DatabaseException e) {
                return makeError(Broadcasts.POST_UPLOAD, Broadcasts.DATABASE_ERROR,
                        e.getMessage());
            }

            Loudly.getContext().addPost(post);
            if (post.getAttachments().isEmpty()) {
                post.setLoadedImage(true);
            } else {
                post.setLoadedImage(false);
            }

            publishProgress(makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.STARTED,
                    post.getLocalId()));

            try {
                for (Wrap w : wraps) {
                    final int networkID = w.networkID();
                    for (final Attachment attachment : post.getAttachments()) {
                        w.uploadImage((Image) attachment, new BackgroundAction() {
                            @Override
                            public void execute(Object... params) {
                                Intent message = makeMessage(Broadcasts.POST_UPLOAD,
                                        Broadcasts.IMAGE, post.getLocalId());
                                message.putExtra(Broadcasts.IMAGE_FIELD, attachment.getLocalID());
                                message.putExtra(Broadcasts.PROGRESS_FIELD, (int) params[0]);
                                message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                                publishProgress(message);
                            }
                        });
                        Intent message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.IMAGE_FINISHED,
                                post.getLocalId());
                        message.putExtra(Broadcasts.IMAGE_FIELD, attachment.getLocalID());
                        message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                    }
                    w.uploadPost(post);

                    Intent message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.PROGRESS,
                            post.getLocalId());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());

                    DatabaseActions.updatePostLinks(w.networkID(), post);
                    publishProgress(message);
                }

            } catch (IOException e) {
                return makeError(Broadcasts.POST_UPLOAD, Broadcasts.NETWORK_ERROR,
                        post.getLocalId(), e.getMessage());
            }
            post.setLoadedImage(true);
            return makeSuccess(Broadcasts.POST_UPLOAD, post.getLocalId());
        }
    }

    /**
     * BroadcastReceivingTask for getting post's likes, shares and comments number. It sends
     * Broadcasts.POST_GET_INFO with parameters:
     * <p>
     * When info got
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.PROGRESS </li>
     * <li>BroadcastSendingTask.NETWORK_FIELD = id of the network</li>
     * </ol>
     * </p>
     * <p>
     * When getting ifo is successfully finished:
     * <ol>
     * <li>Broadcast.STATUS_FIELD = Broadcasts.FINISHED </li>
     * </ol>
     * </p>
     * <p>
     * <p>
     * If an error occurred:
     * <ol>
     * <li>Broadcasts.STATUS = Broadcasts.ERROR</li>
     * <li>Broadcasts.ERROR_KIND = kind of an error</li>
     * <li>Broadcasts.ERROR_FIELD = description of an error</li>
     * </ol>
     * </p>
     */
    public static class InfoGetter extends SocialNetworkTask {
        public InfoGetter(Wrap... wraps) {
            super(wraps);
        }

        @Override
        protected Intent doInBackground(Post... posts) {
            try {
                for (Wrap w : wraps) {
                    w.getPostsInfo(posts);
                    Intent message = makeMessage(Broadcasts.POST_GET_INFO, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                }
            } catch (IOException e) {
                publishProgress(makeError(Broadcasts.POST_GET_INFO, Broadcasts.NETWORK_FIELD,
                        e.getMessage()));
            }

            return makeSuccess(Broadcasts.POST_GET_INFO);
        }
    }

    /**
     * BroadcastSendingTask for saving KeyKeepers to DB. It sends
     * Broadcasts.KEYS_SAVED with parameters:
     * <p>
     * When keys saved:
     * <ol>
     * <li>Broadcast.STATUS_FIELD = Broadcasts.FINISHED </li>
     * </ol>
     * </p>
     * <p>
     * <p>
     * If an error occurred:
     * <ol>
     * <li>Broadcasts.STATUS = Broadcasts.ERROR</li>
     * <li>Broadcasts.ERROR_KIND = DATABASE_ERROR</li>
     * <li>Broadcasts.ERROR_FIELD = description of an error</li>
     * </ol>
     * </p>
     */
    public static class SaveKeysTask extends BroadcastSendingTask<Object> {
        @Override
        protected Intent doInBackground(Object... params) {
            try {
                DatabaseActions.saveKeys();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Broadcasts.KEYS_SAVED, Broadcasts.DATABASE_ERROR,
                        e.getMessage());
            }
            return makeSuccess(Broadcasts.KEYS_SAVED);
        }
    }

    /**
     * BroadcastSendingTask for loading KeyKeepers from DB. It sends
     * Broadcasts.KEYS_LOADED with parameters:
     * <p>
     * When keys loaded:
     * <ol>
     * <li>Broadcast.STATUS_FIELD = Broadcasts.FINISHED </li>
     * </ol>
     * </p>
     * <p>
     * <p>
     * If an error occurred:
     * <ol>
     * <li>Broadcasts.STATUS = Broadcasts.ERROR</li>
     * <li>Broadcasts.ERROR_KIND = DATABASE_ERROR</li>
     * <li>Broadcasts.ERROR_FIELD = description of an error</li>
     * </ol>
     * </p>
     */
    public static class LoadKeysTask extends BroadcastSendingTask<Object> {
        @Override
        protected Intent doInBackground(Object... params) {
            try {
                DatabaseActions.loadKeys();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Broadcasts.KEYS_LOADED, Broadcasts.DATABASE_ERROR, e.getMessage());
            }
            return makeSuccess(Broadcasts.KEYS_LOADED);
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
     * BroadcastReceivingTask for loading posts from network.
     * It sends Broadcasts.POST_LOAD broadcast with parameters:
     * <p>
     * When posts are loaded from DB:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.Started </li>
     * </ol>
     * </p>
     * After loading posts from some network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.PROGRESS</li>
     * <li>Broadcasts.NETWORK_ID = id of the network</li>
     * </ol>
     * <p>
     * After loading posts from every network as text:
     * <ol>
     *     <li>Broadcasts.STATUS_FIELD = Broadcast.LOADED</li>
     * </ol>
     * <p>
     * During loading image form some network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.IMAGE</li>
     * <li>Broadcasts.ID_FIELD = localID of the post</li>
     * <li>Broadcasts.IMAGE_FIELD = localID of the image</li>
     * <li>Broadcasts.PROGRESS_FIELD = progress</li>
     * </ol>
     * After loading image from network of DB:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.IMAGE_FINISHED</li>
     * <li>Broadcasts.IMAGE_FIELD = localID of an image</li>
     * <li>Broadcasts.POST_ID = localID of the post</li>
     * </ol>
     * </p>

     * When loading is successfully finished:
     * <ol>
     * <li>Broadcast.STATUS_FIELD = Broadcasts.FINISHED </li>
     * </ol>
     * </p>
     * <p>
     * If an error occurred:
     * <ol>
     * <li>Broadcasts.STATUS = Broadcasts.ERROR</li>
     * <li>Broadcasts.ERROR_KIND = kind of an error</li>
     * <li>Broadcasts.ERROR_FIELD = description of an error</li>
     * </ol>
     * </p>
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
            post.setLocalId(Loudly.getContext().makeLocalIDForOtherNetworks());
            currentPosts.add(post);
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
                return makeError(Broadcasts.POST_LOAD, Broadcasts.DATABASE_ERROR, e.getMessage());
            }

            publishProgress(makeMessage(Broadcasts.POST_LOAD, Broadcasts.STARTED));

            boolean[] successfulLoading = new boolean[Networks.NETWORK_COUNT];

            for (Wrap w : wraps) {
                try {
                    currentPosts = new LinkedList<>();
                    w.loadPosts(time, this);

                    resultList = merge(resultList, currentPosts);

                    Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);

                    successfulLoading[w.networkID()] = true;
                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.POST_LOAD, Broadcasts.NETWORK_ERROR,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
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
            Loudly.getContext().addPosts(resultList);

            Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.LOADED);
            publishProgress(message);

            for (Post post : resultList) {
                if (post.getAttachments().isEmpty()) {
                    post.setLoadedImage(true);
                } else {
                    post.setLoadedImage(false);
                }
            }

            for (Post post : resultList) {
                if (post.getAttachments().size() != 0) {
                    final long postID = post.getLocalId();
                    Image image = (Image) post.getAttachments().get(0);
                    try {
                        final long imageID = image.getLocalID();
                        Bitmap bitmap;
                        post.setLoadedImage(false);
                        if (image.isLocal()) {
                            Uri uri = Uri.parse(image.getExtra());
                            bitmap = Utils.loadBitmap(uri,
                                    Utils.getDefaultScreenWidth(), Utils.getDefaultScreenWidth());
                        } else {
                            bitmap = Utils.downloadBitmap(image.getExtra(), new BackgroundAction() {
                                        @Override
                                        public void execute(Object... params) {
                                            Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.IMAGE,
                                                    postID);
                                            message.putExtra(Broadcasts.IMAGE_FIELD, imageID);
                                            message.putExtra(Broadcasts.PROGRESS, (int) params[0]);
                                            publishProgress(message);
                                        }
                                    },
                                    Utils.getDefaultScreenWidth(), Utils.getDefaultScreenWidth());
                        }
                        image.setBitmap(bitmap);
                        post.setLoadedImage(true);
                        message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.IMAGE_FINISHED, postID);
                        message.putExtra(Broadcasts.IMAGE_FIELD, imageID);
                        publishProgress(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return makeSuccess(Broadcasts.POST_LOAD);
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
