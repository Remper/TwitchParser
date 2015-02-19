package org.fbk.cit.hlt.parsers.twitchtv.inputs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;

/**
 * File writer that stores data in the specified file
 */
public class IRCFileWriter extends AbstractFileWriter implements IRCReceiver {
    protected static Logger logger = LoggerFactory.getLogger(IRCFileWriter.class);
    
    public IRCFileWriter(File file) {
        super(file);
    }
    
    public IRCFileWriter(String file) {
        super(file);
    }
    
    @Override
    public void message(String caster, String user, String message, long timestamp) {
        try {
            writer().write(caster);
            writer().write("\t");
            writer().write(user);
            writer().write("\t");
            writer().write(Long.toString(timestamp));
            writer().write("\t");
            writer().write(URLEncoder.encode(message, "UTF-8"));
            writer().write("\n");
        } catch (IOException e) {
            logger.warn("Can't write message to file: "+e.getClass()+" "+e.getMessage());
        }
    }
}
