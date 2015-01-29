package org.fbk.cit.hlt.parsers.twitchtv.api;

import org.fbk.cit.hlt.parsers.twitchtv.api.result.AccessToken;

import javax.json.*;
import java.io.IOException;

/**
 * Secret API for getting access tokens
 */
public class SecretAPI extends AbstractAPI {
    public SecretAPI(String host, String schema, String token) {
        super(schema, host, token, "api/channels");
    }

    public SecretAPI(String host, String token) {
        this(host, "https", token);
    }

    public SecretAPI(String token) {
        this("api.twitch.tv", token);
    }

    public JsonObject getRawAccessToken(String channel) throws IOException {
        String method = channel+"/access_token";
        return this.execute(method);
    }

    public AccessToken getAccessToken(String channel) {
        AccessToken token;
        try {
            token = new AccessToken(this.getRawAccessToken(channel));
        } catch (IOException e) {
            e.printStackTrace();
            token = null;
        }

        return token;
    }
}
