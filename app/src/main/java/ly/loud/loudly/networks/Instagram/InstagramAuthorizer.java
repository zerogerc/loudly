package ly.loud.loudly.networks.instagram;

import ly.loud.loudly.networks.Authorizer;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.util.Query;

/**
 * @author Danil Kolikov
 */
public class InstagramAuthorizer extends Authorizer {
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int network() {
        return Networks.INSTAGRAM;
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
    }

    @Override
    public Query makeAuthQuery() {
        Query query = new Query(InstagramClient.AUTHORIZE_URL);
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith("loudly");
    }
}
