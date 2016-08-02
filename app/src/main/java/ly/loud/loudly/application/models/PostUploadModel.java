package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.new_base.SinglePost;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainPost;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class PostUploadModel {
    @NonNull
    private Loudly loudlyApplication;
    @NonNull
    private CoreModel coreModel;

    public PostUploadModel(@NonNull Loudly loudlyApplication, @NonNull CoreModel coreModel) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
    }

    // Save to DB after uploading to networks
    @CheckResult
    @NonNull
    public Observable<SinglePost> uploadPost(@NonNull PlainPost<SingleAttachment> post, @NonNull List<NetworkContract> networks) {
        return Observable.from(networks)
                .flatMap(network -> network.upload(post).toObservable());
    }
}
