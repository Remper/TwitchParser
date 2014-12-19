package org.fbk.cit.hlt.nlpsandbox;

import ru.remper.parsers.twitchtv.CorpusManager;

/**
 * Event loop to check if everything is up and running
 */
public class EventLoop extends Thread {
    CorpusManager cm;

    public EventLoop(CorpusManager cm) {
        this.setName("Sandbox: event loop");
        this.setPriority(Thread.NORM_PRIORITY);
        this.setDaemon(false);
        this.cm = cm;
    }

    /**
     * Checks the input stream for new messages.
     */
    @Override
    public void run() {
        System.out.println("Event loop begins");
        cm.watchUntilEvent();
        cm.stopRecording();
        System.out.println("Event loop ends");
    }
}
