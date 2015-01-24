package ru.remper.parsers.hls.Tags.General;

import ru.remper.parsers.hls.Tags.HLSTag;
import ru.remper.parsers.hls.Tags.Tag;
import ru.remper.parsers.hls.Tags.TagType;

/**
 * #EXTM3U
 */
@HLSTag(name = "EXTM3U")
public class OpeningTag implements Tag {
    @Override
    public String getName() {
        return "EXTM3U";
    }

    @Override
    public TagType getType() {
        return TagType.PLAYLIST;
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
