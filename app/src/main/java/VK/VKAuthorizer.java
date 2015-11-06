package VK;

import android.os.Parcel;
import android.util.Log;

import base.Action;
import base.Authorizer;
import base.ListenerHolder;
import base.ResponseListener;


public class VKAuthorizer extends Authorizer<VKWrap, VKKeyKeeper> {
    private static final String RESPONSE_URL = "https://oauth.vk.com/blank.html";
    private static final String ACCESS_TOKEN = "access_token=";
    private static final String ERROR_DESCRIPTION = "error_description=";

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
    public Action continueAuthorization(String url, final VKKeyKeeper keys) {
        final ResponseListener<VKWrap> listener = ListenerHolder.getListener();
        Log.e(TAG, url);
        // ToDo: remove monkey code
        if (url.contains(ACCESS_TOKEN)) {
            int left = url.indexOf(ACCESS_TOKEN);
            int right = left;
            while (url.charAt(right) != '&') {
                right++;
            }

            String accessToken = url.substring(left + ACCESS_TOKEN.length(), right);
            Log.d(TAG, accessToken);

            keys.setAccessToken(accessToken);
            return new Action() {
                @Override
                public void exectute() {
                    listener.onSuccess(new VKWrap(keys));
                }
            };
        } else {
            int left = url.indexOf(ERROR_DESCRIPTION);
            int right = left;

            while (right < url.length() && url.charAt(right) != '&') {
                right++;
            }

            final String errorToken = url.substring(left + ERROR_DESCRIPTION.length(), right);
            Log.d(TAG, errorToken);
            return new Action() {
                @Override
                public void exectute() {
                    listener.onFail(errorToken);
                }
            };
        }
    }

    @Override
    protected String getAuthUrl() {
        return "https://oauth.vk.com/authorize?client_id=" + VKKeyKeeper.CLIENT_ID
                + "&redirect_uri=https://oauth.vk.com/blank.html&display_type=mobile&scope=8192&response_type=token";
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith(RESPONSE_URL);
    }
}
