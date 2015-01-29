package org.fbk.cit.hlt.parsers.hls.twitchtv;

import org.fbk.cit.hlt.parsers.hls.twitchtv.api.SecretAPI;
import org.fbk.cit.hlt.parsers.hls.twitchtv.api.Usher;
import org.fbk.cit.hlt.parsers.hls.twitchtv.api.result.Stream;
import org.fbk.cit.hlt.parsers.hls.twitchtv.stream.IrcWrapper;
import org.fbk.cit.hlt.parsers.hls.twitchtv.stream.VLCWrapper;
import org.fbk.cit.hlt.parsers.hls.twitchtv.api.result.AccessToken;
import org.fbk.cit.hlt.parsers.hls.twitchtv.entities.Broadcaster;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Corpus manager that handles storing the corpora
 */
public class CorpusManager {
    private File corpusFolder;
    private HashMap<String, Broadcaster> broadcasters;
    private HashMap<Broadcaster, org.fbk.cit.hlt.parsers.hls.twitchtv.entities.Stream> recording;
    private IrcWrapper ircWrapper;
    private SecretAPI secretAPI;
    private Usher usherAPI;

    public CorpusManager(String corpusFolder) throws Exception {
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

        ircWrapper = null;
        secretAPI = null;
        usherAPI = null;
    }

    public static CorpusManager createCorpus(String corpusFolder) throws Exception {
        if (!(new File(corpusFolder)).mkdirs()) {
            throw new Exception("Target directory is not empty or not writable. Aborting");
        }
        return new CorpusManager(corpusFolder);
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
    public Broadcaster recordStream(Stream stream) throws Exception {
        if (secretAPI == null) {
            throw new Exception("Can't record stream without API support");
        }

        System.out.println("Starting recording \""+stream.getDisplayName()+"\"");
        Broadcaster caster = addBroadcaster(stream.getName());

        //Selecting stream folder
        String curDate = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        String streamFolderPath = getChildPath(getBroadcasterFolder(caster).getAbsolutePath(), curDate);
        File streamFolder = null;
        for (int modifier = 0; modifier < 100; modifier++) {
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
        String playlist = usherAPI.getPlaylist(tok);
        VLCWrapper wrapper = new VLCWrapper("/Applications/VLC.app/Contents/MacOS/VLC");
        if (!wrapper.startRecording(playlist, getChildPath(streamFolder.getAbsolutePath(), "video"))) {
            throw new Exception("Failed to start stream recording");
        }

        org.fbk.cit.hlt.parsers.hls.twitchtv.entities.Stream streamObj = new org.fbk.cit.hlt.parsers.hls.twitchtv.entities.Stream(caster);
        streamObj.attachVideo(wrapper.convertToVideo());
        recording.put(caster, streamObj);
        ircWrapper.startRecording(stream.getName(), getChildPath(streamFolder.getAbsolutePath(), "chat.txt"));

        return caster;
    }

    public void stopRecording(Broadcaster caster) {
        org.fbk.cit.hlt.parsers.hls.twitchtv.entities.Stream stream = recording.get(caster);
        if (stream == null) {
            return;
        }

        stream.getVideo().forceStop();
        ircWrapper.stopRecording(caster.getName());
    }

    public void stopRecording() {
        recording.keySet().forEach(this::stopRecording);
        ircWrapper.disconnect();
    }

    public void watchUntilEvent() {
        Broadcaster dead = null;
        while ((dead = getFirstDeadStream()) == null && recording.size() > 0) {
            try {
                Thread.sleep(4000);
                System.out.print('.');
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (dead != null) {
            this.stopRecording(dead);
        }
    }

    public Broadcaster getFirstDeadStream() {
        for (Map.Entry<Broadcaster, org.fbk.cit.hlt.parsers.hls.twitchtv.entities.Stream> entry : recording.entrySet()) {
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

    public void initAPI(String name, String token) {
        ircWrapper = new IrcWrapper(token, name);
        usherAPI = new Usher(token);
        secretAPI = new SecretAPI(token);
    }
}
