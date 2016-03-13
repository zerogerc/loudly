package ly.loud.loudly.base;


import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.ui.Loudly;
import ly.loud.loudly.ui.MainActivity;
import ly.loud.loudly.ui.MainActivityPostsAdapter;
import ly.loud.loudly.ui.adapter.Item;
import ly.loud.loudly.ui.adapter.NetworkDelimiter;
import ly.loud.loudly.util.BackgroundAction;
import ly.loud.loudly.util.BroadcastSendingTask;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.InvalidTokenException;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.UIAction;

/**
 * Class made for storing different asynchronous tasks
 */
public final class Tasks {

    public interface ActionWithWrap<T, R> {
        R apply(T item, Wrap w);
    }

    public interface ActionWithResult<R> {
        void apply(R result);
    }

    /**
     * Perform an action for every wrap asynchronous and wait until it's finished
     *
     * @param item             item
     * @param action           action to do with item
     * @param actionWithResult action to do with result (or null if nothing should be done)
     * @param wraps            wraps
     * @param <T>              type of item, such as Post or List<Post>
     */
    @SuppressWarnings("unchecked")
    public static <T, R> void doAndWait(final T item, final ActionWithWrap<T, R> action,
                                        ActionWithResult<R> actionWithResult, final Wrap... wraps) {
        AsyncTask<Object, Object, R>[] tasks = new AsyncTask[wraps.length];
        // Loudly wrap should be executed after previous finished
        int loudlyWrapPos = -1;
        for (int i = 0; i < wraps.length; i++) {
            final Wrap curWrap = wraps[i];
            if (curWrap.networkID() == Networks.LOUDLY) {
                loudlyWrapPos = i;
            }
            tasks[i] = new AsyncTask<Object, Object, R>() {
                @Override
                protected R doInBackground(Object... params) {
                    return action.apply(item, curWrap);
                }
            };
        }
        // Perform actions before LoudlyWrap, wait till they finished, then do LoudlyWrap,
        // then do other actions
        int st = 0;
        int en = loudlyWrapPos;
        for (int j = 0; j < 3; j++) {
            for (int i = st; i < en; i++) {
                tasks[i] = tasks[i].executeOnExecutor(Loudly.getExecutor());
            }
            for (int i = st; i < en; i++) {
                try {
                    if (actionWithResult == null) {
                        continue;
                    }
                    actionWithResult.apply(tasks[i].get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Log.e("ly/loud/loudly/networks/Loudly", "doAndWait: Execution exception", e);
                    e.printStackTrace();
                }
            }

            if (j == 0) {
                if (loudlyWrapPos == -1) {
                    st = 0;
                    en = tasks.length;
                } else {
                    st = en;
                    en = loudlyWrapPos + 1;
                }
            }
            if (j == 1) {
                st = en;
                en = tasks.length;
            }
        }
    }

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
     * <li>Broadcasts.NETWORK_ID = link of the network</li>
     * </ol>
     * After uploading image to network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.IMAGE_FINISHED</li>
     * <li>Broadcast.ID_FIELD = localID of the post</li>
     * <li>Broadcasts.IMAGE_FIELD = localID of an image</li>
     * <li>Broadcasts.POST_ID = localID of the post</li>
     * <li>Broadcasts.NETWORK_ID = link of the network</li>
     * </ol>
     * </p>
     * <p/>
     * After uploading post to some network:
     * <ol>
     * <li>Broadcasts.STATUS_FIELD = Broadcasts.PROGRESS</li>
     * <li>Broadcast.ID_FIELD = localID of the post</li>
     * <li>Broadcasts.NETWORK_ID = link of the network</li>
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

            publishProgress(makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.STARTED));

