package org.fbk.cit.hlt.parsers.twitchtv.inputs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Abstract class for common file writer logic
 */
public class AbstractFileWriter implements Receiver {
    protected static Logger logger = LoggerFactory.getLogger(AbstractFileWriter.class);
    
    private Writer buffWriter;
    protected File file;
    
    public static final String FILE_EXTENSION = "csv";

    public AbstractFileWriter(File file) {
        this.file = file;
    }

    public AbstractFileWriter(String file) {
        this(new File(file+"."+FILE_EXTENSION));
    }

    protected Writer writer() throws IOException {
        if (buffWriter != null) {
            return buffWriter;
        }

        return buffWriter = new BufferedWriter(new FileWriter(file, true));
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
