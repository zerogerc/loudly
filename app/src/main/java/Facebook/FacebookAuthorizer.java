package Facebook;

import android.os.Parcel;

import base.Authorizer;
import base.KeyKeeper;
import base.Networks;
import util.Query;

public class FacebookAuthorizer extends Authorizer {
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
    public String successToken() {
        return ACCESS_TOKEN;
    }

    @Override
    public String errorToken() {
        return "error";
    }

    @Override
    public void addFieldsFromQuery(KeyKeeper keys, Query response) {
        ((FacebookKeyKeeper) keys).setAccessToken(response.getParameter(ACCESS_TOKEN));
    }

    @Override
    protected Query getAuthQuery() {
        Query query = new Query(AUTHORIZE_URL);
        query.addParameter("client_id", FacebookKeyKeeper.CLIENT_ID);
        query.addParameter("redirect_uri", RESPONSE_URL);
        query.addParameter("scope", "publish_actions,user_posts");
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
