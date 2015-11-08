package VK;

import android.os.Parcel;
import base.Action;
import base.Authorizer;
import util.ListenerHolder;
import base.ResponseListener;
import util.Query;

public class VKAuthorizer extends Authorizer<VKWrap, VKKeyKeeper> {
    private static final String AUTHORIZE_URL = "https://oauth.vk.com/authorize";
    private static final String RESPONSE_URL = "https://oauth.vk.com/blank.html";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ERROR_DESCRIPTION = "error_description";

    @Override
    protected VKKeyKeeper beginAuthorize() {
        return new VKKeyKeeper();
    }

    @Override
    public Action continueAuthorization(final String url, final VKKeyKeeper keys) {
        final ResponseListener listener = ListenerHolder.getListener();
        Query response = Query.fromURL(url);

        if (response == null) {
            return new Action() {
                @Override
                public void execute() {
                    listener.onFail("Failed to parse response: " + url);
                }
            };
        }

        if (response.containsParameter(ACCESS_TOKEN)) {
            String accessToken = response.getParameter(ACCESS_TOKEN);
            keys.setAccessToken(accessToken);
            return new Action() {
                @Override
                public void execute() {
                    listener.onSuccess(new VKWrap(keys));
                }
            };
        } else {
            final String errorToken = response.getParameter(ERROR_DESCRIPTION);
            return new Action() {
                @Override
                public void execute() {
                    listener.onFail(errorToken);
                }
            };
        }
    }

    @Override
    protected Query getAuthQuery() {
        Query query = new Query(AUTHORIZE_URL);
        query.addParameter("client_id", VKKeyKeeper.CLIENT_ID);
        query.addParameter("redirect_uri", RESPONSE_URL);
        query.addParameter("display_type", "mobile");
        query.addParameter("scope", "wall");
        query.addParameter("response_type", "token");
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith(RESPONSE_URL);
    }

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
}
