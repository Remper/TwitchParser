package org.fbk.cit.hlt.parsers.hls.hls.tags;

import org.fbk.cit.hlt.parsers.hls.hls.MediaType;
import org.fbk.cit.hlt.parsers.hls.hls.tags.master.MediaTag;
import org.junit.Assert;
import org.junit.Test;
import org.fbk.cit.hlt.parsers.hls.hls.exceptions.InvalidTagParameters;

/**
 * Test for Media Tag creationg
 */
public class MediaTagTest {
    @Test
    public void parseMinimumCorrectParameters() throws Exception {
        MediaTag tag = new MediaTag("TYPE=VIDEO,GROUP-ID=\"meow\",NAME=\"testtag\"");
        Assert.assertEquals(tag.getMediaType(), MediaType.VIDEO);
    }

    @Test(expected = InvalidTagParameters.class)
    public void parseSingleValidParameter() throws Exception {
        MediaTag tag = new MediaTag("TYPE=VIDEO");
    }

    @Test(expected = InvalidTagParameters.class)
    public void parseMinimumParametersWithInvalidValues() throws Exception {
        MediaTag tag = new MediaTag("TYPE=241,GROUP-ID=\"meow\",NAME=\"testtag\"");
    }
}
