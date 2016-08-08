package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.database.DatabaseUtils;
import rx.Observable;
import solid.collections.SolidList;
import solid.collectors.ToList;

/**
 * Model for post loading
 *
 * @author Danil Kolikov
 */
public class PostLoadModel {
    @NonNull
    private Loudly loudlyApplication;
    @NonNull
    private CoreModel coreModel;

    @Inject
    public PostLoadModel(@NonNull Loudly loudlyApplication, @NonNull CoreModel coreModel) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
    }

    @NonNull
    private static SolidList<? extends PlainPost> merge(
            @NonNull SolidList<? extends PlainPost> oldList,
            @NonNull SolidList<SinglePost> newList) {
        List<PlainPost> withInstances = oldList.map(post -> {
            if (post instanceof LoudlyPost) {
                LoudlyPost loudlyPost = ((LoudlyPost) post);
                return newList.filter(single -> {
                    SinglePost instance = loudlyPost.getSingleNetworkInstance(single.getNetwork());
                    if (instance == null) {
                        return false;
                    }
                    return
                })
            }
            return post;
        }).collect(ToList.toList());
    }

    @CheckResult
    @NonNull
    public Observable<SolidList<? extends PlainPost>> loadPosts(@NonNull TimeInterval interval) {
        return Observable
                .fromCallable(() -> ListUtils.asSolidList(DatabaseUtils.loadPosts(interval)))
                .flatMap(list -> coreModel.getConnectedNetworksModels()
                        .flatMap(model -> model.loadPosts(interval))
                        .scan(list, PostLoadModel::merge));
    }
}
