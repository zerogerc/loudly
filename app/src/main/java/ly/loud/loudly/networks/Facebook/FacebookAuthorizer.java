package ly.loud.loudly.networks.facebook;

import ly.loud.loudly.networks.Authorizer;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.util.Query;

public class FacebookAuthorizer extends Authorizer {
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int network() {
        return Networks.FB;
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
        int expiration = Integer.parseInt(response.getParameter("expires_in"));
    }

    @Override
    public Query makeAuthQuery() {
        Query query = new Query(FacebookClient.AUTHORIZE_URL);
        query.addParameter("client_id", FacebookClient.CLIENT_ID);
        query.addParameter("redirect_uri", FacebookClient.REDIRECT_URL);
        query.addParameter("scope", "publish_actions,user_posts");
        query.addParameter("response_type", "token");
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith(FacebookClient.REDIRECT_URL) || url.startsWith(FacebookClient.RESPONSE_URL);
    }
}
