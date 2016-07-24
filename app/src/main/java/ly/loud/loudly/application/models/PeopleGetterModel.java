package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.Networks.Network;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.Wrap;
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

    public PeopleGetterModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @CheckResult
    @NonNull
    private List<PersonsFromNetwork> getListPersonsByType(@NonNull SingleNetwork element, @RequestType int type) {
        Wrap[] networkWraps = loudlyApplication.getWraps();

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

    @CheckResult
    @NonNull
    public Single<List<PersonsFromNetwork>> getPersonsByType(@NonNull SingleNetwork element,
                                                             @RequestType int type) {
        return Single.fromCallable(() -> getListPersonsByType(element, type));
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
