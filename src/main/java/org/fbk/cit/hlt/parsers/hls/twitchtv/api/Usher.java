package org.fbk.cit.hlt.parsers.hls.twitchtv.api;

import org.fbk.cit.hlt.parsers.hls.twitchtv.api.result.AccessToken;

import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

/**
 * Secret Twitch API implementation for getting the video stream
 */
public class Usher extends AbstractAPI {
    public Usher(String host, String schema, String token) {
        super(schema, host, token, "api/channel/hls");
    }

    public Usher(String host, String token) {
        this(host, "http", token);
    }

    public Usher(String token) {
        this("usher.twitch.tv", token);
    }

    public String getPlaylist(AccessToken accessToken) {
        HashMap<String, String> params = new HashMap<>();

        params.put("token", accessToken.getRawToken());
        params.put("sig", accessToken.getSignature());

        params.put("p", Integer.toString((new Random()).nextInt(8888888)+1111111));
        params.put("player", "twitchweb");
        params.put("allow_source", "true");
        params.put("allow_audio_only", "true");

        try(InputStream is = this.getStream(accessToken.getChannel()+".m3u8", params)) {
            return IOUtils.toString(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
