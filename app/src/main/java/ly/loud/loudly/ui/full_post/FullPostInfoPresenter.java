package ly.loud.loudly.ui.full_post;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ly.loud.loudly.R;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.BasePresenter;
import ly.loud.loudly.util.Utils;
import rx.Observable;
import rx.Subscription;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class FullPostInfoPresenter extends BasePresenter<FullPostInfoView> {

    @Nullable
    private Subscription commentsLoading;

    @NonNull
    private GetterModel getterModel;

    public FullPostInfoPresenter(@NonNull GetterModel getterModel) {
        this.getterModel = getterModel;
    }

    @Override
    public void attachView(@NonNull FullPostInfoView view) {
        super.attachView(view);
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (commentsLoading != null && !commentsLoading.isUnsubscribed()) {
            commentsLoading.unsubscribe();
        }
    }

    public void loadComments(@NonNull PlainPost post) {
        List<SinglePost> instances = Utils.getInstances(post);
        commentsLoading = Observable.from(instances)
                .flatMap(instance -> getterModel.getComments(instance))
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
                );
    }

}
