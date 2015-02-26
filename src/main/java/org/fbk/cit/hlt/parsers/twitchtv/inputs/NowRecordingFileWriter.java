package org.fbk.cit.hlt.parsers.twitchtv.inputs;

import org.fbk.cit.hlt.parsers.twitchtv.entities.Broadcaster;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * File writer that stores data in the specified file
 */
public class NowRecordingFileWriter extends AbstractFileWriter {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH-mm-ss");
    
    public NowRecordingFileWriter(File file) {
        super(file);
    }

    public NowRecordingFileWriter(String file) {
        super(file);
    }
    
    public void dump(Collection<Broadcaster> casters) {
        try {
            writer().write(dateFormat.format(new Date()));
            for (Broadcaster caster : casters) {
                writer().write("\t");
                writer().write(caster.getName());
            }
            writer().write("\n");
            writer().flush();
        } catch (IOException e) {
            logger.warn("Can't write message to file: "+e.getClass()+" "+e.getMessage());
        }
    }
}
