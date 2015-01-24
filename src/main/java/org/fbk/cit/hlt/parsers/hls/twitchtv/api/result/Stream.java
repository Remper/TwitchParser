package org.fbk.cit.hlt.parsers.hls.twitchtv.api.result;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.text.*;
import java.util.Date;

/**
 * A stream object that is returned by Kraken API
 */
public class Stream {
    private Picture preview;
    private String game;

    private String background;
    private String logo;
    private String banner;
    private String status;
    private String url;
    private String displayName;
    private String name;
    private int delay;
    private int viewers;
    private Date created;
    private Date streamingFrom;

    private String snapshot;

    public Stream(JsonObject stream) {
        this.snapshot = stream.toString();
        JsonObject channel = stream.getJsonObject("channel");

        try {
            this.preview = new Picture(stream.getJsonObject("preview"));
        } catch (Exception e) {
            this.preview = null;
        }
        this.game = stream.getString("game");

        this.background = parseNullableString(channel.get("background"));
        this.logo = parseNullableString(channel.get("logo"));
        this.banner = parseNullableString(channel.get("banner"));
        this.status = parseNullableString(channel.get("status"));
        this.url = parseNullableString(channel.get("url"));
        this.displayName = channel.getString("display_name");
        this.name = channel.getString("name");

        this.delay = parseNullableInt(channel.get("delay"));
        this.viewers = stream.getInt("viewers");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            this.created = format.parse(channel.getString("created_at"));
            this.streamingFrom = format.parse(stream.getString("created_at"));
        } catch (ParseException e) {
            this.streamingFrom = new Date();
            this.created = new Date();
        }
    }

    public Picture getPreview() {
        return preview;
    }

    public String getGame() {
        return game;
    }

    public String getBackground() {
        return background;
    }

    public String getLogo() {
        return logo;
    }

    public String getBanner() {
        return banner;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public int getDelay() {
        return delay;
    }

    public int getViewers() {
        return viewers;
    }

    public String getReadableViewers() {
        return this.intToReadable(this.viewers);
    }

    public Date getCreated() {
        return created;
    }

    public Date getStreamingFrom() {
        return streamingFrom;
    }

    public String getSnapshot() {
        return snapshot;
    }

    private String intToReadable(int v) {
        return v == -1 ? "Unknown" : Integer.toString(v);
    }

    private String parseNullableString(JsonValue value) {
        if (value == null || value.getValueType() == JsonValue.ValueType.NULL) {
            return null;
        }

        return value.toString();
    }

    private int parseNullableInt(JsonValue value) {
        if (value == null || value.getValueType() == JsonValue.ValueType.NULL) {
            return 0;
        }

        return ((JsonNumber) value).intValue();
    }
}
