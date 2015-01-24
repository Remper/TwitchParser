package org.fbk.cit.hlt.parsers.hls.hls.tags.extensions.twitchtv;

import org.fbk.cit.hlt.parsers.hls.hls.exceptions.InvalidTagParameters;
import org.fbk.cit.hlt.parsers.hls.hls.tags.HLSTag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.Tag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.TagType;

/**
 * #EXT-X-TWITCH-TOTAL-SECS
 */
@HLSTag(name="EXT-X-TWITCH-TOTAL-SECS")
public class TwitchTotalSecsTag implements Tag {
    protected float secs;

    public TwitchTotalSecsTag(String properties) throws InvalidTagParameters {
        if (!properties.matches("[0-9]+(\\.[0-9]+)?")) {
            throw new InvalidTagParameters();
        }

        secs = Float.valueOf(properties);
    }

    @Override
    public String getName() {
        return "EXT-X-TWITCH-TOTAL-SECS";
    }

    @Override
    public TagType getType() {
        return TagType.MEDIA_PLAYLIST;
    }

    @Override
    public int minVersion() {
        return 0;
    }

    @Override
    public boolean isOneTime() {
        return false;
    }

    @Override
    public boolean shouldBeFollowedByURI() {
        return false;
    }

    @Override
    public boolean shouldBeUnique() {
        return true;
    }

    public float getSecs() {
        return secs;
    }
}
