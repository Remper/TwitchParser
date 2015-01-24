package org.fbk.cit.hlt.parsers.hls.hls.tags.master;

import org.fbk.cit.hlt.parsers.hls.hls.MediaType;
import org.fbk.cit.hlt.parsers.hls.hls.tags.HLSTag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.Tag;
import org.fbk.cit.hlt.parsers.hls.hls.tags.TagType;
import org.fbk.cit.hlt.parsers.hls.hls.tags.TagWithAttributeList;
import org.fbk.cit.hlt.parsers.hls.hls.exceptions.InvalidTagParameters;

/**
 * #EXT-X-MEDIA
 */
@HLSTag(name="EXT-X-MEDIA")
public class MediaTag extends TagWithAttributeList implements Tag {
    protected MediaType mediaType;
    protected String URI;
    protected String groupId;
    protected String language;
    protected String assocLanguage;
    protected String name;
    protected boolean isDefault = false;
    protected boolean autoselect = false;
    protected boolean isForced = false;

    public MediaTag(String propertyString) throws InvalidTagParameters {
        super(propertyString);
        if (mediaType == null) {
            throw InvalidTagParameters.required("TYPE");
        }
        if (groupId == null) {
            throw InvalidTagParameters.required("GROUP-ID");
        }
        if (name == null) {
            throw InvalidTagParameters.required("NAME");
        }
    }

    @Override
    protected void resolveParameter(String name, long number) throws InvalidTagParameters {
        throw new InvalidTagParameters("Invalid parameter name for an integer parameter: \"" + name + "\"");
    }

    @Override
    protected void resolveParameter(String name, float number) throws InvalidTagParameters {
        throw new InvalidTagParameters("Invalid parameter name for a float parameter: \"" + name + "\"");
    }

    @Override
    protected void resolveParameter(String name, String string) throws InvalidTagParameters {
        switch (name) {
            case "TYPE":
                try {
                    mediaType = MediaType.valueOf(string.replace('-', '_').toUpperCase());
                } catch (Exception e) {
                    throw new InvalidTagParameters();
                }
                return;
            case "URI":
                URI = string;
                return;
            case "GROUP-ID":
                groupId = string;
                return;
            case "LANGUAGE":
                language = string;
                return;
            case "ASSOC-LANGUAGE":
                assocLanguage = string;
                return;
            case "NAME":
                this.name = string;
                return;
            case "DEFAULT":
                isDefault = string.equals("YES");
                return;
            case "AUTOSELECT":
                autoselect = string.equals("YES");
                return;
            case "FORCED":
                isForced = string.equals("YES");
                return;
            case "CHARACTERISTICS":
                return;
            case "INSTREAM-ID":
                return;
        }

        throw new InvalidTagParameters("Invalid parameter name for a string parameter: \"" + name + "\"");
    }

    @Override
    public String getName() {
        return "EXT-X-MEDIA";
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
        return false;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getMediaName() {
        return name;
    }

    public String getURI() {
        return URI;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getLanguage() {
        return language;
    }

    public String getAssocLanguage() {
        return assocLanguage;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isAutoselect() {
        return autoselect;
    }

    public boolean isForced() {
        return isForced;
    }
}