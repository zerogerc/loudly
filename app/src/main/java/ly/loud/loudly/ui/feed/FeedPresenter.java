package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.BasePresenter;
import rx.Subscription;
import rx.schedulers.Schedulers;
import solid.collections.SolidList;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

// TODO: config
public class FeedPresenter extends BasePresenter<FeedView> {

    @NonNull
    private Loudly loudlyApp;

    @NonNull
    private GetterModel getterModel;

    @NonNull
    private PostDeleterModel deleterModel;

    @NonNull
    private PostLoadModel postLoadModel;

    private Subscription postLoadSubscription;

    public FeedPresenter(
            @NonNull Loudly loudlyApp,
            @NonNull PostLoadModel postLoadModel,
            @NonNull GetterModel getterModel,
            @NonNull PostDeleterModel deleterModel
    ) {
        this.loudlyApp = loudlyApp;
        this.postLoadModel = postLoadModel;
        this.getterModel = getterModel;
        this.deleterModel = deleterModel;
    }

    public SolidList<PlainPost> getCachedPosts() {
        return postLoadModel.getCachedPosts();
    }

    public void updatePosts() {
        postLoadSubscription = postLoadModel
                .loadPosts(Loudly.getContext().getTimeInterval())
                .subscribeOn(io())
                .observeOn(mainThread())
                .doOnNext(list -> executeIfViewBound(view -> view.onPostsUpdated(list)))
                .subscribe();
    }

    public void unsubscribeAll() {
        if (!postLoadSubscription.isUnsubscribed()) {
            postLoadSubscription.unsubscribe();
        }
    }

    public void deletePost(@NonNull PlainPost post) {
        if (post instanceof LoudlyPost) {
            deleterModel
                    .deletePostFromAllNetworks(((LoudlyPost) post))
                    .subscribeOn(io())
                    .observeOn(mainThread())
                    .subscribe();
        }
    }

}
