package org.fbk.cit.hlt.parsers.twitchtv.inputs;

import org.fbk.cit.hlt.parsers.twitchtv.entities.Broadcaster;
import org.fbk.cit.hlt.parsers.twitchtv.entities.User;

/**
 * Interface for receiving chat messages
 */
public interface IRCReceiver extends Receiver {
    public void message(String caster, String user, String message, long timestamp);
}
