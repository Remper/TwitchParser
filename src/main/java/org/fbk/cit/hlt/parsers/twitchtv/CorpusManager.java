package org.fbk.cit.hlt.parsers.twitchtv;

import org.fbk.cit.hlt.parsers.twitchtv.api.SecretAPI;
import org.fbk.cit.hlt.parsers.twitchtv.api.Usher;
import org.fbk.cit.hlt.parsers.twitchtv.entities.Stream;
import org.fbk.cit.hlt.parsers.twitchtv.inputs.*;
import org.fbk.cit.hlt.parsers.twitchtv.stream.HLSWrapper;
import org.fbk.cit.hlt.parsers.twitchtv.api.result.AccessToken;
import org.fbk.cit.hlt.parsers.twitchtv.entities.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Corpus manager that handles storing the corpora
 */
public class CorpusManager {
    protected static Logger logger = LoggerFactory.getLogger(CorpusManager.class);
    
    private File corpusFolder;
    private HashMap<String, Broadcaster> broadcasters;
    private HashMap<Broadcaster, Stream> recording;
    private IRC irc;
    private String eventIrcServer = null;
    private final StreamDataFileWriter streamDataWriter;
    private SecretAPI secretAPI;
    private Usher usherAPI;
    private Collection<Receiver> receivers;
    private int chatReconnectionCounter = 5;

    public CorpusManager(String corpusFolder, String name, String token) throws Exception {
        File folder = new File(corpusFolder);
        if (!folder.exists()) {
            throw new Exception("Corpus directory doesn't exist");
        }
        if (!folder.canWrite()) {
            throw new Exception("Corpus directory is not writable");
        }
        if (!folder.isDirectory()) {
            throw new Exception("Corpus directory should actually be a directory");
        }
        this.corpusFolder = folder;
        broadcasters = new HashMap<>();
        recording = new HashMap<>();
        addExistingBroadcasters();
        
        String ircFile = getChildPath(folder.getAbsolutePath(), "irc");
        String streamDataFile = getChildPath(folder.getAbsolutePath(), "stream_data");
        String nowRecordingFile = getChildPath(folder.getAbsolutePath(), "now_recording");
        
        IRCFileWriter ircReceiver = new IRCFileWriter(ircFile);
        streamDataWriter = new StreamDataFileWriter(streamDataFile);

        receivers = new ArrayList<>();
        receivers.add(ircReceiver);
        receivers.add(streamDataWriter);
        receivers.add(new NowRecordingFileWriter(nowRecordingFile));
        
        irc = new IRC(ircReceiver, token, name);
        irc.addServer("irc.twitch.tv");
        usherAPI = new Usher(token);
        secretAPI = new SecretAPI(token);
    }

    public static CorpusManager createCorpus(String corpusFolder, String name, String token) throws Exception {
        if (!(new File(corpusFolder)).mkdirs()) {
            throw new Exception("Target directory is not empty or not writable. Aborting");
        }
        return new CorpusManager(corpusFolder, name, token);
    }

