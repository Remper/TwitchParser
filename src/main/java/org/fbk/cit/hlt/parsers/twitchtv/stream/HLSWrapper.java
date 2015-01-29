package org.fbk.cit.hlt.parsers.twitchtv.stream;

import org.apache.commons.cli.ParseException;
import org.fbk.cit.hlt.parsers.hls.MediaType;
import org.fbk.cit.hlt.parsers.hls.StreamManager;
import org.fbk.cit.hlt.parsers.hls.persist.FilePersister;
import org.fbk.cit.hlt.parsers.hls.persist.Persister;
import org.fbk.cit.hlt.parsers.twitchtv.entities.Video;

import java.io.File;

/**
 * A wrapper to our HLS implementation
 */
public class HLSWrapper extends Thread {
    private Process process;
    private File saveFolder;
    private String uri;
    private String label;

    public HLSWrapper(File saveFolder, String masterUri) {
        this(saveFolder, masterUri, masterUri);
    }

    public HLSWrapper(File saveFolder, String masterUri, String label) {
        this.saveFolder = saveFolder;
        this.uri = masterUri;
        this.label = label;
        this.setPriority(Thread.NORM_PRIORITY);
        this.setDaemon(false);
    }

    public Video convertToVideo() throws Exception {
        return new Video(this, saveFolder.getAbsolutePath());
    }

    public Process getProcess() {
        return process;
    }

    public File getSaveFolder() {
        return saveFolder;
    }

    /**
     * Checks the input stream for new messages.
     */
    @Override
    public void run() {
        StreamManager manager;
        Persister persister;
        try {
            persister = new FilePersister(this.saveFolder);
            manager = new StreamManager(persister, uri);
        } catch (Exception e) {
            System.err.println("Can't create a Stream Manager: "+e.getClass().getSimpleName()+" "+e.getMessage());
            return;
        }
        
        manager.setLabel(label);
        manager.start(MediaType.VIDEO);
    }
}