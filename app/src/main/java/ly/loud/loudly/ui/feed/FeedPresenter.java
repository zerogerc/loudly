package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.BasePresenter;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

// TODO: config
public class FeedPresenter extends BasePresenter<FeedView> {

    @NonNull
    private Loudly loudlyApp;

    @NonNull
    private GetterModel getterModel;

    @NonNull
    private PostLoadModel postLoadModel;

    private Subscription postLoadSubscription;

    public FeedPresenter(
            @NonNull Loudly loudlyApp,
            @NonNull PostLoadModel postLoadModel,
            @NonNull GetterModel getterModel
    ) {
        this.loudlyApp = loudlyApp;
        this.postLoadModel = postLoadModel;
        this.getterModel = getterModel;
    }

    public void loadPosts() {
        postLoadSubscription = postLoadModel
                .loadPosts(Loudly.getContext().getTimeInterval())
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .doOnNext(list -> executeIfViewBound(view -> view.onNewLoadedPosts(list)))
                .subscribe();
    }

    public void unsubscribeAll() {
        if (!postLoadSubscription.isUnsubscribed()) {
            postLoadSubscription.unsubscribe();
        }
    }

    public void deletePost(@NonNull PlainPost post) {
        //TODO: delete post here
    }

}
