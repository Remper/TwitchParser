package org.fbk.cit.hlt.parsers.twitchtv.inputs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;

/**
 * File writer that stores data in the specified file
 */
public class IRCFileWriter implements IRCReceiver {
    protected static Logger logger = LoggerFactory.getLogger(IRC.class);
    
    public static final String FILE_EXTENSION = "csv";
    
    protected File file;
    private Writer buffWriter;
    
    public IRCFileWriter(File file) {
        this.file = file;
    }
    
    public IRCFileWriter(String file) {
        this(new File(file+"."+FILE_EXTENSION));
    }
    
    protected Writer writer() throws IOException {
        if (buffWriter != null) {
            return buffWriter;
        }

        return buffWriter = new BufferedWriter(new FileWriter(file, true));
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
    
    @Override
    public void flush() {
        try {
            buffWriter.flush();
            buffWriter.close();
        } catch (IOException e) {
            logger.warn("Can't close file buffer: "+e.getClass()+" "+e.getMessage());
        }
        buffWriter = null;
    }
}
