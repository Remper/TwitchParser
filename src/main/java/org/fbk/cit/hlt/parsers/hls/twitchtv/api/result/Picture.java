package org.fbk.cit.hlt.parsers.hls.twitchtv.api.result;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;

/**
 * Picture object with that Twitch Kraken API sometimes responds
 */
public class Picture {
    String template;
    HashMap<String, String> predefined;

    public static final int DEF_WIDTH = 100;
    public static final int DEF_HEIGHT = 100;

    public Picture(JsonObject source) throws Exception {
        this.template = null;
        this.predefined = new HashMap<>();

        for (Map.Entry<String, JsonValue> e : source.entrySet()) {
            if (e.getKey().equals("template")) {
                this.template = e.getValue().toString();
                continue;
            }
            this.predefined.put(e.getKey(), e.getValue().toString());
        }

        if (this.template == null) {
            throw new Exception("Twitch changed API or not valid Picture object provided");
        }
    }

    public String getLink(int width, int height) {
        if (width < 1 || height < 1) {
            return null;
        }

        return this.template.replace("{width}", Integer.toString(width)).replace("{height}", Integer.toString(height));
    }

    public String getNamedLink(String link) {
        return this.predefined.getOrDefault(link, this.getLink(DEF_WIDTH, DEF_HEIGHT));
    }
}
