package ly.loud.loudly.base;


import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.ui.MainActivity;
import ly.loud.loudly.ui.PostsAdapter;
import ly.loud.loudly.util.BackgroundAction;
import ly.loud.loudly.util.BroadcastSendingTask;
import ly.loud.loudly.util.Broadcasts;
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
                    Log.e("Loudly", "doAndWait: Execution exception", e);
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

        public PostUploader(Post post, Wrap... wraps) {
            this.post = post;
            this.wraps = wraps;

            Arrays.sort(this.wraps);
        }

        @Override
        protected Intent doInBackground(Object... params) {
            Loudly.getContext().stopGetInfoService();

            MainActivity.executeOnUI(new UIAction<MainActivity>() {
                @Override
                public void execute(MainActivity context, Object... params) {
                    Loudly.getContext().stopGetInfoService();
                }
            });

            publishProgress(makeMessage(Broadcasts.POST_UPLOAD, Broadcasts.STARTED));

            for (Wrap w : wraps) {
                ((LoudlyPost)post).setLink(w.networkID(), new Link());
            }

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
                    } catch (TokenExpiredException e) {
                        Intent message = makeError(Broadcasts.INTERNAL_MESSAGE, Broadcasts.EXPIRED_TOKEN, e.getMessage());
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
                public void apply(final Integer result) {
                    if (result != null) {
                        MainActivity.executeOnUI(new UIAction<MainActivity>() {
                            @Override
                            public void execute(MainActivity context, Object... params) {
                                Loudly.getPostHolder().merge(Collections.singletonList((Post) post.getNetworkInstance(result)),
                                        result);
                            }
                        });
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
        private Wrap[] wraps;

        public PostDeleter(Post post, Wrap... wraps) {
            this.post = post;
            this.wraps = wraps;
            Arrays.sort(wraps); // todo not good
        }

        @Override
        protected Intent doInBackground(Object... params) {
            final List<Integer> success = new ArrayList<>();
            final List<Integer> fail = new ArrayList<>();
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
                    } catch (TokenExpiredException e) {
                        Intent message = makeError(Broadcasts.INTERNAL_MESSAGE, Broadcasts.EXPIRED_TOKEN, e.getMessage());
                        message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                        publishProgress(message);
                        fail.add(w.networkID());
                    } catch (IOException e) {
                        e.printStackTrace();
                        publishProgress(makeError(Broadcasts.POST_DELETE, Broadcasts.NETWORK_ERROR,
                                e.getMessage()));
                        fail.add(w.networkID());
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
                    Loudly.getPostHolder().cleanUp(success, true);
                }
            });

            if (fail.isEmpty()) {
                return makeSuccess(Broadcasts.POST_DELETE);
            } else {
                return null;
            }
        }
    }

    /**
     * Fix image links in posts and notify that data set changed
     * Let it be deprecated until it's fixed
     */
    @Deprecated
    public static class FixAttachmentsLinks extends AsyncTask<Object, Object, Object> {
        Post post;
        PostsAdapter adapter;

        public FixAttachmentsLinks(Post post, PostsAdapter adapter) {
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

                    } catch (TokenExpiredException e) {
                        Intent message = makeError(Broadcasts.INTERNAL_MESSAGE, Broadcasts.EXPIRED_TOKEN,
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
                                Loudly.getPostHolder().merge(result.first, result.second);
                            }
                        });

                        successfullyLoaded.add(result.second);
                    }
                }
            }, wraps);

            MainActivity.executeOnUI(new UIAction<MainActivity>() {
                @Override
                public void execute(MainActivity context, Object... params) {
                    Loudly.getPostHolder().cleanUp(successfullyLoaded, true);
                }
            });

            return makeSuccess(Broadcasts.POST_LOAD);
        }
    }
}
