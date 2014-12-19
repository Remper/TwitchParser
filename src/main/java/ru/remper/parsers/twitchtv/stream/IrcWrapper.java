package ru.remper.parsers.twitchtv.stream;

import com.sorcix.sirc.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Library that wraps an IRC connection and reports to Stream entity
 */
public class IrcWrapper implements ServerListener, MessageListener {
    private String host;
    private String token;
    private String nick;
    private IrcConnection conn;
    private HashMap<String, BufferedWriter> recording;
    private SimpleDateFormat dateFormat;

    private int reconnectAttemps = 5;

    public IrcWrapper(String token, String nick) {
        this("irc.twitch.tv", token, nick);
    }

    public IrcWrapper(String host, String token, String nick) {
        this.host = host;
        this.token = token;
        this.nick = nick;
        initIrcConnection();

        recording = new HashMap<>();
        dateFormat = new SimpleDateFormat();
    }

    public void initIrcConnection()
    {
        conn = new IrcConnection(host, "oauth:".concat(token));
        conn.addMessageListener(this);
        conn.addServerListener(this);
        conn.setNick(nick);
    }

    public void forbidToReconnect()
    {
        reconnectAttemps = 0;
    }

    public void startRecording(String channel, String savePath) throws Exception {
        channel = "#"+channel;
        printLog("Starting recording "+channel);
        BufferedWriter stream = recording.get(channel);
        if (stream != null) {
            throw new Exception("You have to stop the recording first");
        }

        FileWriter writer = new FileWriter(savePath);
        stream = new BufferedWriter(writer);
        recording.put(channel, stream);

        if (!this.conn.isConnected()) {
            connect();
            return;
        }

        joinChannel(channel);
    }

    public void stopRecording(String channel) {
        channel = "#"+channel;
        printLog("Stopping recording "+channel);
        BufferedWriter writer = recording.remove(channel);
        if (writer != null) {
            if (conn.isConnected()) {
                conn.sendRaw("PART "+channel);
            }
            try {
                writer.close();
            } catch (IOException e) {
                printLog("Failed to close writer for channel \""+channel+"\"");
                e.printStackTrace();
            }
        } else {
            printLog("Writer has already closed "+channel);
        }
    }

    private void connect() {
        try {
            initIrcConnection();
            conn.connect();
        } catch (IOException | NickNameException | PasswordException e) {
            printLog("Failed to connect to server");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        printLog("Disconnect command received");
        forbidToReconnect();
        conn.disconnect();
        recording.keySet().forEach(this::stopRecording);
    }

    private void joinChannel(String channel) {
        conn.sendRaw("JOIN " + channel);
    }

    private void joinChannels() {
        recording.keySet().forEach(this::joinChannel);
    }

    @Override
    public void onMessage(IrcConnection ircConnection, User user, Channel channel, String s) {
        System.out.print("#");
        BufferedWriter writer = recording.get(channel.getName());
        if (writer != null) {
            try {
                writer.write("["+dateFormat.format(new Date())+"] <"+user.getNick()+"> ::: "+s+"\n");
            } catch (IOException e) {
                printLog("Failed to write message to file");
                e.printStackTrace();
            }
        } else {
            printLog("Couldn't write chat message since there is no writer available "+channel);
        }
    }

    @Override
    public void onConnect(IrcConnection ircConnection) {
        printLog("IRC Connected");
        //After connection we have to connect to all the channels
        joinChannels();
    }

    @Override
    public void onDisconnect(IrcConnection ircConnection) {
        printLog("IRC Disconnected");
        if (reconnectAttemps > 0) {
            reconnectAttemps--;
            printLog("Trying to reconnect... Attempts left: "+Integer.toString(reconnectAttemps));
            connect();
        }
    }

    @Override
    public void onInvite(IrcConnection ircConnection, User user, User user1, Channel channel) {

    }

    @Override
    public void onJoin(IrcConnection ircConnection, Channel channel, User user) {
        printLog("Joined to channel: " + channel);
    }

    @Override
    public void onKick(IrcConnection ircConnection, Channel channel, User user, User user1, String s) {
        if (user1.getNick().equals(nick)) {
            printLog("Kicked from channel: "+channel);
            printLog("    Reason: \""+s+"\"");
            printLog("    Trying to reconnect...");
            joinChannel(channel.getName());
        }
    }

    @Override
    public void onMode(IrcConnection ircConnection, Channel channel, User user, String s) {

    }

    @Override
    public void onMotd(IrcConnection ircConnection, String s) {

    }

    @Override
    public void onNick(IrcConnection ircConnection, User user, User user1) {

    }

    @Override
    public void onPart(IrcConnection ircConnection, Channel channel, User user, String s) {
        printLog("Left the channel: " + channel);
    }

    @Override
    public void onQuit(IrcConnection ircConnection, User user, String s) {

    }

    @Override
    public void onTopic(IrcConnection ircConnection, Channel channel, User user, String s) {

    }

    @Override
    public void onAction(IrcConnection ircConnection, User user, Channel channel, String s) {

    }

    @Override
    public void onAction(IrcConnection ircConnection, User user, String s) {

    }

    @Override
    public void onCtcpReply(IrcConnection ircConnection, User user, String s, String s1) {

    }

    @Override
    public void onNotice(IrcConnection ircConnection, User user, Channel channel, String s) {

    }

    @Override
    public void onNotice(IrcConnection ircConnection, User user, String s) {

    }

    @Override
    public void onPrivateMessage(IrcConnection ircConnection, User user, String s) {

    }

    private void printLog(String message) {
        System.out.println("IRC :: "+message);
    }

    public String getHost() {
        return host;
    }

    public String getToken() {
        return token;
    }

    public String getNick() {
        return nick;
    }
}
