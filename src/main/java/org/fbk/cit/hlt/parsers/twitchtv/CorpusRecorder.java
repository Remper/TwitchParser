package org.fbk.cit.hlt.parsers.twitchtv;

import org.apache.commons.cli.*;
import org.fbk.cit.hlt.parsers.twitchtv.util.OptionBuilder;
import org.apache.commons.configuration.ConfigurationException;
import org.fbk.cit.hlt.parsers.twitchtv.api.KrakenAPI;
import org.fbk.cit.hlt.parsers.twitchtv.api.request.Filter;
import org.fbk.cit.hlt.parsers.twitchtv.api.result.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Main entry point for TwitchParser
 */
public class CorpusRecorder {
    private KrakenAPI api;
    private CorpusManager cm;
    StreamConfiguration config;
    
    protected static Logger logger = LoggerFactory.getLogger(CorpusRecorder.class);
    
    public CorpusRecorder(String corpus, StreamConfiguration config) throws Exception {
        this.config = config;
        this.api = new KrakenAPI(config.getToken());
        this.cm = new CorpusManager(corpus, config.getUser(), config.getToken());
    }
    
    public static CorpusRecorder resume(String corpus) throws Exception {
        //Reading configuration
        StreamConfiguration configuration = new StreamConfiguration(corpus);
        CorpusRecorder cr = new CorpusRecorder(corpus, configuration);
        
        return cr;
    }

    /**
     * Start recording
     */
    public void record() {
        APIPoller poller = new APIPoller(api);
        APISubscriber subscriber = new APISubscriber();
        poller.setReceiver(cm.getStreamDataWriter());
        poller.subscribe(subscriber);
        poller.updateStreamList(Arrays.asList(config.getWhitelist()));
        poller.start();
        
        HashSet<Stream> list = populateRecordList(config.getWildcard());
        if (list.size() == 0) {
            logger.info("Nothing to record. Aborting");
            return;
        }
        logger.info("Recording "+list.size()+" streams");
        
        recordStreams(list);
        
        while (cm.isRecording() && !Thread.interrupted()) {
            try {
                Thread.sleep(APIPoller.POLLING_DELAY / 4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cm.checkSystem();
            if (subscriber.polled) {
                subscriber.ack();
                recordStreams(poller.getStreams());
            }
            
            int online = config.getWildcard() - cm.getOnlineCount();
            if (online < 0) {
                online = 0;
            }
            recordStreams(getTopStreams(online));
        }
        cm.stopRecording();
        
        logger.info("Stopped recording");
    }
    
    public void recordStreams(Collection<Stream> list) {
        try {
            for (Stream stream : list) {
                cm.recordStream(stream);
            }
        } catch (Exception e) {
            logger.warn("Exception while submitting streams: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }
    
    public HashSet<Stream> populateRecordList(int wildcards) {
        HashSet<Stream> list = new HashSet<>();
        list.addAll(api.getStreamsByWhitelist(String.join(",", config.getWhitelist())));
        logger.info("Querying whitelist channels... " + list.size() + " online");
        list.addAll(getTopStreams(wildcards));
        return list;
    }

    public ArrayList<Stream> getTopStreams(int limit) {
        if (limit == 0) {
            return new ArrayList<>();
        }
        return api.getStreams(null, null, new Filter().embeddable().hls().limit(limit).offset(0));
    }

    public static void main(String args[]) {
        //Reading defaults
        StreamConfiguration config;
        try {
            config = new StreamConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Invalid configuration file: "+e.getClass().getSimpleName()+" "+e.getMessage());
            System.exit(1);
            return;
        }
        
        //Options specification
        Options options = new Options();
        options.addOption("w", StreamConfiguration.PARAM_WILDCARD, true, "Amount of the wildcard streams to download");
        options.addOption("l", StreamConfiguration.PARAM_WHITELIST, true, "Comma-separated list of whitelist that should always be recorded");
        options.addOption(OptionBuilder.createRequired("c", "corpus", "Folder to where we should store the data"));
        options.addOption(OptionBuilder.createRequired("u", "user", "Twitch login"));
        options.addOption(OptionBuilder.createRequired("t", "token", "Twitch authorization token"));

        CommandLineParser parser = new PosixParser();
        CommandLine line;
        CorpusRecorder recorder;
        try {
            line = parser.parse(options, args);
            config.replaceWithCommandLine(line);
            
            recorder = new CorpusRecorder(line.getOptionValue("corpus"), config);
        } catch (Exception e) {
            String footer = "\nError: "+e.getMessage();
            new HelpFormatter().printHelp(400, "java -jar twitchtv-0.2.jar", "\n", options, footer, true);
            return;
        }
        
        recorder.record();
    }
    
    protected class APISubscriber implements APIListener {
        public boolean polled = false;
        
        @Override
        public void polled() {
            synchronized (this) {
                polled = true;
            }
        }
        
        public void ack() {
            polled = false;
        }
    }
}
