package org.fbk.cit.hlt.parsers.hls.hls.extensions.twitchtv;

import org.fbk.cit.hlt.parsers.hls.hls.MediaType;
import org.fbk.cit.hlt.parsers.hls.hls.exceptions.IllegalTagSequence;
import org.fbk.cit.hlt.parsers.hls.hls.tags.Tag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.extensions.twitchtv.TwitchElapsedSecsTag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.extensions.twitchtv.TwitchTotalSecsTag;

/**
 * Extension class with the support of Twitch tags
 */
public class MediaPlaylist extends org.fbk.cit.hlt.parsers.hls.hls.MediaPlaylist {
    protected float elapsedSecs = 0;
    protected float totalSecs = 0;

    public MediaPlaylist(String baseURI, MediaType mediaType, String groupId) {
        super(baseURI, mediaType, groupId);
    }

    @Override
    public void applyTag(Tag tag) throws IllegalTagSequence {
        super.applyTag(tag);

        if (tag instanceof TwitchElapsedSecsTag) {
            elapsedSecs = ((TwitchElapsedSecsTag) tag).getSecs();
            return;
        }
        if (tag instanceof TwitchTotalSecsTag) {
            totalSecs = ((TwitchTotalSecsTag) tag).getSecs();
        }
    }

    public float getElapsedSecs() {
        return elapsedSecs;
    }

    public float getTotalSecs() {
        return totalSecs;
    }
}
