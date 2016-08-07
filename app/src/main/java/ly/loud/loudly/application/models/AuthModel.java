package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import rx.Observable;
import rx.Single;

/**
 * Model for authorizing in networks
 *
 */
public class AuthModel {
    @NonNull
    private Loudly loudlyApplcation;

    @NonNull
    private CoreModel coreModel;

    @NonNull
    private KeysModel keysModel;

    @Inject
    public AuthModel(@NonNull Loudly loudlyApplcation,
                     @NonNull CoreModel coreModel,
                     @NonNull KeysModel keysModel) {
        this.loudlyApplcation = loudlyApplcation;
        this.coreModel = coreModel;
        this.keysModel = keysModel;
    }

    @CheckResult
    @NonNull
    public Single<String> getAuthUrl(@Network int network) {
        NetworkContract contract = coreModel.getModelByNetwork(network);
        if (contract == null) {
            return Single.just("");
        }
        return contract.getBeginAuthUrl();
    }

    @CheckResult
    @NonNull
    public Single<Boolean> finishAuthorization(@NonNull Observable<String> urls,
                                               @Network int network) {
        NetworkContract contract = coreModel.getModelByNetwork(network);
        if (contract == null) {
            return Single.just(false);
        }
        return contract.proceedAuthUrls(urls)
                .flatMap(contract::connect);
    }

    @CheckResult
    @NonNull
    public Single<Boolean> logout(@Network int network) {
        NetworkContract contract = coreModel.getModelByNetwork(network);
        if (contract == null) {
            return Single.just(false);
        }
        return contract
                .disconnect()
                .map(success -> {
                    if (success) {
                        keysModel.disconnectFromNetwork(network);
                    }
                    return success;
                });
    }
}
