package base;


import android.content.ContentProvider;
import android.content.ContentResolver;
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
     * <p/>
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

    public static class PostUploader extends BroadcastSendingTask {
        private Post post;
        private Wrap[] wraps;
        private LinkedList<Post> posts;

        public PostUploader(Post post, LinkedList<Post> posts, Wrap... wraps) {
            this.post = post;
            this.posts = posts;
            this.wraps = wraps;
        }

        @Override
        protected Intent doInBackground(Object... params) {
            try {
                DatabaseActions.savePost(post);
            } catch (DatabaseException e) {
                return makeError(Broadcasts.POST_UPLOAD, Broadcasts.DATABASE_ERROR,
                        e.getMessage());
            }

            posts.add(0, post);
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
    public static class InfoGetter extends BroadcastSendingTask {
        private LinkedList<Post> posts;
        private Wrap[] wraps;

        public InfoGetter(LinkedList<Post> posts, Wrap... wraps) {
            this.posts = posts;
            this.wraps = wraps;
        }

        @Override
        protected Intent doInBackground(Object... params) {
            try {
                for (Wrap w : wraps) {
                    w.getPostsInfo(posts);
                    Intent message = makeMessage(Broadcasts.POST_GET_INFO, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                }
            } catch (IOException e) {
                publishProgress(makeError(Broadcasts.POST_GET_INFO, Broadcasts.NETWORK_ERROR,
                        e.getMessage()));
            }

            return makeSuccess(Broadcasts.POST_GET_INFO);
        }
    }

    public static final int LIKES = 0;
    public static final int SHARES = 1;

    public static class PersonGetter extends BroadcastSendingTask {
        private Post post;
        private int what;
        private LinkedList<Person> persons;
        private Wrap[] wraps;

        public PersonGetter(Post post, int what, LinkedList<Person> persons, Wrap... wraps) {
            this.post = post;
            this.what = what;
            this.persons = persons;
            this.wraps = wraps;
        }

        @Override
        protected Intent doInBackground(Object... posts) {
            for (Wrap w : wraps) {
                try {
                    persons.addAll(w.getPersons(what, post));
                    Intent message = makeMessage(Broadcasts.POST_GET_PERSONS, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                } catch (IOException e) {
                    publishProgress(makeError(Broadcasts.POST_GET_PERSONS, Broadcasts.NETWORK_ERROR,
                            e.getMessage()));
                }
            }
            return makeSuccess(Broadcasts.POST_GET_PERSONS);
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
    public static class SaveKeysTask extends BroadcastSendingTask {
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
    public static class LoadKeysTask extends BroadcastSendingTask {
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
     * <li>Broadcasts.STATUS_FIELD = Broadcast.LOADED</li>
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
     * <p/>
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

    public static class LoadPostsTask extends BroadcastSendingTask implements LoadCallback {
        private LinkedList<Post> posts;
        private TimeInterval time;
        private Wrap[] wraps;
        private LinkedList<Post> loudlyPosts;
        private LinkedList<Post> currentPosts;


        /**
         * Loads posts from every network
         *
         * @param time  Load posts with date int interval
         * @param wraps Networks, from which load posts
         */
        public LoadPostsTask(LinkedList<Post> posts, TimeInterval time, Wrap[] wraps) {
            this.posts = posts;
            this.time = time;
            this.wraps = wraps;
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

        @Override
        protected Intent doInBackground(Object... params) {
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

                    resultList = Utils.merge(resultList, currentPosts);

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

            resultList = Utils.merge(resultList, cleaned);

            posts.addAll(resultList);

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
}
