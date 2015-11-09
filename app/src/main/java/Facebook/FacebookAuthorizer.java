package Facebook;

import android.os.Parcel;

import base.Action;
import base.Authorizer;
import base.Networks;
import util.ListenerHolder;
import base.ResponseListener;
import util.Query;

public class FacebookAuthorizer extends Authorizer<FacebookWrap, FacebookKeyKeeper> {
    private static final String AUTHORIZE_URL = "https://www.facebook.com/dialog/oauth";
    private static final String RESPONSE_URL = "https://www.facebook.com/connect/login_success.html";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ERROR_DESCRIPTION = "error";

    @Override
    public int network() {
        return Networks.FB;
    }

    @Override
    protected FacebookKeyKeeper beginAuthorize() {
        return new FacebookKeyKeeper();
    }

    @Override
    public Action continueAuthorization(final String url, final FacebookKeyKeeper keys) {
        final ResponseListener listener = ListenerHolder.getListener(network());
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
                    listener.onSuccess(new FacebookWrap(keys));
                }
            };
        } else {
            final String error = response.getParameter(ERROR_DESCRIPTION);
            return new Action() {
                @Override
                public void execute() {
                    listener.onFail(error);
                }
            };
        }
    }

    @Override
    protected Query getAuthQuery() {
        Query query = new Query(AUTHORIZE_URL);
        query.addParameter("client_id", FacebookKeyKeeper.CLIENT_ID);
        query.addParameter("redirect_uri", RESPONSE_URL);
        query.addParameter("scope", "publish_actions, manage_pages,publish_pages");
        query.addParameter("response_type", "token");
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith(RESPONSE_URL);
    }

    public static final Creator<FacebookAuthorizer> CREATOR = new Creator<FacebookAuthorizer>() {
        @Override
        public FacebookAuthorizer createFromParcel(Parcel source) {
            return new FacebookAuthorizer();
        }

        @Override
        public FacebookAuthorizer[] newArray(int size) {
            return new FacebookAuthorizer[size];
        }
    };
}
