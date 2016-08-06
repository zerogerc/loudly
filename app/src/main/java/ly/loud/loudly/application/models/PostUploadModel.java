package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.base.multiple.LoudlyImage;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.util.database.DatabaseUtils;
import rx.Observable;
import solid.collections.SolidList;

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
    private Observable<SinglePost> uploadPost(@Nullable String post,
                                              @NonNull SolidList<Attachment> attachments,
                                              @NonNull NetworkContract network) {
        return uploadAttachments(attachments, network)
                .flatMap(list -> {
                    PlainPost<SingleAttachment> plainPost =
                            new PlainPost<>(post, System.currentTimeMillis(), new ArrayList<>(list), null);
                    return network.upload(plainPost);
                });
    }

    @CheckResult
    @NonNull
    private Observable<List<SingleAttachment>> uploadAttachments(
            @NonNull SolidList<Attachment> attachments,
            @NonNull NetworkContract networkContract) {

        // ToDo: Handle error
        return Observable.from(attachments)
                .filter(attachment -> attachment instanceof PlainImage)
                .flatMap(attachment -> networkContract.upload(((PlainImage) attachment))
                        .map(uploaded -> ((SingleAttachment) uploaded)))
                .toList();
    }

    public Observable<LoudlyPost> uploadPost(@Nullable String text,
                                             @NonNull SolidList<Attachment> attachments,
                                             @NonNull List<NetworkContract> networks) {

        return Observable.from(networks)
                .flatMap(networkContract -> uploadPost(text, attachments, networkContract))
                .toList()
                .flatMap(singlePosts ->
                        Observable.fromCallable(() -> {
                            ArrayList<MultipleAttachment> images = new ArrayList<>();
                            for (Attachment attachment : attachments) {
                                if (attachment instanceof PlainImage) {
                                    PlainImage image = ((PlainImage) attachment);
                                    images.add(new LoudlyImage(image.getUrl(), image.getSize()));
                                }
                            }

                            for (SinglePost post : singlePosts) {
                                ArrayList<SingleAttachment> singles = post.getAttachments();
                                // ToDo: fix dependency of uploaded length
                                for (int i = 0; i < singles.size(); i++) {
                                    SingleAttachment single = singles.get(i);

                                    MultipleNetworkElement<SingleAttachment> element =
                                            images.get(i).setSingleNetworkInstance(single.getNetwork(), single);
                                    images.set(i, ((MultipleAttachment) element));
                                }
                            }

                            LoudlyPost loudlyPost = new LoudlyPost(text, System.currentTimeMillis(),
                                    images, null);
                            for (SinglePost post : singlePosts) {
                                loudlyPost = loudlyPost.setSingleNetworkInstance(post.getNetwork(), post);
                            }
                            return DatabaseUtils.savePost(loudlyPost);
                        }));
    }
}
