package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.exceptions.FatalNetworkException;
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
import rx.subjects.PublishSubject;
import solid.collections.SolidList;

import static ly.loud.loudly.util.ListUtils.asArrayList;
import static ly.loud.loudly.util.RxUtils.retry3TimesAndFail;

public class PostUploadModel {
    @NonNull
    private final CoreModel coreModel;

    @NonNull
    private final PostsDatabaseModel postsDatabaseModel;

    @NonNull
    private final InfoUpdateModel infoUpdateModel;

    @NonNull
    private final PublishSubject<Throwable> uploadErrors;

    public PostUploadModel(@NonNull CoreModel coreModel,
                           @NonNull PostsDatabaseModel postsDatabaseModel,
                           @NonNull InfoUpdateModel infoUpdateModel) {
        this.coreModel = coreModel;
        this.postsDatabaseModel = postsDatabaseModel;
        this.infoUpdateModel = infoUpdateModel;
        uploadErrors = PublishSubject.create();
    }

    @CheckResult
    @NonNull
    public Observable<Throwable> observeUploadErrors() {
        return uploadErrors.asObservable();
    }

    @CheckResult
    @NonNull
    private Observable<SingleAttachment> safeUploadAttachment(
            @NonNull Attachment attachment,
            @NonNull NetworkContract networkContract) {
        if (attachment instanceof PlainImage) {
            return retry3TimesAndFail(
                    networkContract
                            .upload((PlainImage) attachment)
                            .cast(SingleAttachment.class),
                    new FatalNetworkException(networkContract.getId())
            );
        }
        return Observable.empty();
    }

    @CheckResult
    @NonNull
    private Observable<List<SingleAttachment>> safeUploadAttachments(
            @NonNull List<Attachment> attachments,
            @NonNull NetworkContract networkContract) {
        return Observable.from(attachments)
                .flatMap(attachment -> safeUploadAttachment(attachment, networkContract))
                .toList();
    }

    @CheckResult
    @NonNull
    private Observable<SinglePost> safeUploadPost(@Nullable String text,
                                                  @NonNull List<SingleAttachment> attachments,
                                                  @NonNull NetworkContract networkContract) {
        final PlainPost<SingleAttachment> post = new PlainPost<>(
                text,
                System.currentTimeMillis(),
                asArrayList(attachments),
                null
        );
        return retry3TimesAndFail(
                networkContract.upload(post),
                new FatalNetworkException(networkContract.getId())
        )
                .doOnError(uploadErrors::onNext)
                .onErrorResumeNext(Observable.empty());
    }

    @CheckResult
    @NonNull
    private Observable<SinglePost> uploadPost(@Nullable String post,
                                              @NonNull SolidList<Attachment> attachments,
                                              @NonNull NetworkContract network) {
        return safeUploadAttachments(attachments, network)
                .flatMap(singleAttachments -> safeUploadPost(post, singleAttachments, network));
    }

    @CheckResult
    @NonNull
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
                .doOnError(uploadErrors::onNext)
                .flatMap(loudlyPost -> infoUpdateModel
                                .subscribeOnFrequentUpdates(loudlyPost)
                                .toSingleDefault(loudlyPost)
                )
                .toObservable()
                .onErrorResumeNext(Observable.empty())
                .flatMap(loudlyPost ->
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
