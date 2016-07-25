package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.Networks.Network;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.says.Comment;
import rx.Observable;

/**
 * Model for loading persons from different networks.
 * For example: get likes to post.
 */
public class GetterModel {

    @IntDef
    public @interface RequestType {
    }

    public static final int LIKES = 0;
    public static final int SHARES = 1;

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private CoreModel coreModel;

    @Inject
    public GetterModel(
            @NonNull Loudly loudlyApplication,
            @NonNull CoreModel coreModel
    ) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
    }

    @CheckResult
    @NonNull
    public Observable<PersonsFromNetwork> getPersonsByType(@NonNull SingleNetwork element, @RequestType int type) {
        return coreModel.elementExistsIn(element)
                .flatMap(model ->
                        model.getPersons(element, type)
                                .map(persons -> new PersonsFromNetwork(persons, model.getId()))
                                .toObservable()
                );
    }

    @CheckResult
    @NonNull
    public Observable<CommentsFromNetwork> getComments(@NonNull SingleNetwork element) {
        return coreModel.elementExistsIn(element)
                .flatMap(model ->
                        model.getComments(element)
                                .map(comments -> new CommentsFromNetwork(comments, model.getId()))
                                .toObservable()
                );
    }

    public class CommentsFromNetwork {
        @NonNull
        public List<Comment> comments;

        public int network;

        public CommentsFromNetwork(@NonNull List<Comment> comments, int network) {
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
