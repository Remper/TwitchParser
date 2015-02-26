package org.fbk.cit.hlt.parsers.twitchtv;

import org.fbk.cit.hlt.parsers.twitchtv.api.KrakenAPI;
import org.fbk.cit.hlt.parsers.twitchtv.api.result.Stream;
import org.fbk.cit.hlt.parsers.twitchtv.inputs.StreamDataFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that executes in the separate thread and continuously poll twitch API
 */
public class APIPoller extends Thread {
    protected static Logger logger = LoggerFactory.getLogger(CorpusManager.class);
    
    public static final long POLLING_DELAY = 5*50*1000; //5 minutes
    
    private KrakenAPI api;
    private StreamDataFileWriter receiver;
    private final Set<String> streamList = new HashSet<>();
    private final ArrayList<APIListener> listeners = new ArrayList<>();
    private ArrayList<Stream> lastStreams = null;
    
    public APIPoller(KrakenAPI api) {
        this.api = api;
        this.setDaemon(false);
        this.setPriority(Thread.NORM_PRIORITY);
    }
    
    private void writeObj(Stream stream) {
        if (receiver == null) {
            return;
        }
        
        receiver.dumpStream(stream);
    }
    
    public void updateStreamList(Collection<String> streams) {
        synchronized (streamList) {
            streamList.clear();
            streamList.addAll(streams);
        }
    }
    
    public synchronized ArrayList<Stream> getStreams() {
        return lastStreams == null ? null : (ArrayList<Stream>) lastStreams.clone();
    }
    
    private void poll() {
        String streamList;
        synchronized (this.streamList) {
            streamList = String.join(",", this.streamList);
        }
        ArrayList<Stream> streams = new ArrayList<>();
        for (Stream stream : api.getStreamsByWhitelist(streamList)) {
            writeObj(stream);
            streams.add(stream);
        }
        logger.info("Polled "+streams.size()+" streams");
        lastStreams = streams;
    }
    
    @Override
    public void run() {
        logger.info("Starting listening the API");
        while (!this.isInterrupted()) {
            try {
                poll();
                listeners.forEach(this::notify);
                sleep(POLLING_DELAY);
            } catch (InterruptedException e) {
                logger.warn("My sleep was interrupted");
            }
        }
        logger.info("Shutting down the poller");
    }
    
    public void subscribe(APIListener listener) {
        listeners.add(listener);
    }
    
    private void notify(APIListener listener) {
        listener.polled();
    }

    public void setReceiver(StreamDataFileWriter receiver) {
        this.receiver = receiver;
    }
}
