package org.fbk.cit.hlt.parsers.hls.twitchtv.entities;

import java.sql.Time;

/**
 * Chat entry entity
 */
public class ChatEntry {
    public User author;
    public String message;
    public Time timestamp;
    public boolean isMod;
    public boolean isSubscriber;
    public boolean isTurbo;
    public boolean isBroadcaster;

    private Chat chat;

    public ChatEntry(Chat chat) {
        this.chat = chat;
        this.isMod = this.isSubscriber = this.isTurbo = this.isBroadcaster = false;
    }

    public Chat getChat() {
        return chat;
    }

    //Chain functions for easy configuration
    public void mod() {
        this.isMod = true;
    }

    public void subscriber() {
        this.isSubscriber = true;
    }

    public void turbo() {
        this.isTurbo = true;
    }

    public void broadcaster() {
        this.isBroadcaster = true;
    }
}
