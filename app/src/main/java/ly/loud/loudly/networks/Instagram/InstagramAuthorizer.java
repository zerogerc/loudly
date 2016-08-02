package ly.loud.loudly.networks.Instagram;

import ly.loud.loudly.new_base.Authorizer;
import ly.loud.loudly.new_base.KeyKeeper;
import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.util.Query;

/**
 * @author Danil Kolikov
 */
public class InstagramAuthorizer extends Authorizer {
    private static final String AUTHORIZE_URL = "https://api.instagram.com/oauth/authorize/";
    private static final String RESPONSE_URL = "loudly://";
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int network() {
        return Networks.INSTAGRAM;
    }

    @Override
    protected KeyKeeper beginAuthorize() {
        return Networks.makeKeyKeeper(network());
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
        ((InstagramKeyKeeper) keys).setAccessToken(response.getParameter(ACCESS_TOKEN));
    }

    @Override
    public Query makeAuthQuery() {
        Query query = new Query(AUTHORIZE_URL);
        query.addParameter("client_id", InstagramKeyKeeper.CLIENT_ID);
        query.addParameter("redirect_uri", RESPONSE_URL);
        query.addParameter("response_type", "token");
        query.addParameter("scope", "basic public_content");
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith("loudly");
    }
}
