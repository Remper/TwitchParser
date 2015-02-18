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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

/**
 * Main entry point for TwitchParser
 */
public class CorpusRecorder extends Thread {
    private String corpus;
    private KrakenAPI api;
    StreamConfiguration config;
    
    protected static Logger logger = LoggerFactory.getLogger(CorpusRecorder.class);
    
    public CorpusRecorder(String corpus, StreamConfiguration config) {
        this.config = config;
        this.corpus = corpus;
        this.api = new KrakenAPI(config.getToken());

        this.setPriority(Thread.NORM_PRIORITY);
        this.setDaemon(false);
    }
    
    public static CorpusRecorder resume(String corpus) throws ConfigurationException {
        //Reading configuration
        StreamConfiguration configuration = new StreamConfiguration(corpus);
        CorpusRecorder cr = new CorpusRecorder(corpus, configuration);
        cr.start();
        
        return cr;
    }
    
    @Override
    public void run() {
        record();
    }

    /**
     * Start recording
     */
    public void record() {
        HashSet<Stream> list = new HashSet<>();
        list.addAll(getWhitelistedStreams(String.join(",", config.getWhitelist())));
        logger.info("Querying whitelist channels... " + list.size() + " online");
        list.addAll(getTopStreams(config.getWildcard()));
        if (list.size() == 0) {
            logger.info("Nothing to record. Aborting");
            return;
        }
        logger.info("Recording "+list.size()+" streams");

        CorpusManager cm = null;
        try {
            cm = new CorpusManager(corpus, config.getUser(), config.getToken());
        } catch (Exception e) {
            logger.warn("Exception while initializing corpus: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return;
        }
        
        try {
            for (Stream stream : list) {
                cm.recordStream(stream);
            }
        } catch (Exception e) {
            logger.warn("Exception while submitting streams: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
        
        while (cm.isRecording()) {
            cm.watchUntilEvent();
        }
        cm.stopRecording();
        
        logger.info("Stopped recording");
    }

    public ArrayList<Stream> getWhitelistedStreams(String channels) {
        return api.getStreams(null, channels, new Filter().embeddable().hls().offset(0));
    }

    public ArrayList<Stream> getTopStreams(int limit) {
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
}
