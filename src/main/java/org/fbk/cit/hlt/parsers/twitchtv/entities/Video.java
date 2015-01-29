package org.fbk.cit.hlt.parsers.twitchtv.entities;

/**
 * Video entity that is being recorded
 */
public class Video {
    private String savePath;
    private Thread thread;

    public Video(Thread thread, String savePath)
    {
        this.savePath = savePath;
        this.thread = thread;
    }

    public boolean isFinishedRecording() {
        return !thread.isAlive();
    }

    public void forceStop() {
        if (thread.isInterrupted()) {
            return;
        }
        thread.interrupt();
    }

    public String getSavePath() {
        return savePath;
    }
}
