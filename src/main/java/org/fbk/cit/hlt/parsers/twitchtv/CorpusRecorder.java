package org.fbk.cit.hlt.parsers.twitchtv;

import org.apache.commons.cli.*;
import org.fbk.cit.hlt.parsers.twitchtv.api.KrakenAPI;
import org.fbk.cit.hlt.parsers.twitchtv.api.request.Filter;
import org.fbk.cit.hlt.parsers.twitchtv.api.result.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Main entry point for TwitchParser
 */
public class CorpusRecorder {
    private Mode mode;
    private String corpus;
    private String user;
    private String token;
    private KrakenAPI api;
    
    private String game = null;
    private String name;
    
    protected Logger logger = LoggerFactory.getLogger(CorpusRecorder.class);

    public CorpusRecorder(Mode mode, String corpus, String user, String token) {
        this.mode = mode;
        this.user = user;
        this.token = token;
        this.api = new KrakenAPI(token);
        this.corpus = corpus;
    }
    
    public void record() {
        ArrayList<Stream> list;
        switch (mode) {
            case SINGLE:
                list = getSingleStreamByName(name);
                break;
            default:
            case TOP:
                list = getTopStreamsByGame(game, 5);
                break;
        }
        if (list.size() == 0) {
            logger.info("Nothing to record. Aborting");
            return;
        }
        logger.info("Recording "+list.size()+" streams");

        CorpusManager cm = null;
        try {
            cm = new CorpusManager(corpus);
            cm.initAPI(user, token);
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

    public ArrayList<Stream> getSingleStreamByName(String name) {
        return api.getStreams(null, name, new Filter().embeddable().hls().limit(1).offset(0));
    }

    public ArrayList<Stream> getTopStreamsByGame(String game, int limit) {
        return api.getStreams(game, null, new Filter().embeddable().hls().limit(limit).offset(0));
    }
    
    public Mode getMode() {
        return mode;
    }

    public String getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void main(String args[]) {
        //Options specification
        //TODO: create options explicitly to get rid of extra parsing logic
        Options options = new Options();
        options.addOption("n", "name", true, "Name of the stream to download");
        options.addOption("m", "mode", true, "Main command");
        options.addOption("g", "game", true, "Filter streams by game (if relevant)");
        options.addOption("c", "corpus", true, "Folder to where we should store the data");
        options.addOption("u", "user", true, "Twitch login");
        options.addOption("t", "token", true, "Twitch authorization token");

        CommandLineParser parser = new PosixParser();
        CommandLine line;
        CorpusRecorder recorder;
        try {
            line = parser.parse(options, args);
            
            Mode mode = Mode.SINGLE;
            if (!line.hasOption("corpus")) {
                throw new ParseException("Missing parameter corpus");
            }
            if (!line.hasOption("user")) {
                throw new ParseException("Missing parameter user");
            }
            if (!line.hasOption("token")) {
                throw new ParseException("Missing parameter token");
            }
            if (line.hasOption("mode")) {
                mode = Mode.valueOf(line.getOptionValue("mode"));
            }
            recorder = new CorpusRecorder(mode, line.getOptionValue("corpus"), line.getOptionValue("user"), line.getOptionValue("token"));
            switch (mode) {
                case SINGLE:
                    if (!line.hasOption("name")) {
                        throw new ParseException("Missing parameter name for mode SINGLE");
                    }
                    recorder.setName(line.getOptionValue("name"));
                    break;
                case TOP:
                    String game = null;
                    if (line.hasOption("game")) {
                        game = line.getOptionValue("game");
                    }
                    recorder.setGame(game);
                    break;
            }
        } catch (Exception e) {
            String footer = "\nError: "+e.getMessage();
            new HelpFormatter().printHelp(400, "java -jar twitchtv-0.1.jar", "\n", options, footer, true);
            return;
        }
        
        recorder.record();
    }
    
    public enum Mode {
        TOP, SINGLE
    }
}
