package org.fbk.cit.hlt.parsers.hls.twitchtv.api.result;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.util.Date;

/**
 * Access Token from Secret API
 */
public class AccessToken {
    private String rawToken;
    private Date expires;
    private int user_id;
    private String channel;
    private String signature;

    public AccessToken(JsonObject rawResponse)
    {
        this.signature = rawResponse.getString("sig");
        this.rawToken = rawResponse.getString("token");
        JsonObject token = Json.createReader(new StringReader(this.rawToken)).readObject();

        this.expires = new Date(((long) token.getInt("expires"))*1000);
        this.user_id = token.getInt("user_id");
        this.channel = token.getString("channel");
    }

    public String getRawToken() {
        return rawToken;
    }

    public Date getExpires() {
        return expires;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getChannel() {
        return channel;
    }

    public String getSignature() {
        return signature;
    }
}
