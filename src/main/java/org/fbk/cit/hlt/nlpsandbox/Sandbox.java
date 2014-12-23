package org.fbk.cit.hlt.nlpsandbox;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

//IRC library
import com.sorcix.sirc.*;
import ru.remper.parsers.twitchtv.CorpusManager;
import ru.remper.parsers.twitchtv.api.*;
import ru.remper.parsers.twitchtv.api.request.Filter;
import ru.remper.parsers.twitchtv.api.result.*;
import ru.remper.parsers.twitchtv.stream.VLCWrapper;

/**
 * Some code goes here
 */
public class Sandbox {
    public static void main(String args[]) {
        System.out.println("Starting the system");

        String username = "Remper";
        String token = "9w2hck4i2n9d8a7918b7t1h1w1e8rf";

        KrakenAPI api = new KrakenAPI(token);
        ArrayList<Stream> list = api.getStreams(null, null, new Filter().embeddable().hls().limit(1).offset(0));
        if (list.size() != 1) {
            System.out.println("Didn't select single stream. Aborting");
        }

        CorpusManager cm = null;
        try {
            cm = new CorpusManager("/Users/remper/Downloads/corpus");
            cm.initAPI(username, token);
            cm.recordStream(list.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        (new EventLoop(cm)).start();

        System.out.println("Main execution is ended");
    }
}
