package org.fbk.cit.hlt.parsers.twitchtv.api;

import org.fbk.cit.hlt.parsers.twitchtv.api.result.AccessToken;

import javax.json.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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


    public ArrayList<String> getChatServers(String channel) {
        ArrayList<String> result = new ArrayList<>();
        JsonObject rawSettings;
        try {
            rawSettings = this.getRawChatSettings(channel);
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

        if (rawSettings == null || rawSettings.getValueType() == JsonValue.ValueType.NULL
                || !rawSettings.getBoolean("eventchat", false)) {
            return result;
        }

        for (JsonValue value : rawSettings.getJsonArray("chat_servers")) {
            if (value.getValueType() != JsonValue.ValueType.STRING) {
                continue;
            }

            result.add(((JsonString) value).getString());
        }

        return result;
    }

    public JsonObject getRawChatSettings(String channel) throws IOException {
        return this.execute(channel+"/chat_properties");
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
