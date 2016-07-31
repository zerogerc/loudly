package ly.loud.loudly.ui;

import android.app.IntentService;
import android.content.Intent;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.util.BroadcastSendingTask;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.Utils;

import java.util.LinkedList;

// ToDo: It's totally not thread-safe
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
        stopped = false;
        if (Loudly.getPostHolder().getPosts().isEmpty()) {
            return;
        }

        summary = new Info();
        final LinkedList<Integer> success = new LinkedList<>();

//        Tasks.doAndWait(Loudly.getPostHolder().getPosts(), new Tasks.ActionWithWrap<List<Post>, Pair<List<Pair<Post, Info>>, Integer>>() {
//            @Override
//            public Pair<List<Pair<Post, Info>>, Integer> apply(List<Post> item, Wrap w) {
//                ArrayList<Post> current = new ArrayList<>();
//                for (Post p : item) {
//                    if (p.existsIn(w.networkID())) {
//                        current.add((Post)p.getNetworkInstance(w.networkID()));
//                    }
//                }
//                try {
//                    return new Pair<>(w.getPostsInfo(current), w.networkID());
//                } catch (TokenExpiredException e) {
//                    Intent message = BroadcastSendingTask.makeError(Broadcasts.INTERNAL_MESSAGE,
//                            Broadcasts.EXPIRED_TOKEN, e.getMessage());
//                    message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
//                    Loudly.sendLocalBroadcast(message);
//                } catch (IOException e) {
//                    Log.e(NAME, e.getMessage(), e);
//                    Loudly.sendLocalBroadcast(BroadcastSendingTask.makeError(Broadcasts.POST_GET_INFO,
//                            Broadcasts.NETWORK_ERROR, e.getMessage()));
//                }
//                return null;
//            }
//        }, new Tasks.ActionWithResult<Pair<List<Pair<Post, Info>>, Integer>>() {
//            @Override
//            public void apply(final Pair<List<Pair<Post, Info>>, Integer> result) {
//                if (result != null && result.first != null) {
//                    MainActivity.executeOnUI(new UIAction<MainActivity>() {
//                        @Override
//                        public void execute(MainActivity context, Object... params) {
//                            summary.add(Loudly.getPostHolder().updateInfo(result, result.second));
//
//                            Intent message = BroadcastSendingTask.makeMessage(Broadcasts.POST_GET_INFO,
//                                    Broadcasts.PROGRESS);
//                            message.putExtra(Broadcasts.NETWORK_FIELD, result.second);
//                            Loudly.sendLocalBroadcast(message);
//                        }
//                    });
//                    if (result.first.size() > 0) {
//                        success.add(result.second);
//                    }
//                }
//            }
//        }, Loudly.getContext().getWraps());

        if (success.size() > 0) {
            MainActivity.executeOnUI(new UIAction<MainActivity>() {
                @Override
                public void execute(MainActivity context, Object... params) {
                    Loudly.getPostHolder().cleanUp(success, true);
                }
            });
        }
        Loudly.sendLocalBroadcast(BroadcastSendingTask.makeSuccess(Broadcasts.POST_GET_INFO));
        if (summary.hasPositiveChanges()) {
            String[] strings = makeNewInfoMessages(summary);
            if (Loudly.getCurrentActivity() == null) {
                Utils.makeNotification(this, strings[0], strings[1], NOTIFICATION_ID);
            } else {
                Utils.showSnackBar(strings[1]);
            }
        }

        Loudly.getContext().startGetInfoService();  // Restarting
    }
}
