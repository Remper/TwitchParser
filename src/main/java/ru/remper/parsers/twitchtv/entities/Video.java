package ru.remper.parsers.twitchtv.entities;

/**
 * Video entity that is being recorded
 */
public class Video {
    private String savePath;
    private Process process;

    public Video(Process process, String savePath)
    {
        this.savePath = savePath;
        this.process = process;
    }

    public boolean isFinishedRecording() {
        return !process.isAlive();
    }

    public void forceStop() {
        process.destroy();
    }
}
