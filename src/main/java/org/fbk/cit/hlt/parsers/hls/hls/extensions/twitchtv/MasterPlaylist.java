package org.fbk.cit.hlt.parsers.hls.hls.extensions.twitchtv;

import org.fbk.cit.hlt.parsers.hls.hls.exceptions.IllegalTagSequence;
import org.fbk.cit.hlt.parsers.hls.hls.tags.Tag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.extensions.twitchtv.TwitchInfoTag;

/**
 * Extension class with the support of Twitch tags
 */
public class MasterPlaylist extends org.fbk.cit.hlt.parsers.hls.hls.MasterPlaylist {
    protected TwitchInfo twitchInfo;

    @Override
    public void applyTag(Tag tag) throws IllegalTagSequence {
        super.applyTag(tag);

        if (tag instanceof TwitchInfoTag) {
            twitchInfo = TwitchInfo.fromTag((TwitchInfoTag) tag);
        }
    }
}
