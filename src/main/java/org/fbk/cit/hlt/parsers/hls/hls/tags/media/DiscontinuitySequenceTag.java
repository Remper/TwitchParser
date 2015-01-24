package org.fbk.cit.hlt.parsers.hls.hls.tags.media;

import org.fbk.cit.hlt.parsers.hls.hls.tags.HLSTag;
import org.fbk.cit.hlt.parsers.hls.hls.exceptions.InvalidTagParameters;
import org.fbk.cit.hlt.parsers.hls.hls.tags.Tag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.TagType;

/**
 * #EXT-X-DISCONTINUITY-SEQUENCE
 */
@HLSTag(name="EXT-X-DISCONTINUITY-SEQUENCE")
public class DiscontinuitySequenceTag implements Tag {
    protected int sequence;

    public DiscontinuitySequenceTag(String properties) throws InvalidTagParameters {
        if (!properties.matches("[0-9]+")) {
            throw new InvalidTagParameters();
        }

        sequence = Integer.valueOf(properties);
    }

    @Override
    public String getName() {
        return "EXT-X-DISCONTINUITY-SEQUENCE";
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

    public int getSequence() {
        return sequence;
    }
}
