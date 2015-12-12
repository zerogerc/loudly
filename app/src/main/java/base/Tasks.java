package base;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import base.attachments.Attachment;
import base.attachments.Image;
import base.says.LoudlyPost;
import base.says.Post;
import base.says.SinglePost;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.MainActivity;
import ly.loud.loudly.PeopleList.Item;
import ly.loud.loudly.PeopleList.NetworkDelimiter;
import ly.loud.loudly.RecyclerViewAdapter;
import util.BackgroundAction;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.TimeInterval;
import util.UIAction;
import util.Utils;
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

            if (post.getAttachments().size() != 0) {
                Image image = (Image) post.getAttachments().get(0);
                Utils.resolveImageSize(image);
            }

            posts.add(0, post);

            publishProgress(makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.STARTED,
                    post.getLocalId()));

            for (Wrap w : wraps) {
                try {
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
                    publishProgress(message);
                } catch (IOException e) {

                    publishProgress(makeError(Broadcasts.POST_UPLOAD, Broadcasts.NETWORK_ERROR,
                            post.getLocalId(), e.getMessage()));
                }
            }

            // Change image links to external
            for (Attachment attachment : post.getAttachments()) {
                if (attachment instanceof Image) {
                    Image image = ((Image) attachment);
                    if (!image.isLocal()) {
                        image.setInternalLink(image.getExternalLink());
                    }
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
            int i = 0;
            for (Post p : posts) {
                if (p.equals(post)) {
                    break;
                }
                i++;
            }
            posts.remove(i);
            final int fixed = i;
            MainActivity.executeOnMain(new UIAction() {
                @Override
                public void execute(Context context, Object... params) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.recyclerViewAdapter.deleteAtPosition(fixed);
                }
            });

        }

        @Override
        protected Intent doInBackground(Object... params) {
            for (Wrap w : wraps) {
                if (post.existsIn(w.networkID())) {
                    try {
                        w.deletePost(post);
                        Intent message = makeMessage(Broadcasts.POST_DELETE, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        publishProgress(makeError(Broadcasts.POST_DELETE, Broadcasts.NETWORK_ERROR,
                                e.getMessage()));
                    }
                }
            }

            if (post instanceof SinglePost) {
                if (!post.existsIn(post.getNetwork())) {
                    beautifulDelete(post);
                }
            }

            if (post instanceof LoudlyPost) {
                boolean dead = true;
                for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                    if (post.existsIn(i)) {
                        dead = false;
                        break;
                    }
                }

                // If post is dead, delete it from DB. Otherwise, update its links
                if (dead) {
                    beautifulDelete(post);
                    try {
                        DatabaseActions.deletePost(((LoudlyPost) post));
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                        return makeError(Broadcasts.POST_DELETE, Broadcasts.DATABASE_ERROR,
                                e.getMessage());
                    }
                } else {
                    int[] networks = new int[wraps.length];
                    for (int i = 0; i < wraps.length; i++) {
                        networks[i] = wraps[i].networkID();
                    }

                    try {
                        DatabaseActions.updatePostLinks(networks, ((LoudlyPost) post));
                    } catch (DatabaseException e) {
                        return makeError(Broadcasts.POST_DELETE, Broadcasts.DATABASE_ERROR,
                                e.getMessage());
                    }
                }
            }

            return makeSuccess(Broadcasts.POST_DELETE);
        }
    }


    public static final int LIKES = 0;
    public static final int SHARES = 1;

    public static class PersonGetter extends BroadcastSendingTask {
        private Post post;
        private int what;
        private List<Item> persons;
        private Wrap[] wraps;

        public PersonGetter(Post post, int what, List<Item> persons, Wrap... wraps) {
            this.post = post;
            this.what = what;
            this.persons = persons;
            this.wraps = wraps;
        }

        @Override
        protected Intent doInBackground(Object... posts) {
            for (Wrap w : wraps) {
                try {
                    if (post.existsIn(w.networkID())) {
                        List<Person> got = w.getPersons(what, post);
                        if (!got.isEmpty()) {
                            persons.add(new NetworkDelimiter(w.networkID()));
                            persons.addAll(w.getPersons(what, post));
                        }

                        Intent message = makeMessage(Broadcasts.POST_GET_PERSONS, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    }

                } catch (IOException e) {
                    publishProgress(makeError(Broadcasts.POST_GET_PERSONS, Broadcasts.NETWORK_ERROR,
                            e.getMessage()));
                }
            }
            return makeSuccess(Broadcasts.POST_GET_PERSONS);
        }
    }

    /**
     * Throws Broadcasts.POST_GET_PERSON as like as PersonGetter
     */
    public static class CommentsGetter extends BroadcastSendingTask {
        private Post post;
        private int what;
        private List<Item> comments;
        private Wrap[] wraps;

        public CommentsGetter(Post post, int what, List<Item> comments, Wrap... wraps) {
            this.post = post;
            this.what = what;
            this.comments = comments;
            this.wraps = wraps;
        }

        @Override
        protected Intent doInBackground(Object... posts) {
            for (Wrap w : wraps) {
                try {
                    if (post.existsIn(w.networkID())) {
                        List<Person> got = w.getPersons(what, post);
                        if (!got.isEmpty()) {
                            comments.add(new NetworkDelimiter(w.networkID()));
                            comments.addAll(w.getComments(post));
                        }

                        Intent message = makeMessage(Broadcasts.POST_GET_PERSONS, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    }

                } catch (IOException e) {
                    publishProgress(makeError(Broadcasts.POST_GET_PERSONS, Broadcasts.NETWORK_ERROR,
                            e.getMessage()));
                }
            }
            return makeSuccess(Broadcasts.POST_GET_PERSONS);
        }
    }

    /**
     * Fix image links in posts and notify that data set changed
     */
    public static class FixPostsLinks extends AsyncTask<Object, Object, Object> {
        Post post;
        RecyclerViewAdapter adapter;

        public FixPostsLinks(Post post, RecyclerViewAdapter adapter) {
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
         * @return Link to post, if it exists, or null, if not
         */
        LoudlyPost findLoudlyPost(String postID, int network);

        void postLoaded(SinglePost post);
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
        private RecyclerViewAdapter adapter;
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
        public LoadPostsTask(LinkedList<Post> posts, RecyclerViewAdapter adapter,
                             TimeInterval time, Wrap... wraps) {
            this.posts = posts;
            this.time = time;
            this.wraps = wraps;
            this.adapter = adapter;
        }

        private void merge(LinkedList<? extends Post> newPosts) {
            Log.e("TASKS", "merging");
            int i = 0, j = 0;
            // TODO: 12/11/2015 Make quicker with arrayLists
            while (j < newPosts.size()) {
                // while date of ith old post is greater than date of jth post in newPosts, i++
                while (i < posts.size()) {
                    Post post = posts.get(i);

                    // Notify that likes in LoudlyPost have changed
                    if (post instanceof LoudlyPost) {
                        final int postPosition = i;
                        MainActivity.executeOnMain(new UIAction() {
                            @Override
                            public void execute(Context context, Object... params) {
                                MainActivity mainActivity = (MainActivity) context;
                                mainActivity.recyclerViewAdapter.notifyItemChanged(postPosition);
                            }
                        });
                    }
                    if (post.getDate() < newPosts.get(j).getDate()) {
                        break;
                    }
                    i++;
                }
                int oldJ = j;
                while (j < newPosts.size() &&
                        (i == posts.size() || newPosts.get(j).getDate() > posts.get(i).getDate())) {
                    j++;
                }
                posts.addAll(i, newPosts.subList(oldJ, j));
                // Crutch
                int newI = i + j - oldJ;
                if (newI == posts.size()) {
                    MainActivity.executeOnMain(new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            MainActivity mainActivity = ((MainActivity) context);
                            mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    final int fixedI = i;
                    final int fixedLength = j - oldJ;
                    MainActivity.executeOnMain(new UIAction() {
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
        public LoudlyPost findLoudlyPost(String postID, int network) {
            int ind = 0;
            for (LoudlyPost lPost : loudlyPosts) {
                if (lPost.existsIn(network) && lPost.getLink(network).equals(postID)) {
                    loudlyPostExists[ind] = true;
                    return lPost;
                }
                ind++;
            }
            return null;
        }

        @Override
        public void postLoaded(SinglePost post) {
            currentPosts.add(post);
        }

        @Override
        protected Intent doInBackground(Object... params) {
            try {
                loudlyPosts = DatabaseActions.loadPosts(time);
            } catch (DatabaseException e) {
                e.printStackTrace();
                return makeError(Broadcasts.POST_LOAD, Broadcasts.DATABASE_ERROR, e.getMessage());
            }

            loudlyPostExists = new boolean[loudlyPosts.size()];

            publishProgress(makeMessage(Broadcasts.POST_LOAD, Broadcasts.STARTED));

            merge(loudlyPosts);

            for (Wrap w : wraps) {
                try {
                    currentPosts = new LinkedList<>();
                    w.loadPosts(time, this);

                    int ind = 0;
                    for (LoudlyPost loudlyPost : loudlyPosts) {
                        if (!loudlyPostExists[ind++]) {
                            loudlyPost.setLink(w.networkID(), null);
                        }
                    }
                    Arrays.fill(loudlyPostExists, false);

                    merge(currentPosts);

                    Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);

                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.POST_LOAD, Broadcasts.NETWORK_ERROR,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
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
                                    final int postSize = posts.size();
                                    MainActivity.executeOnMain(new UIAction() {
                                        @Override
                                        public void execute(Context context, Object... params) {
                                            MainActivity mainActivity = (MainActivity) context;
                                            mainActivity.recyclerViewAdapter.
                                                    notifyItemRemoved(fixedInd);
                                            mainActivity.recyclerViewAdapter.
                                                    notifyItemRangeChanged(fixedInd, postSize);
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

            Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.LOADED);
            publishProgress(message);

            return makeSuccess(Broadcasts.POST_LOAD);
        }
    }
}
