package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;
import solid.collections.SolidList;

import static solid.collectors.ToList.toList;
import static solid.collectors.ToSolidList.toSolidList;

/**
 * Model for post loading
 *
 * @author Danil Kolikov
 */
public class PostLoadModel {
    @NonNull
    private CoreModel coreModel;

    @NonNull
    private PostsDatabaseModel postsDatabaseModel;

    @Inject
    public PostLoadModel(@NonNull CoreModel coreModel, @NonNull PostsDatabaseModel databaseModel) {
        this.postsDatabaseModel = databaseModel;
        this.coreModel = coreModel;
    }

    @NonNull
    private static SolidList<PlainPost> merge(
            @NonNull SolidList<PlainPost> oldList,
            @NonNull SolidList<SinglePost> newList) {
        // Set new instances to LoudlyPosts
        SolidList<PlainPost> withInstances = oldList.map(post -> {
            if (post instanceof LoudlyPost) {
                LoudlyPost loudlyPost = ((LoudlyPost) post);
                return newList
                        // Filter posts, which has same IDs as loudlyPost
                        .filter(single -> {
                            SinglePost instance = loudlyPost
                                    .getSingleNetworkInstance(single.getNetwork());
                            return instance != null && instance.getLink().equals(single.getLink());
                        }) // And set this posts as instances
                        .reduce(loudlyPost, LoudlyPost::setSingleNetworkInstance);
            }
            return post;
        }).collect(toSolidList());
        // Drop post which instances were set
        List<SinglePost> notSet = newList.filter(post ->
                        !withInstances.any(otherPost -> (otherPost instanceof LoudlyPost) &&
                                ((LoudlyPost) otherPost)
                                        .getSingleNetworkInstance(post.getNetwork()) == post)
        ).collect(toList());
        // Merge this two lists
        List<PlainPost> result = new ArrayList<>();
        result.addAll(withInstances);
        result.addAll(notSet);
        Collections.sort(result);
        return ListUtils.asSolidList(result);
    }

    /**
     * Get list posts to show
     *
     * @param interval Interval to show posts
     * @return Observable, containing lists of post to show. First list in stream contains
     * posts from DB, next contains previous list merged with posts from some new network
     */
    @CheckResult
    @NonNull
    public Observable<SolidList<PlainPost>> loadPosts(@NonNull TimeInterval interval) {
        return postsDatabaseModel.selectPostsByTimeInterval(interval)
                .flatMap(list -> coreModel.getConnectedNetworksModels()
                        .flatMap(model -> model.loadPosts(interval))
                        .scan(list, PostLoadModel::merge));
    }
}