    private void addExistingBroadcasters() throws Exception {
        File[] files = corpusFolder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                addBroadcaster(file.getName());
            }
        }
    }

    private String getChildPath(String parent, String child) {
        return parent + "/" + child;
    }

    /**
     * Let CorpusManager to handle the recording of the Stream object received from KrakenAPI
     *
     * @param stream Stream object received from KrakenAPI
     */
    public Broadcaster recordStream(org.fbk.cit.hlt.parsers.twitchtv.api.result.Stream stream) throws Exception {
        Broadcaster caster = addBroadcaster(stream.getName());
        if (recording.containsKey(caster)) {
            logger.info("Stream \""+stream.getDisplayName()+"\" is already being recorded");
            return caster;
        }
        logger.info("Starting recording \""+stream.getDisplayName()+"\"");

        //Selecting stream folder
        String curDate = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        String streamFolderPath = getChildPath(getBroadcasterFolder(caster).getAbsolutePath(), curDate);
        File streamFolder = null;
        for (int modifier = 0; modifier < 200; modifier++) {
            streamFolder = new File(streamFolderPath+"-"+Integer.toString(modifier));
            if (streamFolder.exists()) {
                continue;
            }

            break;
        }

        if (!streamFolder.mkdir()) {
            throw new Exception("Failed to create stream folder");
        }

        AccessToken tok = secretAPI.getAccessToken(stream.getName());
        if (tok == null) {
            throw new Exception("Failed to acquire read permissions to the stream");
        }
        String playlistUrl = usherAPI.getPlaylistUrl(tok);
        if (playlistUrl == null) {
            throw new Exception("Failed to get playlist URL");
        }
        HLSWrapper wrapper = new HLSWrapper(streamFolder, playlistUrl, caster.getName());
        wrapper.start();

        Stream streamObj = new Stream(caster);
        streamObj.attachVideo(wrapper.convertToVideo());
        recording.put(caster, streamObj);
        if (eventIrcServer == null) {
            ArrayList<String> servers = secretAPI.getChatServers(caster.getName());
            if (servers.size() != 0) {
                logger.info("Found event server, adding to connect list");
                Random random = new Random(System.currentTimeMillis());
                eventIrcServer = servers.get(random.nextInt(servers.size()));
                irc.addServer(eventIrcServer);
            }
        }
        irc.addChannel(stream.getName());
        irc.lazyStart();

        return caster;
    }

    public void stopRecording(Broadcaster caster) {
        Stream stream = recording.get(caster);
        if (stream == null) {
            return;
        }

        stream.getVideo().forceStop();
        recording.remove(caster);
    }

    public void stopRecording() {
        recording.keySet().forEach(this::stopRecording);
        irc.stop();
    }

    public void checkSystem() {
        Broadcaster dead = null;
        while ((dead = getFirstDeadStream()) != null) {
            logger.info(dead.getName()+" is dead. Stopping recording");
            stopRecording(dead);
        }

        processReceivers();
        if (!isRecording()) {
            logger.info("System is not recording");
            return;
        }
        
        if (irc.getLiveChannels().size() == 0) {
            chatReconnectionCounter--;
            if (chatReconnectionCounter <= 0) {
                irc.stop();
                irc.start();
                chatReconnectionCounter = 5;
            }
        }
    }
    
    public void watchUntilEvent() {
        Broadcaster dead = null;
        while ((dead = getFirstDeadStream()) == null && isRecording() && !Thread.interrupted()) {
            try {
                processReceivers();
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (dead != null) {
            this.stopRecording(dead);
        }
    }
    
    public void processReceivers() {
        for (Receiver receiver : receivers) {
            if (receiver instanceof NowRecordingFileWriter) {
                ((NowRecordingFileWriter) receiver).dump(recording.keySet());
            }
        }
    }
    
    public boolean isRecording() {
        return getOnlineCount() > 0;
    }
    
    public int getOnlineCount() {
        return recording.size();
    }

    public Broadcaster getFirstDeadStream() {
        for (Map.Entry<Broadcaster, Stream> entry : recording.entrySet()) {
            if (entry.getValue().getVideo().isFinishedRecording()) {
                return entry.getKey();
            }
        }

        return null;
    }

    public File getBroadcasterFolder(Broadcaster caster) {
        return new File(getChildPath(this.corpusFolder.getAbsolutePath(), caster.getName()));
    }

    public boolean broadcasterExists(String broadcaster) {
        return broadcasters.containsKey(broadcaster);
    }

    public Broadcaster addBroadcaster(String broadcaster) throws Exception {
        Broadcaster caster = broadcasters.get(broadcaster);
        if (caster == null) {
            caster = new Broadcaster(broadcaster);
            broadcasters.put(broadcaster, caster);
        }

        //Now we have to maintain our directory structure
        File casterFolder = new File(getChildPath(corpusFolder.getPath(), broadcaster));
        if (casterFolder.exists() && casterFolder.isDirectory()) {
            System.out.println("Directory for "+broadcaster+" already exists. Skipping creation");
            return caster;
        }

        if (!casterFolder.mkdir()) {
            throw new Exception("Can't create directory for "+broadcaster+". Aborting");
        }
        return caster;
    }

    public Collection<Broadcaster> getBroadcasters() {
        return broadcasters.values();
    }

    public StreamDataFileWriter getStreamDataWriter() {
        return streamDataWriter;
    }
}
