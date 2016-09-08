package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;

import javax.inject.Inject;
import javax.inject.Named;

import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.networks.facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.vk.VKKeyKeeper;
import ly.loud.loudly.util.database.DatabaseException;
import ly.loud.loudly.util.database.entities.Key;
import rx.Completable;
import rx.Single;
import rx.exceptions.Exceptions;

import static ly.loud.loudly.networks.Networks.FB;
import static ly.loud.loudly.networks.Networks.VK;

public class KeysModel {
    @NonNull
    private StorIOSQLite keysDatabase;

    @Nullable
    private VKKeyKeeper vkKeyKeeper;

    @Nullable
    private FacebookKeyKeeper facebookKeyKeeper;

    @Inject
    public KeysModel(@NonNull @Named("keys") StorIOSQLite keysDatabase) {
        this.keysDatabase = keysDatabase;
    }

    @CheckResult
    @Nullable
    public VKKeyKeeper getVKKeyKeeper() {
        return vkKeyKeeper;
    }

    public void setVKKeyKeeper(@Nullable VKKeyKeeper keyKeeper) {
        vkKeyKeeper = keyKeeper;
    }

    @CheckResult
    @Nullable
    public FacebookKeyKeeper getFacebookKeyKeeper() {
        return facebookKeyKeeper;
    }

    public void setFacebookKeyKeeper(@Nullable FacebookKeyKeeper facebookKeyKeeper) {
        this.facebookKeyKeeper = facebookKeyKeeper;
    }

    @CheckResult
    @NonNull
    public Completable loadKeys() {
        return keysDatabase
                .get()
                .listOfObjects(Key.class)
                .withQuery(Key.selectAll())
                .prepare()
                .asRxSingle()
                .map(list -> {
                    for (Key key : list) {
                        int network = key.getNetwork();
                        setKeyKeeperInMemory(network,
                                KeyKeeper.fromStringBundle(network, key.getValue()));
                    }
                    return null;
                })
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Completable putKeyKeeper(@Network int network, @NonNull KeyKeeper keyKeeper) {
        return keysDatabase
                .put()
                .object(new Key(network, keyKeeper.toStringBundle()))
                .prepare()
                .asRxSingle()
                .map(result -> {
                    if (!result.wasInserted() && !result.wasUpdated()) {
                        throw Exceptions.propagate(new DatabaseException("Can't update keys"));
                    }
                    return null;
                })
                .toCompletable();
    }

    @CheckResult
    @NonNull
    public Completable deleteStoredKeyKeeper(@Network int network) {
        return keysDatabase
                .delete()
                .byQuery(Key.deleteByNetwork(network))
                .prepare()
                .asRxSingle()
                .map(deleteResult -> {
                    if (deleteResult.numberOfRowsDeleted() == 0) {
                        throw Exceptions.propagate(new DatabaseException("Can't delete keys"));
                    }
                    return null;
                })
                .toCompletable();
    }

    @CheckResult
    @NonNull
    public Completable setKeyKeeper(@Network int network, @NonNull KeyKeeper keyKeeper) {
        return putKeyKeeper(network, keyKeeper)
                .andThen(Completable.fromAction(() -> setKeyKeeperInMemory(network, keyKeeper)));
    }

    private void setKeyKeeperInMemory(@Network int network, @Nullable KeyKeeper keyKeeper) {
        switch (network) {
            case FB:
                setFacebookKeyKeeper((FacebookKeyKeeper) keyKeeper);
                return;
            case VK:
                setVKKeyKeeper(((VKKeyKeeper) keyKeeper));
                return;
        }
    }
}
