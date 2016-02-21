package ly.loud.loudly;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import base.Tasks;
import base.Wrap;
import base.says.Info;
import base.says.Post;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.InvalidTokenException;
import util.ThreadStopped;
import util.UIAction;
import util.Utils;

public class GetInfoService extends IntentService {
    private static final String NAME = "GetInfoService";
    private static final int NOTIFICATION_ID = 0;
    private static volatile boolean stopped;
    private Info summary;

    public GetInfoService() {
        super(NAME);
        stopped = false;
    }

    public static void stop() {
        stopped = true;
    }

    private String[] makeNewInfoMessages(Info summary) {
        String message = "New";
        String longMessage = "You've got";
        if (summary.like > 0) {
            message += " likes,";
            longMessage += " " + summary.like + " new like" +
                    ((summary.like > 1) ? "s" : "") + ",";
        }
        if (summary.repost > 0) {
            message += " shares,";
            longMessage += " " + summary.repost + " new repost" +
                    ((summary.repost > 1) ? "s" : "") + ",";
        }
        if (summary.comment > 0) {
            message += " comments,";
            longMessage += " " + summary.comment + " new comment" +
                    ((summary.comment > 1) ? "s" : "") + ",";
        }
        longMessage = longMessage.substring(0, longMessage.length() - 1);
        message = message.substring(0, message.length() - 1);
        return new String[]{message, longMessage};
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Loudly.getExecutor().getQueue().size() > 0) {
            // If executor is busy, get info later
            // TODO: get info for networks which aren't in use

            Loudly.getContext().startGetInfoService();  // Restarting
            return;
        }

        stopped = false;
        if (MainActivity.posts.isEmpty()) {
            return;
        }

        summary = new Info();
        final LinkedList<Integer> success = new LinkedList<>();

        Tasks.doAndWait(MainActivity.posts, new Tasks.ActionWithWrap<LinkedList<Post>, Pair<List<Pair<Post, Info>>, Integer>>() {
            @Override
            public Pair<List<Pair<Post, Info>>, Integer> apply(LinkedList<Post> item, Wrap w) {
                ArrayList<Post> current = new ArrayList<>();
                for (Post p : item) {
                    if (p.existsIn(w.networkID())) {
                        current.add((Post)p.getNetworkInstance(w.networkID()));
                    }
                }
                try {
                    return new Pair<>(w.getPostsInfo(current), w.networkID());
                } catch (InvalidTokenException e) {
                    Intent message = BroadcastSendingTask.makeError(Broadcasts.POST_GET_INFO,
                            Broadcasts.INVALID_TOKEN, e.getMessage());
                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                    Loudly.sendLocalBroadcast(message);
                } catch (IOException e) {
                    Log.e(NAME, e.getMessage(), e);
                    Loudly.sendLocalBroadcast(BroadcastSendingTask.makeError(Broadcasts.POST_GET_INFO,
                            Broadcasts.NETWORK_ERROR, e.getMessage()));
                }
                return null;
            }
        }, new Tasks.ActionWithResult<Pair<List<Pair<Post, Info>>, Integer>>() {
            @Override
            public void apply(final Pair<List<Pair<Post, Info>>, Integer> result) {
                if (result != null && result.first != null) {
                    MainActivity.executeOnUI(new UIAction<MainActivity>() {
                        @Override
                        public void execute(MainActivity context, Object... params) {
                            summary.add(context
                                    .mainActivityPostsAdapter
                                    .updateInfo(result.first, result.second));

                            Intent message = BroadcastSendingTask.makeMessage(Broadcasts.POST_GET_INFO,
                                    Broadcasts.PROGRESS);
                            message.putExtra(Broadcasts.NETWORK_FIELD, result.second);
                            Loudly.sendLocalBroadcast(message);
                        }
                    });
                    if (result.first.size() > 0) {
                        success.add(result.second);
                    }
                }
            }
        }, Loudly.getContext().getWraps());

        if (success.size() > 0) {
            MainActivity.executeOnUI(new UIAction<MainActivity>() {
                @Override
                public void execute(MainActivity context, Object... params) {
                    context.mainActivityPostsAdapter.cleanUp(success);
                }
            });
        }
        Loudly.sendLocalBroadcast(BroadcastSendingTask.makeSuccess(Broadcasts.POST_GET_INFO));
        if (summary.hasPositiveChanges()) {
            String[] strings = makeNewInfoMessages(summary);
            if (MainActivity.aliveCopy == 0 && SettingsActivity.aliveCopy == 0) {
                Utils.makeNotification(this, strings[0], strings[1], NOTIFICATION_ID);
            } else {
                Utils.showSnackBar(strings[1]);
            }
        }

        Loudly.getContext().startGetInfoService();  // Restarting
    }
}
