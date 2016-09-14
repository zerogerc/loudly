package ly.loud.loudly.ui.full_post;

import android.support.annotation.NonNull;

import java.util.List;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.util.BasePresenter;
import ly.loud.loudly.util.Utils;
import rx.Observable;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class FullPostInfoPresenter extends BasePresenter<FullPostInfoView> {

    @NonNull
    private GetterModel getterModel;

    public FullPostInfoPresenter(
            @NonNull Loudly loudlyApplication,
            @NonNull GetterModel getterModel
    ) {
        super(loudlyApplication);
        this.getterModel = getterModel;
    }

    public void loadComments(@NonNull PlainPost post) {
        List<SinglePost> instances = Utils.getInstances(post);
        unsubscribeOnUnbindView(Observable.from(instances)
                        .flatMap(getterModel::getComments)
                        .subscribeOn(io())
                        .observeOn(mainThread())
                        .subscribe(
                                result -> executeIfViewBound(
                                        view -> view.onNewCommentsFromNetwork(
                                                result.comments,
                                                result.network
                                        )
                                ),
                                error -> executeIfViewBound(
                                        view -> view.onError(R.string.comments_load_error)
                                )
                        )
        );
    }

    public void getUserPageUrl(@NonNull Person person) {
        String pageUrl = getterModel.getPersonPageUrl(person);
        if (!pageUrl.isEmpty()) {
            executeIfViewBound(view -> view.onGotWebPageUrl(pageUrl));
        }
    }

    public void getCommentPageUrl(@NonNull Comment comment, @NonNull PlainPost plainPost) {
        unsubscribeOnUnbindView(getterModel
                .getCommentUrl(comment, plainPost)
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(url -> {
                            if (!url.isEmpty()) {
                                executeIfViewBound(view -> view.onGotWebPageUrl(url));
                            }
                        }
                ));
    }
}
