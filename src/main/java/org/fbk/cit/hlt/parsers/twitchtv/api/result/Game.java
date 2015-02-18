package org.fbk.cit.hlt.parsers.twitchtv.api.result;

import javax.json.JsonObject;

/**
 * A game object that is returned by Kraken API
 */
public class Game {
    private String name;
    private Picture box;
    private Picture logo;
    private int viewers;
    private int channels;

    public Game(JsonObject game)
    {
        this.viewers = game.getInt("viewers");
        this.channels = game.getInt("whitelist");
        JsonObject innerGame = game.getJsonObject("game");
        this.name = innerGame.getString("name");
        try {
            this.box = new Picture(innerGame.getJsonObject("box"));
            this.logo = new Picture(innerGame.getJsonObject("logo"));
        } catch (Exception e) {
            this.box = null;
            this.logo = null;
        }
    }

    public Game(String game)
    {
        this.name = game;
        this.box = null;
        this.logo = null;
        this.viewers = -1;
        this.channels = -1;
    }

    public String getName() {
        return name;
    }

    public Picture getBox() {
        return box;
    }

    public Picture getLogo() {
        return logo;
    }

    public int getViewers() {
        return viewers;
    }

    public String getReadableViewers() {
        return this.intToReadable(this.viewers);
    }

    public int getChannels() {
        return channels;
    }

    public String getReadableChannels() {
        return this.intToReadable(this.channels);
    }

    private String intToReadable(int v) {
        return v == -1 ? "Unknown" : Integer.toString(v);
    }
}
