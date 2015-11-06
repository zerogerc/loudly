package VK;

import android.os.Parcel;
import android.util.Log;

import base.Authorizer;
import base.ListenerHolder;
import base.ResponseListener;


public class VKAuthorizer extends Authorizer<VKWrap, VKKeyKeeper> {
    private static final String ACCESS_TOKEN = "#access_token=";
    private static final String ERROR_DESCRIPTION = "#error_description=";

    private static final String TAG = "VK_AUTH_TAG";
    public static final Creator<VKAuthorizer> CREATOR = new Creator<VKAuthorizer>() {
        @Override
        public VKAuthorizer createFromParcel(Parcel source) {
            return new VKAuthorizer();
        }

        @Override
        public VKAuthorizer[] newArray(int size) {
            return new VKAuthorizer[size];
        }
    };

    @Override
    protected VKKeyKeeper beginAuthorize() {
        return new VKKeyKeeper();
    }

    @Override
    public void continueAuthorization(String url, VKKeyKeeper keys) {
        ResponseListener<VKWrap> listener = ListenerHolder.getListener();
        Log.e(TAG, url);
        if (url.contains(ACCESS_TOKEN)) {
            int left = url.indexOf(ACCESS_TOKEN);
            int right = left;
            while (url.charAt(right) != '&') {
                right++;
            }

            String accessToken = url.substring(left + ACCESS_TOKEN.length());
            Log.d(TAG, accessToken);

            keys.setAccessToken(accessToken);
            listener.onSuccess(new VKWrap(keys));
        } else if (url.contains(ERROR_DESCRIPTION)){
            int left = url.indexOf(ERROR_DESCRIPTION);
            int right = left;
            while (url.charAt(right) != '&') {
                right++;
            }

            String errorToken = url.substring(left + ERROR_DESCRIPTION.length(), right);
            Log.d(TAG, errorToken);
            listener.onFail(errorToken);
        } else {
            Log.d(TAG, "WTF?");
            listener.onFail("Bad VK response");
        }
    }

    @Override
    protected String getAuthUrl() {
        return "https://oauth.vk.com/authorize?client_id=" + VKKeyKeeper.CLIENT_ID
                + "&redirect_uri=https://oauth.vk.com/blank.html&display_type=mobile&response_type=token";
    }
}
