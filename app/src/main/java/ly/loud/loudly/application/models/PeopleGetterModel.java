package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.Wrap;
import rx.Single;
import rx.schedulers.Schedulers;

/**
 * Model for loading persons from different networks.
 * For example: get likes to post.
 */
public class PeopleGetterModel {

    @NonNull
    private Loudly loudlyApplication;

    public PeopleGetterModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @NonNull
    private List<PersonsFromNetwork> getListPersonsByType(SingleNetwork element, int type, Wrap... networkWraps) {
        ArrayList<Wrap> goodWraps = new ArrayList<>();
        for (Wrap w : networkWraps) {
            if (element.existsIn(w.networkID())) {
                goodWraps.add(w);
            }
        }

        List<PersonsFromNetwork> result = new ArrayList<>();
        for (Wrap wrap : goodWraps) {
            try {
                result.add(new PersonsFromNetwork(
                        wrap.getPersons(
                                type,
                                element.getNetworkInstance(wrap.networkID())),
                        wrap.networkID()
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @NonNull
    public Single<List<PersonsFromNetwork>> getPersonsByType(@NonNull SingleNetwork element,
                                                             int type) {
        Wrap[] networkWraps = loudlyApplication.getWraps();
        return Single.fromCallable(() -> getListPersonsByType(element, type, networkWraps))
                .subscribeOn(Schedulers.io());
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
