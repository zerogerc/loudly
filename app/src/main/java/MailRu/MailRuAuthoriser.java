package MailRu;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

import base.KeyKeeper;
import ly.loud.loudly.Loudly;
import util.UIAction;
import base.Authorizer;
import base.Networks;
import util.ResultListener;
import util.Query;

public class MailRuAuthoriser extends Authorizer {
    private static final String AUTHORIZE_URL = "https://connect.mail.ru/oauth/authorize";
    private static final String REDIRECT_URL = "http://connect.mail.ru/oauth/success.html";
    private static final String ACCESS_TOKEN = "access_token";

    @Override
    public int network() {
        return Networks.MAILRU;
    }

    @Override
    protected MailRuKeyKeeper beginAuthorize() {
        return new MailRuKeyKeeper();
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
        ((MailRuKeyKeeper) keys).setSessionKey(response.getParameter(ACCESS_TOKEN));
        ((MailRuKeyKeeper)keys).setSessionKey(response.getParameter("refresh_token"));
    }

    @Override
    protected Query getAuthQuery() {
        Query query = new Query(AUTHORIZE_URL);
        query.addParameter("client_id", MailRuKeyKeeper.CLIENT_ID);
        query.addParameter("response_type", "token");
        query.addParameter("scope", "stream");
        query.addParameter("redirect_uri", REDIRECT_URL);
        return query;
    }

    @Override
    public boolean isResponse(String url) {
        return url.startsWith(REDIRECT_URL);
    }
    public static final Creator<MailRuAuthoriser> CREATOR = new Creator<MailRuAuthoriser>() {
        @Override
        public MailRuAuthoriser createFromParcel(Parcel source) {
            return new MailRuAuthoriser();
        }

        @Override
        public MailRuAuthoriser[] newArray(int size) {
            return new MailRuAuthoriser[size];
        }
    };
}
