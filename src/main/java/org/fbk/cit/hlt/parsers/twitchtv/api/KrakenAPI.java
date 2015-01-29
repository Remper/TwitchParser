package org.fbk.cit.hlt.parsers.twitchtv.api;

import org.fbk.cit.hlt.parsers.twitchtv.api.request.Filter;
import org.fbk.cit.hlt.parsers.twitchtv.api.result.Game;
import org.fbk.cit.hlt.parsers.twitchtv.api.result.Stream;

import javax.json.*;
import java.io.IOException;
import java.util.*;

/**
 * General Twitch API implementation to get Stream information
 */
public class KrakenAPI extends AbstractAPI {
    public KrakenAPI(String host, String schema, String token) {
        super(schema, host, token, "kraken");
    }

    public KrakenAPI(String host, String token) {
        this(host, "https", token);
    }

    public KrakenAPI(String token) {
        this("api.twitch.tv", token);
    }

    public ArrayList<Game> getTopGames()
    {
        return this.getTopGames((new Filter()).hls().limit(5).offset(0));
    }

    public ArrayList<Game> getTopGames(Filter filter)
    {
        ArrayList<Game> result = new ArrayList<>();
        JsonArray rawGames;
        try {
            rawGames = this.getRawTopGames(filter).getJsonArray("top");
        } catch (IOException e) {
            return result;
        }

        for (JsonValue rawGame : rawGames) {
            if (rawGame.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }

            result.add(new Game((JsonObject) rawGame));
        }

        return result;
    }

    public JsonObject getRawTopGames(Filter filter) throws IOException
    {
        return this.execute("games/top", filter.getParams());
    }

    public ArrayList<Stream> getStreams()
    {
        return this.getStreams(null);
    }

    public ArrayList<Stream> getStreams(String game)
    {
        return this.getStreams(game, null);
    }

    public ArrayList<Stream> getStreams(String game, String channels)
    {
        return this.getStreams(game, channels, (new Filter()).hls().embeddable().limit(5).offset(0));
    }

    public ArrayList<Stream> getStreams(String game, String channels, Filter filter)
    {
        ArrayList<Stream> result = new ArrayList<>();
        JsonArray rawStreams;
        try {
            rawStreams = this.getRawStreams(game, channels, filter).getJsonArray("streams");
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

        for (JsonValue rawStream : rawStreams) {
            if (rawStream.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }

            result.add(new Stream((JsonObject) rawStream));
        }

        return result;
    }

    public JsonObject getRawStreams(String game, String channels, Filter filter) throws IOException
    {
        HashMap<String, String> params = new HashMap<>();
        if (game != null) {
            params.put("game", game);
        }
        if (channels != null) {
            params.put("channels", channels);
        }

        return this.execute("streams", filter.mergeParams(params));
    }
}
