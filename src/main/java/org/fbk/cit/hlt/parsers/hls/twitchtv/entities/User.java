package org.fbk.cit.hlt.parsers.hls.twitchtv.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Twitch User entity
 */
public class User {
    protected String name;

    public User(String username) {
        name = username;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 11).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User compUser = (User) obj;
        return new EqualsBuilder().append(name, compUser.name).isEquals();
    }

    public String getName() {
        return name;
    }
}
