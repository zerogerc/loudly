package ly.loud.loudly.networks.vk;

import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Authorizer;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.util.Query;

public class VKAuthorizer extends Authorizer {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ERROR_DESCRIPTION = "error";
    private static final String USER_ID = "user_id";

    @Override
    public int network() {
        return Networks.VK;
    }

    @Override
    protected VKKeyKeeper beginAuthorize() {
        return null;
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
        // It's unsafe to store expiration time in KeyKeeper
        int expiration = Integer.parseInt(response.getParameter("expires_in"));
    }

    @Override
    public Query makeAuthQuery() {
        Query query = new Query(VKModel.AUTHORIZE_URL);
        query.addParameter("client_id", VKClient.CLIENT_ID);
        query.addParameter("redirect_uri", VKModel.RESPONSE_URL);
        query.addParameter("display_type", "mobile");
        query.addParameter("scope", "wall,photos");
        query.addParameter("response_type", "token");
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith(VKModel.RESPONSE_URL);
    }
}
