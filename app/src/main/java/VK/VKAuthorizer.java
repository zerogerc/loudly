package VK;

import android.os.Parcel;

import base.KeyKeeper;
import base.Authorizer;
import base.Networks;
import util.Query;

public class VKAuthorizer extends Authorizer {
    private static final String AUTHORIZE_URL = "https://oauth.vk.com/authorize";
    private static final String RESPONSE_URL = "https://oauth.vk.com/blank.html";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ERROR_DESCRIPTION = "error";
    private static final String USER_ID = "user_id";

    @Override
    public int network() {
        return Networks.VK;
    }

    @Override
    protected VKKeyKeeper beginAuthorize() {
        return new VKKeyKeeper();
    }

    @Override
    public String successToken() {
        return ACCESS_TOKEN;
    }

    @Override
    public String errorToken() {
        return ERROR_DESCRIPTION;
    }

    @Override
    public void addFieldsFromQuery(KeyKeeper keys, Query response) {
        ((VKKeyKeeper) keys).setAccessToken(response.getParameter(ACCESS_TOKEN));
        ((VKKeyKeeper) keys).setUserId(response.getParameter(USER_ID));
    }

    @Override
    protected Query getAuthQuery() {
        Query query = new Query(AUTHORIZE_URL);
        query.addParameter("client_id", VKKeyKeeper.CLIENT_ID);
        query.addParameter("redirect_uri", RESPONSE_URL);
        query.addParameter("display_type", "mobile");
        query.addParameter("scope", "wall,photos");
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
