package base;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import base.attachments.Attachment;
import base.attachments.Image;
import base.attachments.LoudlyImage;
import base.says.Comment;
import base.says.Info;
import base.says.LoudlyPost;
import base.says.Post;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.MainActivity;
import ly.loud.loudly.PeopleList.Item;
import ly.loud.loudly.PeopleList.NetworkDelimiter;
import ly.loud.loudly.RecyclerViewAdapter;
import util.BackgroundAction;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.InvalidTokenException;
import util.ThreadStopped;
import util.TimeInterval;
import util.UIAction;
import util.database.DatabaseActions;
import util.database.DatabaseException;

/**
 * Class made for storing different asynchronous tasks
 */
public class Tasks {
    /**
     * BroadcastReceivingTask for uploading ost to network.
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
        private LoudlyPost post;
        private Wrap[] wraps;
        private LinkedList<Post> posts;

        public PostUploader(LoudlyPost post, LinkedList<Post> posts, Wrap... wraps) {
            this.post = post;
            this.posts = posts;
            this.wraps = wraps;

            Arrays.sort(this.wraps);
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

            publishProgress(makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.STARTED,
                    post.getLocalId()));

            for (Wrap w : wraps) {
                try {

                    Intent message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.PROGRESS,
                            post.getLocalId());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);

                    final int networkID = w.networkID();
                    post.setNetwork(networkID);

                    for (Attachment attachment : post.getAttachments()) {
                        final LoudlyImage image = (LoudlyImage) attachment;
                        image.setNetwork(w.networkID());
                        w.uploadImage((LoudlyImage) attachment, new BackgroundAction() {
                            @Override
                            public void execute(Object... params) {
                                Intent message = makeMessage(Broadcasts.POST_UPLOAD,
                                        Broadcasts.IMAGE, post.getLocalId());
                                message.putExtra(Broadcasts.IMAGE_FIELD, image.getLocalId());
                                message.putExtra(Broadcasts.PROGRESS_FIELD, (int) params[0]);
                                message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                                publishProgress(message);
                            }
                        });
                        message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.IMAGE_FINISHED,
                                post.getLocalId());
                        message.putExtra(Broadcasts.IMAGE_FIELD, image.getLocalId());
                        message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                    }

                    w.uploadPost(post);
                } catch (InvalidTokenException e) {
                    Intent message = makeError(Broadcasts.POST_UPLOAD, Broadcasts.INVALID_TOKEN,
                            post.getLocalId(), e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.POST_UPLOAD, Broadcasts.NETWORK_ERROR,
                            post.getLocalId(), e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                }
            }

            // Save posts links to DB
            try {
                int[] networks = new int[wraps.length];
                for (int i = 0; i < wraps.length; i++) {
                    networks[i] = wraps[i].networkID();
                }

                DatabaseActions.updatePostLinks(networks, post);
            } catch (DatabaseException e) {
                return makeError(Broadcasts.POST_UPLOAD, Broadcasts.DATABASE_ERROR,
                        post.getLocalId(), e.getMessage());
            }

            return makeSuccess(Broadcasts.POST_UPLOAD, post.getLocalId());
        }
    }

    public static class PostDeleter extends BroadcastSendingTask {
        private Post post;
        LinkedList<Post> posts;
        private Wrap[] wraps;

        public PostDeleter(Post post, LinkedList<Post> posts, Wrap... wraps) {
            this.post = post;
            this.wraps = wraps;
            this.posts = posts;
        }

        private void beautifulDelete(Post post) {
            Iterator<Post> iterator = posts.listIterator();
            int ind = 0;
            while (iterator.hasNext()) {
                Post old = iterator.next();
                if (old.equals(post)) {
                    iterator.remove();
                    final int fixed = ind;
                    MainActivity.executeOnUI(new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            MainActivity mainActivity = (MainActivity) context;
                            mainActivity.recyclerViewAdapter.notifyDeletedAtPosition(fixed);
                        }
                    });
                }
                ind++;
            }
        }

        @Override
        protected Intent doInBackground(Object... params) {

            for (Wrap w : wraps) {
                if (post.existsIn(w.networkID())) {
                    try {
                        Intent message = makeMessage(Broadcasts.POST_DELETE, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);

                        post.setNetwork(w.networkID());
                        w.deletePost(post);

                    } catch (InvalidTokenException e) {
                        Intent message = makeError(Broadcasts.POST_DELETE, Broadcasts.INVALID_TOKEN, e.getMessage());
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        publishProgress(makeError(Broadcasts.POST_DELETE, Broadcasts.NETWORK_ERROR,
                                e.getMessage()));
                    }
                }
            }
            // TODO: 15.12.15 removing without wraps
            if (post instanceof LoudlyPost) {
                boolean dead = true;
                for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                    if (post.existsIn(i)) {
                        dead = false;
                        break;
                    }
                }

                // If post is dead, delete it from DB. Otherwise, update its links
                try {
                    if (dead) {
                        beautifulDelete(post);
                        DatabaseActions.deletePost(((LoudlyPost) post));
                    } else {
                        int[] networks = new int[wraps.length];
                        for (int i = 0; i < wraps.length; i++) {
                            networks[i] = wraps[i].networkID();
                        }
                        DatabaseActions.updatePostLinks(networks, ((LoudlyPost) post));
                    }
                } catch (DatabaseException e) {
                    return makeError(Broadcasts.POST_DELETE, Broadcasts.DATABASE_ERROR,
                            e.getMessage());
                }
            } else {
                if (!post.existsIn(post.getNetwork())) {
                    beautifulDelete(post);
                }
            }

            return makeSuccess(Broadcasts.POST_DELETE);
        }
    }


    public static final int LIKES = 0;
    public static final int SHARES = 1;

    public static class PersonGetter extends BroadcastSendingTask {
        private int ID;
        private SingleNetwork element;
        private int what;
        private List<Item> persons;
        private Wrap[] wraps;

        public PersonGetter(int ID, SingleNetwork element, int what, List<Item> persons, Wrap... wraps) {
            this.element = element;
            this.what = what;
            this.persons = persons;
            this.wraps = wraps;
            this.ID = ID;
        }

        @Override
        protected Intent doInBackground(Object... posts) {
            for (Wrap w : wraps) {
                try {
                    if (element.existsIn(w.networkID())) {
                        element.setNetwork(w.networkID()); // for LoudlyPosts

                        List<Person> got = w.getPersons(what, element);
                        if (!got.isEmpty()) {
                            persons.add(new NetworkDelimiter(w.networkID()));
                            persons.addAll(got);
                        }

                        Intent message = makeMessage(Broadcasts.GET_PERSONS, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        message.putExtra(Broadcasts.ID_FIELD, ID);
                        publishProgress(message);
                    }

                } catch (InvalidTokenException e) {
                    Intent message = makeError(Broadcasts.GET_PERSONS, Broadcasts.INVALID_TOKEN,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    message.putExtra(Broadcasts.ID_FIELD, ID);
                    publishProgress(message);
                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.GET_PERSONS, Broadcasts.NETWORK_ERROR,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    message.putExtra(Broadcasts.ID_FIELD, ID);
                    publishProgress(message);
                }
            }
            return makeSuccess(Broadcasts.GET_PERSONS);
        }
    }

    /**
     * Throws Broadcasts.POST_GET_PERSON as like as PersonGetter
     */
    public static class CommentsGetter extends BroadcastSendingTask {
        private int ID;
        private SingleNetwork element;
        private List<Item> comments;
        private Wrap[] wraps;

