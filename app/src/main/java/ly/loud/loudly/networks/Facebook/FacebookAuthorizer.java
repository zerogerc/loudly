package ly.loud.loudly.networks.Facebook;

import ly.loud.loudly.base.Authorizer;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.util.Query;

public class FacebookAuthorizer extends Authorizer {
    private static final String AUTHORIZE_URL = "https://www.facebook.com/dialog/oauth";
    private static final String RESPONSE_URL = "https://web.facebook.com/connect/login_success.html";
    private static final String REDIRECT_URL = "https://www.facebook.com/connect/login_success.html";
    private static final String ACCESS_TOKEN = "access_token";

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
        int expiration = Integer.parseInt(response.getParameter("expires_in"));
        keys.expiresIn(expiration);
    }

    @Override
    public Query makeAuthQuery() {
        Query query = new Query(AUTHORIZE_URL);
        query.addParameter("client_id", FacebookKeyKeeper.CLIENT_ID);
        query.addParameter("redirect_uri", REDIRECT_URL);
        query.addParameter("scope", "publish_actions,user_posts");
        query.addParameter("response_type", "token");
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith(REDIRECT_URL) || url.startsWith(RESPONSE_URL);
    }
}
