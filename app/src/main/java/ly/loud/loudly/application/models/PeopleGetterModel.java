package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import rx.Single;

/**
 * Model for loading persons from different networks.
 * For example: get likes to post.
 */
public class PeopleGetterModel {

    @IntDef
    public @interface RequestType {}
    public static final int LIKES = 0;
    public static final int SHARES = 1;

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private CoreModel coreModel;

    public PeopleGetterModel(
            @NonNull Loudly loudlyApplication,
            @NonNull CoreModel coreModel
    ) {
        this.loudlyApplication = loudlyApplication;
        this.coreModel = coreModel;
    }

    @CheckResult
    @NonNull
    private List<PersonsFromNetwork> getListPersonsByType(@NonNull SingleNetwork element, @RequestType int type) {

        ArrayList<NetworkContract> availableModels = new ArrayList<>();

        for (NetworkContract model : coreModel.getNetworksModels()) {
            if (element.existsIn(model.getId())) {
                availableModels.add(model);
            }
        }

        List<PersonsFromNetwork> result = new ArrayList<>();

        for (NetworkContract model : availableModels) {
            result.add(new PersonsFromNetwork(
                    model.getPersons(
                            element,
                            type),
                    model.getId()
            ));

        }
        return result;
    }

    @CheckResult
    @NonNull
    public Single<List<PersonsFromNetwork>> getPersonsByType(@NonNull SingleNetwork element,
                                                             @RequestType int type) {
        return Single.fromCallable(() -> getListPersonsByType(element, type));
    }

    public class PersonsFromNetwork {

        @NonNull
        public List<Person> persons;

        public int network;

        public PersonsFromNetwork(@NonNull List<Person> persons, int network) {
            this.persons = persons;
            this.network = network;
        }
    }
}
