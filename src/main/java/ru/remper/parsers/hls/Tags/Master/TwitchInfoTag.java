package ru.remper.parsers.hls.tags.master;

import ru.remper.parsers.hls.exceptions.InvalidTagParameters;
import ru.remper.parsers.hls.tags.HLSTag;
import ru.remper.parsers.hls.tags.Tag;
import ru.remper.parsers.hls.tags.TagType;
import ru.remper.parsers.hls.tags.TagWithAttributeList;

/**
 * #EXT-X-TWITCH-INFO
 */
@HLSTag(name="EXT-X-TWITCH-INFO")
public class TwitchInfoTag extends TagWithAttributeList implements Tag {
    public TwitchInfoTag(String propertyString) throws InvalidTagParameters {
        super(propertyString);
    }

    @Override
    public String getName() {
        return "EXT-X-TWITCH-INFO";
    }

    @Override
    public TagType getType() {
        return TagType.MASTER_PLAYLIST;
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