        public CommentsGetter(int ID, SingleNetwork element, List<Item> comments, Wrap... wraps) {
            this.ID = ID;
            this.element = element;
            this.comments = comments;
            this.wraps = wraps;
        }

        @Override
        protected Intent doInBackground(Object... posts) {
            for (Wrap w : wraps) {
                try {
                    if (element.existsIn(w.networkID())) {
                        element.setNetwork(w.networkID());

                        List<Comment> got = w.getComments(element);
                        if (!got.isEmpty()) {
                            comments.add(new NetworkDelimiter(w.networkID()));
                            comments.addAll(got);
                        }

                        Intent message = makeMessage(Broadcasts.GET_PERSONS, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        message.putExtra(Broadcasts.ID_FIELD, ID);
                        publishProgress(message);
                    }

                } catch (InvalidTokenException e) {
                    Intent message = makeError(Broadcasts.GET_PERSONS, Broadcasts.INVALID_TOKEN,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    message.putExtra(Broadcasts.ID_FIELD, ID);
                    publishProgress(message);
                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.GET_PERSONS, Broadcasts.NETWORK_ERROR,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    message.putExtra(Broadcasts.ID_FIELD, ID);
                    publishProgress(message);
                }
            }
            return makeSuccess(Broadcasts.GET_PERSONS);
        }
    }

    /**
     * Fix image links in posts and notify that data set changed
     */
    public static class FixAttachmentsLinks extends AsyncTask<Object, Object, Object> {
        Post post;
        RecyclerViewAdapter adapter;

        public FixAttachmentsLinks(Post post, RecyclerViewAdapter adapter) {
            this.post = post;
            this.adapter = adapter;
        }

        @Override
        protected Object doInBackground(Object... params) {
            Wrap[] wraps = Loudly.getContext().getWraps();
            Arrays.sort(wraps);
            boolean fixed = false;
            for (Wrap w : wraps) {
                try {
                    if (post.existsIn(w.networkID())) {
                        post.setNetwork(w.networkID());

                        // TODO: 12/12/2015 set image network too
                        LinkedList<Image> images = new LinkedList<>();
                        for (Attachment attachment : post.getAttachments()) {
                            if (attachment instanceof Image) {
                                images.add(((Image) attachment));
                            }
                        }
                        w.getImageInfo(images);
                        fixed = true;
                        break;
                    }
                } catch (IOException e) {
                    Log.e("IOEXCEPTION", e.getMessage());
                }
            }
            if (!fixed) {
                post.getAttachments().clear();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            adapter.notifyDataSetChanged();
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

                // Loading preferences
                SharedPreferences preferences = Loudly.getContext().getSharedPreferences(
                        Loudly.PREFERENCES, Context.MODE_PRIVATE);
                int frequency = preferences.getInt(Loudly.UPDATE_FREQUENCY, 30);
                int loadLast = preferences.getInt(Loudly.LOAD_LAST, 7);
                Loudly.setPreferences(frequency, loadLast);
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
         * @param postID  LoudlyPost
         * @param network network
         * @param info
         * @return Link to post, if it exists, or null, if not
         */
        boolean updateLoudlyPostInfo(String postID, int network, Info info);

        void postLoaded(Post post);
    }

    public interface GetInfoCallback {
        void infoLoaded(Post post, Info info);

        void foundDeletedPost(Post post);
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
     * Before loading posts from some network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.PROGRESS</li>
     * <li>Broadcasts.NETWORK_FIELD = id of the network</li>
     * </ol>
     * <p>
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
        private volatile boolean stopped;
        private LinkedList<Post> posts;
        private TimeInterval time;
        private Wrap[] wraps;
        private LinkedList<LoudlyPost> loudlyPosts;
        private LinkedList<Post> currentPosts;

        private boolean[] loudlyPostExists;


        /**
         * Loads posts from every network
         *
         * @param time  Load posts with date int interval
         * @param wraps Networks, from which load posts
         */
        public LoadPostsTask(LinkedList<Post> posts,
                             TimeInterval time, Wrap... wraps) {
            this.posts = posts;
            this.time = time;
            this.wraps = wraps;
            stopped = false;
        }

        public void stop() {
            stopped = true;
        }

        private void merge(LinkedList<? extends Post> newPosts) {
            int i = 0, j = 0;
            // TODO: 12/11/2015 Make quicker with arrayLists
            while (j < newPosts.size()) {
                if (stopped) throw new ThreadStopped();
                // while date of ith old post is greater than date of jth post in newPosts, i++
                Post right = newPosts.get(j);
                while (i < posts.size() && j < newPosts.size()) {
                    Post post = posts.get(i);

                    if (post.getDate() == right.getDate()) {
                        // Skip existing post (especially for Loudly posts)
                        j++;
                        continue;
                    }
                    if (post.getDate() < right.getDate()) {
                        break;
                    }
                    i++;
                }
                int oldJ = j;
                while (j < newPosts.size() &&
                        (i == posts.size() || newPosts.get(j).getDate() > posts.get(i).getDate())) {
                    j++;
                }
                if (stopped) throw new ThreadStopped();

                posts.addAll(i, newPosts.subList(oldJ, j));
                // Crutch
                int newI = i + j - oldJ;
                if (newI == posts.size()) {
                    MainActivity.executeOnUI(new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            MainActivity mainActivity = ((MainActivity) context);
                            mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    final int fixedI = i;
                    final int fixedLength = j - oldJ;
                    MainActivity.executeOnUI(new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            MainActivity mainActivity = (MainActivity) context;
                            mainActivity.recyclerViewAdapter.notifyItemRangeInserted(fixedI, fixedLength);
                        }
                    });
                }
                i = newI;
            }
        }

        @Override
        public boolean updateLoudlyPostInfo(String postID, int network, Info info) {
            int ind = 0;
            for (LoudlyPost lPost : loudlyPosts) {
                if (stopped) throw new ThreadStopped();

                if (lPost.existsIn(network) && lPost.getId(network).equals(postID)) {
                    loudlyPostExists[ind] = true;
                    lPost.setInfo(network, info);
                    final int postPosition = ind;
                    MainActivity.executeOnUI(new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            MainActivity mainActivity = (MainActivity) context;
                            mainActivity.recyclerViewAdapter.notifyItemChanged(postPosition);
                        }
                    });
                    return true;
                }
                ind++;
            }
            return false;
        }

        @Override
        public void postLoaded(Post post) {
            if (stopped) throw new ThreadStopped();
            currentPosts.add(post);
        }

        @Override
        protected Intent doInBackground(Object... params) {
            publishProgress(makeMessage(Broadcasts.POST_LOAD, Broadcasts.STARTED));
            try {
                loudlyPosts = DatabaseActions.loadPosts(time);
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Broadcasts.POST_LOAD, Broadcasts.DATABASE_ERROR, e.getMessage());
            }

            publishProgress(makeMessage(Broadcasts.POST_LOAD, Broadcasts.LOADED));
            loudlyPostExists = new boolean[loudlyPosts.size()];


            merge(loudlyPosts);

            for (Wrap w : wraps) {
                try {
                    if (stopped) throw new ThreadStopped();

                    Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                    currentPosts = new LinkedList<>();
                    w.loadPosts(time, this);

                    int ind = 0;
                    for (LoudlyPost loudlyPost : loudlyPosts) {
                        if (!loudlyPostExists[ind++]) {
                            loudlyPost.setId(w.networkID(), null);
                        }
                    }
                    Arrays.fill(loudlyPostExists, false);

                    merge(currentPosts);
                } catch (InvalidTokenException e) {
                    Intent message = makeError(Broadcasts.POST_LOAD, Broadcasts.INVALID_TOKEN,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.POST_LOAD, Broadcasts.NETWORK_ERROR,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                } catch (ThreadStopped e) {
                    return makeSuccess(Broadcasts.POST_LOAD);
                }
            }

            for (LoudlyPost p : loudlyPosts) {
                boolean postAlive = false;
                for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                    if (p.existsIn(i)) {
                        postAlive = true;
                    }
                }
                if (!postAlive) {
                    try {
                        DatabaseActions.deletePost(p);
                        int ind = 0;
                        for (Post post : posts) {
                            if (post instanceof LoudlyPost) {
                                if (((LoudlyPost) post).getLocalId() == p.getLocalId()) {
                                    posts.remove(ind);
                                    final int fixedInd = ind;
                                    MainActivity.executeOnUI(new UIAction() {
                                        @Override
                                        public void execute(Context context, Object... params) {
                                            MainActivity mainActivity = (MainActivity) context;
                                            mainActivity.recyclerViewAdapter.notifyDeletedAtPosition(fixedInd);
                                        }
                                    });
                                    break;
                                }
                            }
                            ind++;
                        }
                    } catch (DatabaseException e) {
                        publishProgress(makeError(Broadcasts.POST_LOAD, Broadcasts.DATABASE_ERROR,
                                e.getMessage()));
                    }
                }
            }

            return makeSuccess(Broadcasts.POST_LOAD);
        }
    }
}
