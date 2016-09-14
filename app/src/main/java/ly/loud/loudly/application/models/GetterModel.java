package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.exceptions.FatalNetworkException;
import ly.loud.loudly.base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import rx.Observable;
import rx.subjects.PublishSubject;
import solid.collections.SolidList;

import static ly.loud.loudly.util.RxUtils.retry3TimesAndFail;

/**
 * Model for loading persons from different networks.
 * For example: get likes to post.
 */
public class GetterModel {

    @IntDef({LIKES, SHARES})
    public @interface RequestType {
    }

    public static final int LIKES = 0;
    public static final int SHARES = 1;

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private CoreModel coreModel;

    @NonNull
    private final PublishSubject<Pair<SingleNetworkElement, Throwable>> getErrors;

    @NonNull
    private final PublishSubject<Pair<SingleNetworkElement, Throwable>> getCommentsErrors;

    @Inject
    public GetterModel(
            @NonNull Loudly loudlyApplication,
            @NonNull CoreModel coreModel
    ) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
        getErrors = PublishSubject.create();
        getCommentsErrors = PublishSubject.create();
    }

    @CheckResult
    @NonNull
    public Observable<Pair<SingleNetworkElement, Throwable>> observeGetErrors() {
        return getErrors.asObservable();
    }

    @CheckResult
    @NonNull
    public Observable<Pair<SingleNetworkElement, Throwable>> observeGetCommentsErrors() {
        return getErrors.asObservable();
    }

    @CheckResult
    @NonNull
    private Observable<PersonsFromNetwork> safeGetPersonsByType(@NonNull SingleNetworkElement element,
                                                                @RequestType int type,
                                                                @NonNull NetworkContract networkContract) {
        return retry3TimesAndFail(
                networkContract
                        .getPersons(element, type)
                        .map(persons -> new PersonsFromNetwork(persons, networkContract.getId())),
                new FatalNetworkException(networkContract.getId())
        )
                .doOnError(error -> getErrors.onNext(new Pair<>(element, error)))
                .onErrorResumeNext(Observable.empty());
    }

    @CheckResult
    @NonNull
    private Observable<CommentsFromNetwork> safeGetComments(@NonNull SingleNetworkElement element,
                                                            @NonNull NetworkContract networkContract) {
        return retry3TimesAndFail(
                networkContract
                        .getComments(element)
                        .map(comments -> new CommentsFromNetwork(comments, networkContract.getId())),
                new FatalNetworkException(networkContract.getId())
        )
                .doOnError(error -> getCommentsErrors.onNext(new Pair<>(element, error)))
                .onErrorResumeNext(Observable.empty());
    }

    @CheckResult
    @NonNull
    public Observable<PersonsFromNetwork> getPersonsByType(@NonNull SingleNetworkElement element,
                                                           @RequestType int type) {
        return coreModel.elementExistsIn(element)
                .flatMap(networkContract -> safeGetPersonsByType(element, type, networkContract));
    }

    @CheckResult
    @NonNull
    public Observable<CommentsFromNetwork> getComments(@NonNull SingleNetworkElement element) {
        return coreModel.elementExistsIn(element)
                .flatMap(networkContract -> safeGetComments(element, networkContract));
    }

    @CheckResult
    @NonNull
    public Observable<CommentsFromNetwork> getComments(@NonNull MultipleNetworkElement<?> element) {
        return Observable.from(element.getNetworkInstances())
                .flatMap(this::getComments);
    }

    @CheckResult
    @NonNull
    public Observable<PersonsFromNetwork> getPersonsByType(@NonNull MultipleNetworkElement<?> element,
                                                           @RequestType int requestType) {
        return Observable.from(element.getNetworkInstances())
                .flatMap(instance -> getPersonsByType(instance, requestType));
    }

    public class CommentsFromNetwork {
        @NonNull
        public SolidList<Comment> comments;

        @Network
        public int network;

        public CommentsFromNetwork(@NonNull SolidList<Comment> comments, @Network int network) {
            this.comments = comments;
            this.network = network;
        }
    }

    public class PersonsFromNetwork {
        @NonNull
        public List<Person> persons;

        @Network
        public int network;

        public PersonsFromNetwork(@NonNull List<Person> persons, @Network int network) {
            this.persons = persons;
            this.network = network;
        }
    }
}
