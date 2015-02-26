package org.fbk.cit.hlt.parsers.twitchtv.inputs;

import org.fbk.cit.hlt.parsers.twitchtv.api.result.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * File writer that stores data in the specified file
 */
public class StreamDataFileWriter extends AbstractFileWriter {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH-mm-ss");

    protected static Logger logger = LoggerFactory.getLogger(StreamDataFileWriter.class);

    public StreamDataFileWriter(File file) {
        super(file);
    }

    public StreamDataFileWriter(String file) {
        super(file);
    }
    
    public synchronized void dumpStream(Stream stream) {
        try {
            Writer writer = writer();
            writer.write(stream.getName());
            writer.write("\t");
            writer.write(stream.getDisplayName());
            writer.write("\t");
            writer.write(dateFormat.format(stream.getStreamingFrom()));
            writer.write("\t");
            writer.write(dateFormat.format(new Date()));
            writer.write("\t");
            writer.write(stream.getDelay());
            writer.write("\t");
            writer.write(stream.getGame());
            writer.write("\t");
            writer.write(URLEncoder.encode(stream.getStatus(), "UTF-8"));
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            logger.warn("Can't write message to file: "+e.getClass()+" "+e.getMessage());
        }
    }
}
