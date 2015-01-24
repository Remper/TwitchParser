package org.fbk.cit.hlt.parsers.hls.twitchtv.stream;


import org.fbk.cit.hlt.parsers.hls.twitchtv.entities.Video;

import java.io.*;
import java.util.ArrayList;

/**
 * Class that handles interaction with VLC
 */
public class VLCWrapper {
    private String vlcPath;
    private Process process;
    private File saveFile;

    public VLCWrapper() {
        this("vlc");
    }

    public VLCWrapper(String vlcPath) {
        this.vlcPath = vlcPath;
    }

    public Video convertToVideo() throws Exception {
        if (!process.isAlive()) {
            throw new Exception("Start the recording first");
        }

        return new Video(process, saveFile.getAbsolutePath());
    }

    public boolean startRecording(String playlist, String savePath) {
        //Writing a playlist
        saveFile = new File(savePath+".ts");
        File playlistFile = new File(savePath+".playlist");
        if (saveFile.exists() || playlistFile.exists()
                || !writePlaylist(playlistFile, playlist)) {
            return false;
        }

        try {
            ArrayList<String> command = new ArrayList<>();
            command.add(this.vlcPath);
            command.add("-I");
            command.add("dummy");
            command.add("--sout-avcodec-strict=-2");
            command.add("--sout-file-append");
            command.add("--sout");
            command.add("file/ts:"+saveFile.getAbsolutePath());
            command.add(playlistFile.getAbsolutePath());
            command.add("vlc://quit");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(savePath+".log")));
            this.process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            if (!playlistFile.delete()) {
                System.err.println("Playlist file at \""+playlistFile.getAbsolutePath()+"\" may not have been deleted");
            }
            return false;
        }

        return true;
    }

    private boolean writePlaylist(File file, String playlist) {
        try (FileWriter writer = new FileWriter(file); BufferedWriter bufWriter = new BufferedWriter(writer)) {
            bufWriter.write(playlist);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Process getProcess() {
        return process;
    }

    public File getSaveFile() {
        return saveFile;
    }
}
