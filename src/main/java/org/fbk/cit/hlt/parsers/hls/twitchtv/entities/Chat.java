package org.fbk.cit.hlt.parsers.hls.twitchtv.entities;

import java.util.ArrayList;

/**
 * Chat entity
 */
public class Chat {
    private ArrayList<ChatEntry> entries;
    private Stream stream;

    public Chat(Stream stream) {
        this.stream = stream;
        this.entries = new ArrayList<ChatEntry>();
    }

    public ArrayList<ChatEntry> getEntries() {
        return entries;
    }

    public Stream getStream() {
        return stream;
    }

    public ChatEntry createEntry(User author, String message) {
        ChatEntry entry = new ChatEntry(this);
        entry.author = author;
        entry.message = message;
        this.entries.add(entry);

        return entry;
    }
}
