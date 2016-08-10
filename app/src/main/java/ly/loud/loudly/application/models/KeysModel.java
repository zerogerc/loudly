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
import ly.loud.loudly.util.database.entities.Key;
import rx.Single;

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
    public Single<Boolean> loadKeys() {
        return keysDatabase
                .get()
                .listOfObjects(Key.class)
                .withQuery(Key.selectAll())
                .prepare()
                .asRxObservable()
                .map(list -> {
                    for (Key key : list) {
                        int network = key.getNetwork();
                        setKeyKeeperInMemory(network,
                                KeyKeeper.fromStringBundle(network, key.getValue()));
                    }
                    return true;
                })
                .first()
                .toSingle();
    }

    @CheckResult
    @NonNull
    private Single<Boolean> putKeyKeeper(@Network int network, @NonNull KeyKeeper keyKeeper) {
        return keysDatabase
                .put()
                .object(new Key(network, keyKeeper.toStringBundle()))
                .prepare()
                .asRxObservable()
                .map(result -> result.wasInserted() || result.wasUpdated())
                .first()
                .toSingle();
    }

    @CheckResult
    @NonNull
    private Single<Boolean> deleteStoredKeyKeeper(@Network int network) {
        return keysDatabase
                .delete()
                .byQuery(Key.deleteByNetwork(network))
                .prepare()
                .asRxObservable()
                .map(deleteResult -> deleteResult.numberOfRowsDeleted() > 0)
                .first()
                .toSingle();
    }

    @CheckResult
    @NonNull
    public Single<Boolean> setKeyKeeper(@Network int network, @NonNull KeyKeeper keyKeeper) {
        return putKeyKeeper(network, keyKeeper)
                .map(result -> {
                    if (result) {
                        setKeyKeeperInMemory(network, keyKeeper);
                    }
                    return result;
                });
    }

    @CheckResult
    @NonNull
    public Single<Boolean> deleteKeyKeeper(@Network int network) {
        return deleteStoredKeyKeeper(network)
                .map(result -> {
                    if (result) {
                        setKeyKeeperInMemory(network, null);
                    }
                    return result;
                });
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
