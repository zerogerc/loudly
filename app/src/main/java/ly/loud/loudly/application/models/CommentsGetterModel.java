package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.base.says.Comment;
import rx.Single;

/**
 * Created by ZeRoGerc on 20/07/16.
 */
public class CommentsGetterModel {

    @NonNull
    private Loudly loudlyApplication;

    public CommentsGetterModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @CheckResult
    @NonNull
    private List<CommentsFromNetwork> getCommentsPrivate(@NonNull SingleNetwork element) {
        Wrap[] networkWraps = loudlyApplication.getWraps();

        ArrayList<Wrap> goodWraps = new ArrayList<>();
        for (Wrap w : networkWraps) {
            if (element.existsIn(w.networkID())) {
                goodWraps.add(w);
            }
        }

        List<CommentsFromNetwork> result = new ArrayList<>();
        for (Wrap wrap : goodWraps) {
            try {
                result.add(new CommentsFromNetwork(
                        wrap.getComments(element.getNetworkInstance(wrap.networkID())),
                        wrap.networkID()
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @CheckResult
    @NonNull
    public Single<List<CommentsFromNetwork>> getComments(@NonNull SingleNetwork element) {
        return Single.fromCallable(() -> getCommentsPrivate(element));
    }

    public class CommentsFromNetwork {

        @NonNull
        public List<Comment> comments;

        public int network;

        public CommentsFromNetwork(@NonNull List<Comment> comments, int network) {
            this.comments = comments;
            this.network = network;
        }
    }

}
