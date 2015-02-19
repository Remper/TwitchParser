package org.fbk.cit.hlt.parsers.twitchtv.inputs;

import org.fbk.cit.hlt.parsers.twitchtv.entities.Broadcaster;
import org.fbk.cit.hlt.parsers.twitchtv.entities.User;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * IRC messages part
 */
public class IRC extends ListenerAdapter<PircBotX> {
    protected static Logger logger = LoggerFactory.getLogger(IRC.class);
    
    public static final String CHANNEL_PREFIX = "#";
    
    MultiBotManager<PircBotX> manager = new MultiBotManager<>();
    Configuration.Builder<PircBotX> defaultConfig;
    IRCReceiver receiver;
    Set<String> channels = new HashSet<>();
    ArrayList<String> servers = new ArrayList<>();
    String token;
    String username;
    Status status = Status.DEAD;
    
    public IRC(IRCReceiver receiver, String token, String username) {
        this.receiver = receiver;
        this.token = token;
        this.username = username;
        
        defaultConfig = new Configuration.Builder<>()
                .setName(username)
                .setLogin(username)
                .setServerPassword("oauth:" + token)
                .setAutoReconnect(true)
                .addListener(this);
    }
    
    public void recreateBots() {
        if (status != Status.DEAD) {
            logger.warn("Cannot recreate bots on working system");
            return;
        }
        
        manager = new MultiBotManager<>();
        for (String server : servers) {
            manager.addBot(buildForServer(server));
        }
    }

    public void addChannel(String channel) {
        if (!channel.startsWith(CHANNEL_PREFIX)) {
            channel = CHANNEL_PREFIX+channel;
        }
        channels.add(channel);
        defaultConfig.addAutoJoinChannel(channel);
        if (status != Status.ALIVE) {
            return;
        }
        
        for (PircBotX bot : manager.getBots()) {
            bot.sendIRC().joinChannel(channel);
        }
    }
    
    public void addServer(String ip) {
        servers.add(ip);
        manager.addBot(buildForServer(ip));
    }
    
    private Configuration<PircBotX> buildForServer(String hostname) {
        return new Configuration.Builder<>(defaultConfig)
                .setServerHostname(hostname)
                .buildConfiguration();
    }
    
    public void lazyStart() {
        if (servers.size() == 0 || channels.size() == 0 || status != Status.DEAD) {
            return;
        }
        
        start();
    }
    
    public void start() {
        if (status != Status.DEAD) {
            logger.info("Trying to start IRC recording multiple times");
            return;
        }
        
        recreateBots();
        manager.start();
        status = Status.ALIVE;
    }
    
    public ArrayList<String> getLiveChannels() {
        ArrayList<String> result = new ArrayList<>();
        for (PircBotX bot : manager.getBots()) {
            if (!bot.isConnected()) {
                continue;
            }
            
            for (Channel channel : bot.getUserChannelDao().getAllChannels()) {
                result.add(sanitizeBroadcaster(channel.getName()));
            }
        }
        
        return result;
    }

    //Methods that handle events from servers
    @Override
    public void onConnect(ConnectEvent event) {
        
    }
    
    @Override
    public void onDisconnect(DisconnectEvent event) {
        if (status == Status.DYING) {
            return;
        }

        try {
            event.getBot().startBot();
        } catch (IOException | IrcException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onMessage(MessageEvent event)  {
        String caster = sanitizeBroadcaster(event.getChannel().getName());
        receiver.message(caster, event.getUser().getNick(), event.getMessage(), event.getTimestamp());
    }
    
    private String sanitizeBroadcaster(String caster) {
        if (caster.startsWith(CHANNEL_PREFIX)) {
            return caster.substring(1);
        }
        return caster;
    }
    
    public void stop() {
        try {
            status = Status.DYING;
            manager.stopAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        receiver.flush();
        status = Status.DEAD;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        ALIVE, DEAD, DYING
    }
}
