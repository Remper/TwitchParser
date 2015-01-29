package org.fbk.cit.hlt.parsers.twitchtv.entities;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * TwitchTV broadcast entity (ongoing or past broadcast)
 */
public class Stream {
    private Broadcaster broadcaster;
    private Video video;
    private Chat chat;

    private Date started;
    private Date started_local;
    private Duration offset;
    private Duration duration;
    private Date ended_local;

    public Stream(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.video = null;
        this.chat = new Chat(this);

        this.started = null;
        this.started_local = null;
        this.ended_local = null;
        this.duration = Duration.ofSeconds(0);
        this.offset = Duration.ofSeconds(0);
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }

    public Video getVideo() {
        return video;
    }

    public void attachVideo(Video video) {
        this.video = video;
    }

    public Chat getChat() {
        return chat;
    }

    public Duration getDuration() {
        if (this.isRecorded()) {
            return duration;
        }
        return Duration.between(started_local.toInstant(), Instant.now());
    }

    public Instant getProjectedStreamInstant() throws Exception {
        if (!this.isStarted()) {
            throw new Exception("Start the recording first");
        }
        return Instant.now().minus(this.offset);
    }

    public boolean isStarted() {
        return this.started == null;
    }

    public boolean isRecorded() {
        return this.ended_local == null;
    }

    public void startRecording() throws Exception {
        this.startRecording(new Date());
    }

    public void startRecording(Date startTime) throws Exception {
        if (this.isStarted()) {
            throw new Exception("The recording of this stream has already started");
        }
        this.started_local = new Date();
        this.started = (Date) startTime.clone();
        this.offset = Duration.between(this.started.toInstant(), this.started_local.toInstant());
    }

    public void stopRecording() {
        try {
            this.stopRecording(this.getProjectedStreamInstant());
        } catch (Exception e) {
            //Means that you are trying to stop recording which hasn't been started yet. Do nothing
        }
    }

    public void stopRecording(Date endTime) {
        try {
            this.stopRecording(endTime.toInstant());
        } catch (Exception e) {
            //Means that you are trying to stop recording which hasn't been started yet. Do nothing
        }
    }

    private void stopRecording(Instant endTime) throws Exception {
        if (!this.isStarted()) {
            throw new Exception("Start the recording first");
        }
        this.ended_local = new Date();
        this.duration = Duration.between(this.started.toInstant(), endTime);
    }
}
