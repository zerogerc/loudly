package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.Networks.Network;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import rx.Observable;

/**
 * Model for loading persons from different networks.
 * For example: get likes to post.
 */
public class PeopleGetterModel {

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
    public PeopleGetterModel(
            @NonNull Loudly loudlyApplication,
            @NonNull CoreModel coreModel
    ) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
    }

    @CheckResult
    @NonNull
    private Observable<PersonsFromNetwork> getListPersonsByType(@NonNull SingleNetwork element, @RequestType int type) {

        ArrayList<NetworkContract> availableModels = new ArrayList<>();

        for (NetworkContract model : coreModel.getNetworksModels()) {
            if (element.existsIn(model.getId())) {
                availableModels.add(model);
            }
        }

        Observable<PersonsFromNetwork>[] peoples = ((Observable<PersonsFromNetwork>[]) new Observable[availableModels.size()]);
        for (int i = 0; i < peoples.length; i++) {
            final NetworkContract model = availableModels.get(i);
            peoples[i] = model.getPersons(element, type)
                    .map(persons -> new PersonsFromNetwork(persons, model.getId()))
                    .toObservable();

        }
        return Observable.merge(peoples);
    }

    @CheckResult
    @NonNull
    public Observable<PersonsFromNetwork> getPersonsByType(@NonNull SingleNetwork element,
                                                           @RequestType int type) {
        return getListPersonsByType(element, type);
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
