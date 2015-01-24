package ru.remper.parsers.hls.Tags.Media;

import ru.remper.parsers.hls.Tags.*;

/**
 * #EXT-X-ENDLIST
 */
@HLSTag(name="EXT-X-ENDLIST")
public class EndListTag implements Tag {
    @Override
    public String getName() {
        return "EXT-X-ENDLIST";
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
}
