package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.multiple.LoudlyImage;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.NetworkContract;
import rx.Observable;
import solid.collections.SolidList;

public class PostUploadModel {
    @NonNull
    private final CoreModel coreModel;

    @NonNull
    private final PostsDatabaseModel postsDatabaseModel;

    @NonNull
    private final InfoUpdateModel infoUpdateModel;

    public PostUploadModel(@NonNull CoreModel coreModel,
                           @NonNull PostsDatabaseModel postsDatabaseModel,
                           @NonNull InfoUpdateModel infoUpdateModel) {
        this.coreModel = coreModel;
        this.postsDatabaseModel = postsDatabaseModel;
        this.infoUpdateModel = infoUpdateModel;
    }

    @CheckResult
    @NonNull
    private Observable<List<SingleAttachment>> uploadAttachments(
            @NonNull SolidList<Attachment> attachments,
            @NonNull NetworkContract networkContract) {

        // ToDo: Handle error
        return Observable
                .from(attachments)
                .filter(attachment -> attachment instanceof PlainImage)
                .flatMap(attachment ->
                                networkContract
                                        .upload((PlainImage) attachment)
                                        .cast(SingleAttachment.class)
                )
                .toList();
    }

    @CheckResult
    @NonNull
    private Observable<SinglePost> uploadPost(@Nullable String post,
                                              @NonNull SolidList<Attachment> attachments,
                                              @NonNull NetworkContract network) {
        return uploadAttachments(attachments, network)
                .<PlainPost>map(list -> new PlainPost<>(
                        post,
                        System.currentTimeMillis(),
                        new ArrayList<>(list),
                        null
                ))
                .flatMap(network::upload);
    }

    public Observable<LoudlyPost> uploadPost(@Nullable String text,
                                             @NonNull SolidList<Attachment> attachments,
                                             @NonNull List<NetworkContract> networks) {

        ArrayList<MultipleAttachment> multipleAttachments = new ArrayList<>();
        for (Attachment attachment : attachments) {
            if (attachment instanceof PlainImage) {
                PlainImage image = (PlainImage) attachment;
                multipleAttachments.add(new LoudlyImage(image.getUrl(), image.getSize()));
            }
        }
        LoudlyPost initial = new LoudlyPost(
                text,
                System.currentTimeMillis(),
                multipleAttachments,
                null
        );

        return postsDatabaseModel
                .putPost(initial)
                .flatMap(loudlyPost -> infoUpdateModel
                                .subscribeOnFrequentUpdates(loudlyPost)
                                .toSingleDefault(loudlyPost)
                )
                .flatMapObservable(loudlyPost ->
                                Observable
                                        .from(networks)
                                        .flatMap(networkContract ->
                                                uploadPost(text, attachments, networkContract))
                                        .scan(loudlyPost, LoudlyPost::setSingleNetworkInstance)
                )
                .flatMap(loudlyPost ->
                        postsDatabaseModel.updatePostLinks(loudlyPost).toObservable());
    }
}