            doAndWait(post, new ActionWithWrap<Post, Integer>() {
                @Override
                public Integer apply(Post item, Wrap w) {
                    try {
                        final int networkID = w.networkID();
                        Post post = (Post) item.getNetworkInstance(w.networkID());

                        for (Attachment attachment : post.getAttachments()) {
                            final Attachment fixed = attachment;
                            w.upload((Image) attachment, new BackgroundAction() {
                                @Override
                                public void execute(Object... params) {
                                    Intent message = makeMessage(Broadcasts.POST_UPLOAD,
                                            Broadcasts.IMAGE);
                                    message.putExtra(Broadcasts.IMAGE_FIELD, fixed.getLink());
                                    message.putExtra(Broadcasts.PROGRESS_FIELD, (int) params[0]);
                                    message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                                    publishProgress(message);
                                }
                            });
                            Intent message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.IMAGE_FINISHED);
                            message.putExtra(Broadcasts.NETWORK_FIELD, networkID);
                            publishProgress(message);
                        }

                        w.upload(post);
                        return w.networkID();
                    } catch (InvalidTokenException e) {
                        Intent message = makeError(Broadcasts.POST_UPLOAD, Broadcasts.INVALID_TOKEN, e.getMessage());
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    } catch (IOException e) {
                        Intent message = makeError(Broadcasts.POST_UPLOAD, Broadcasts.NETWORK_ERROR, e.getMessage());
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    }
                    return null;
                }
            }, new ActionWithResult<Integer>() {
                @Override
                public void apply(Integer result) {
                    if (result != null) {
                        Intent message = makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, result);
                        publishProgress(message);
                    }
                }
            }, wraps);

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
            ArrayList<Wrap> goodWraps = new ArrayList<>();
            for (Wrap w : wraps) {
                if (post.existsIn(w.networkID())) {
                    goodWraps.add(w);
                }
            }

            doAndWait(post, new ActionWithWrap<Post, Integer>() {
                @Override
                public Integer apply(Post item, Wrap w) {
                    try {
                        Intent message = makeMessage(Broadcasts.POST_DELETE, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);

                        w.delete((Post) post.getNetworkInstance(w.networkID()));
                        return w.networkID();
                    } catch (InvalidTokenException e) {
                        Intent message = makeError(Broadcasts.POST_DELETE, Broadcasts.INVALID_TOKEN, e.getMessage());
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        publishProgress(makeError(Broadcasts.POST_DELETE, Broadcasts.NETWORK_ERROR,
                                e.getMessage()));
                    }
                    return null;
                }
            }, new ActionWithResult<Integer>() {
                @Override
                public void apply(Integer result) {
                    if (result != null) {
                        success.add(result);
                    }
                }
            }, goodWraps.toArray(new Wrap[goodWraps.size()]));

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
            ArrayList<Wrap> goodWraps = new ArrayList<>();
            for (Wrap w : wraps) {
                if (element.existsIn(w.networkID())) {
                    goodWraps.add(w);
                }
            }
            doAndWait(element, new ActionWithWrap<SingleNetwork, Pair<List<Person>, Integer>>() {
                @Override
                public Pair<List<Person>, Integer> apply(SingleNetwork item, Wrap w) {
                    try {
                        return new Pair<>(w.getPersons(what, element.getNetworkInstance(w.networkID())),
                                w.networkID());
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
                    return null;
                }
            }, new ActionWithResult<Pair<List<Person>, Integer>>() {
                @Override
                public void apply(Pair<List<Person>, Integer> result) {
                    if (result != null) {
                        if (!result.first.isEmpty()) {
                            // ToDO: there is a bug
                            persons.add(new NetworkDelimiter(result.second));
                            persons.addAll(result.first);
                        }

                        Intent message = makeMessage(Broadcasts.GET_PERSONS, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, result.second);
                        message.putExtra(Broadcasts.ID_FIELD, ID);
                        publishProgress(message);
                    }
                }
            }, goodWraps.toArray(new Wrap[goodWraps.size()]));

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
            ArrayList<Wrap> goodWraps = new ArrayList<>();
            for (Wrap w : wraps) {
                if (element.existsIn(w.networkID())) {
                    goodWraps.add(w);
                }
            }
            doAndWait(element, new ActionWithWrap<SingleNetwork, Pair<List<Comment>, Integer>>() {
                @Override
                public Pair<List<Comment>, Integer> apply(SingleNetwork item, Wrap w) {
                    try {
                        return new Pair<>(w.getComments(element.getNetworkInstance(w.networkID())),
                                w.networkID());
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
                    return null;
                }
            }, new ActionWithResult<Pair<List<Comment>, Integer>>() {
                @Override
                public void apply(Pair<List<Comment>, Integer> result) {
                    if (result != null) {
                        if (!result.first.isEmpty()) {
                            // ToDO: there is a bug
                            comments.add(new NetworkDelimiter(result.second));
                            comments.addAll(result.first);
                        }

                        Intent message = makeMessage(Broadcasts.GET_PERSONS, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, result.second);
                        message.putExtra(Broadcasts.ID_FIELD, ID);
                        publishProgress(message);
                    }
                }
            }, goodWraps.toArray(new Wrap[goodWraps.size()]));

            return makeSuccess(Broadcasts.GET_PERSONS);
        }
    }

    /**
     * Fix image links in posts and notify that data set changed
     * Let it be deprecated until it's fixed
     */
    @Deprecated
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

                        LinkedList<Image> images = new LinkedList<>();
                        for (Attachment attachment : post.getAttachments()) {
                            if (attachment instanceof Image) {
                                images.add(((Image) attachment.getNetworkInstance(w.networkID())));
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
     * <li>Broadcasts.NETWORK_FIELD = link of the network</li>
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

    public static class LoadPostsTask extends BroadcastSendingTask {
        private TimeInterval time;
        private Wrap[] wraps;

        /**
         * Loads posts from every network
         *
         * @param time  Load posts with date int interval
         * @param wraps Networks, from which load posts
         */
        public LoadPostsTask(TimeInterval time, Wrap... wraps) {
            this.time = time;
            this.wraps = wraps;
        }

        public void stop() {
            Log.e("load post task", "should be stopped, but isn't stopped");
        }

        @Override
        protected Intent doInBackground(Object... params) {
            publishProgress(makeMessage(Broadcasts.POST_LOAD, Broadcasts.STARTED));
            final ArrayList<Integer> successfullyLoaded = new ArrayList<>();

            doAndWait(time, new ActionWithWrap<TimeInterval, Pair<List<Post>, Integer>>() {
                @Override
                public Pair<List<Post>, Integer> apply(TimeInterval item, Wrap w) {
                    try {
                        Intent message = makeMessage(Broadcasts.POST_LOAD, Broadcasts.PROGRESS);
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                        return new Pair<>(w.loadPosts(time), w.networkID());

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
                    }
                    return null;
                }
            }, new ActionWithResult<Pair<List<Post>, Integer>>() {
                @Override
                public void apply(final Pair<List<Post>, Integer> result) {
                    if (result != null) {
                        MainActivity.executeOnUI(new UIAction<MainActivity>() {
                            @Override
                            public void execute(MainActivity context, Object... params) {
                                context.mainActivityPostsAdapter.merge(result.first, result.second);
                            }
                        });

                        successfullyLoaded.add(result.second);
                    }
                }
            }, wraps);

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
