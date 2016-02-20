package base;


import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import base.attachments.Attachment;
import base.attachments.Image;
import base.says.Comment;
import base.says.Info;
import base.says.LoudlyPost;
import base.says.Post;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.MainActivity;
import ly.loud.loudly.MainActivityPostsAdapter;
import ly.loud.loudly.adapter.Item;
import ly.loud.loudly.adapter.NetworkDelimiter;
import util.BackgroundAction;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.InvalidTokenException;
import util.ThreadStopped;
import util.TimeInterval;
import util.UIAction;

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
        private Post post;
        private Wrap[] wraps;
        private List<Post> posts;

        public PostUploader(Post post, List<Post> posts, Wrap... wraps) {
            this.post = post;
            this.posts = posts;
            this.wraps = wraps;

            Arrays.sort(this.wraps);
        }

        @Override
        protected Intent doInBackground(Object... params) {
            MainActivity.executeOnUI(new UIAction<MainActivity>() {
                @Override
                public void execute(MainActivity context, Object... params) {
                    posts.add(0, post);
                    context.mainActivityPostsAdapter.notifyItemInserted(0);
                    Loudly.getContext().stopGetInfoService();
                }
            });

//            publishProgress(makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.STARTED));

            for (Wrap w : wraps) {
                try {
                    Intent message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);

                    final int networkID = w.networkID();
                    post.setNetwork(networkID);

                    for (Attachment attachment : post.getAttachments()) {
                        attachment.setNetwork(w.networkID());
                        final Attachment fixed = attachment;
                        w.upload((Image) attachment, new BackgroundAction() {
                            @Override
                            public void execute(Object... params) {
                                Intent message = makeMessage(Broadcasts.POST_UPLOAD,
                                        Broadcasts.IMAGE);
                                message.putExtra(Broadcasts.IMAGE_FIELD, fixed.getId());
                                message.putExtra(Broadcasts.PROGRESS_FIELD, (int) params[0]);
                                message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                                publishProgress(message);
                            }
                        });
                        message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.IMAGE_FINISHED);
                        message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                    }

                    w.upload(post);
                } catch (InvalidTokenException e) {
                    Intent message = makeError(Broadcasts.POST_UPLOAD, Broadcasts.INVALID_TOKEN, e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.POST_UPLOAD, Broadcasts.NETWORK_ERROR, e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                }
            }

            return makeSuccess(Broadcasts.POST_UPLOAD);
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
            Arrays.sort(wraps); // todo not good
        }

        @Override
        protected Intent doInBackground(Object... params) {
            final LinkedList<Integer> success = new LinkedList<>();
            for (Wrap w : wraps) {
                if (post.existsIn(w.networkID())) {
                    try {
                        Intent message = makeMessage(Broadcasts.POST_DELETE, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);

                        post.setNetwork(w.networkID());
                        w.delete(post);
                        success.add(w.networkID());
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

            MainActivity.executeOnUI(new UIAction<MainActivity>() {
                @Override
                public void execute(MainActivity context, Object... params) {
                    context.mainActivityPostsAdapter.cleanUp(success);
                }
            });

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
                        if (got != null && !got.isEmpty()) { // TODO crutch (remove)
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
                        if (got != null && !got.isEmpty()) {
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
        MainActivityPostsAdapter adapter;

        public FixAttachmentsLinks(Post post, MainActivityPostsAdapter adapter) {
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
                        w.updateImagesInfo(images);
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

    public interface LoadCallback {
        void postLoaded(Post post);
    }

    public interface GetInfoCallback {
        void infoLoaded(Post post, Info info);
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
        private TimeInterval time;
        private Wrap[] wraps;
        private LinkedList<Post> currentPosts;
        private LinkedList<Post> loaded;

        /**
         * Loads posts from every network
         *
         * @param time  Load posts with date int interval
         * @param wraps Networks, from which load posts
         */
        public LoadPostsTask(TimeInterval time, Wrap... wraps) {
            this.time = time;
            this.wraps = wraps;
            stopped = false;
        }

        public void stop() {
            stopped = true;
        }

        @Override
        public void postLoaded(Post post) {
            Post alreadyLoaded = null;
            for (Post p : loaded) {
                if (p.equals(post)) {
                    alreadyLoaded = p;
                    break;
                }
            }
            if (stopped) throw new ThreadStopped();
            if (alreadyLoaded == null) {
                currentPosts.add(post);
                loaded.add(post);
            } else {
                alreadyLoaded.setNetwork(post.getNetwork());
                alreadyLoaded.setInfo(post.getInfo());
                alreadyLoaded.getId().setValid(true);

                // todo: fix index
                final Post fixed = alreadyLoaded;
                MainActivity.executeOnUI(new UIAction<MainActivity>() {
                    @Override
                    public void execute(MainActivity mainActivity, Object... params) {
                        mainActivity.mainActivityPostsAdapter.notifyPostChanged(fixed);
                    }
                });
            }
        }

        /**
         * Mark loudly post from some network as valid
         *
         * @param network current network
         */
        private void setLoaded(int network) {
            for (Post post : loaded) {
                if (post instanceof LoudlyPost) {
                    Link link = ((LoudlyPost) post).getId(network);
                    if (link != null && !link.isValid()) {
                        link.setValid(true);
                    }
                }
            }
        }

        @Override
        protected Intent doInBackground(Object... params) {
            publishProgress(makeMessage(Broadcasts.POST_LOAD, Broadcasts.STARTED));
            loaded = new LinkedList<>();
            final LinkedList<Integer> successfullyLoaded = new LinkedList<>();
            for (Wrap w : wraps) {
                try {
                    if (stopped) throw new ThreadStopped();

                    Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.PROGRESS);
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                    currentPosts = new LinkedList<>();
                    w.loadPosts(time, this);

                    final LinkedList<Post> copy = new LinkedList<>();
                    for (Post post : currentPosts) {
                        copy.add(post);
                    }
                    MainActivity.executeOnUI(new UIAction<MainActivity>() {
                        @Override
                        public void execute(MainActivity context, Object... params) {
                            context.mainActivityPostsAdapter.merge(copy);
                        }
                    });
                    successfullyLoaded.add(w.networkID());
                } catch (InvalidTokenException e) {
                    Intent message = makeError(Broadcasts.POST_LOAD, Broadcasts.INVALID_TOKEN,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);

                    // Set posts from this network loaded, so we can't delete them from DB
                    setLoaded(w.networkID());
                } catch (IOException e) {
                    Intent message = makeError(Broadcasts.POST_LOAD, Broadcasts.NETWORK_ERROR,
                            e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    publishProgress(message);
                    setLoaded(w.networkID());
                } catch (ThreadStopped e) {
                    return makeSuccess(Broadcasts.POST_LOAD);
                }
            }

            MainActivity.executeOnUI(new UIAction<MainActivity>() {
                @Override
                public void execute(MainActivity context, Object... params) {
                    context.mainActivityPostsAdapter.cleanUp(successfullyLoaded);
                }
            });

            return makeSuccess(Broadcasts.POST_LOAD);
        }
    }
}
