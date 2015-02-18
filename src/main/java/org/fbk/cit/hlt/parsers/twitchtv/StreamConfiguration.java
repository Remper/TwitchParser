package org.fbk.cit.hlt.parsers.twitchtv;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class that manages configuration along with stream-config.properties 
 */
public class StreamConfiguration {
    public static final String CONFIG_FILENAME = "/stream-config.properties";
    
    public static final String ROOT_NAMESPACE = "twitchtv";
    public static final String PARAM_WILDCARD = "wildcard";
    public static final String PARAM_WHITELIST = "whitelist";
    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_USER = "user";
    
    protected int wildcard;
    protected String[] whitelist;
    protected String token;
    protected String user;
    
    public StreamConfiguration() throws ConfigurationException {
        load(CorpusRecorder.class.getResource(CONFIG_FILENAME));
    }
    
    public StreamConfiguration(String corpusDir) throws ConfigurationException {
        try {
            load(new URL("file://" + corpusDir + CONFIG_FILENAME));
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Malformed corpus directory");
        }
    }
    
    private void load(URL filename) throws ConfigurationException{
        Configuration config = new PropertiesConfiguration(filename);
        try {
            wildcard = config.getInt(getParamName(PARAM_WILDCARD));
            whitelist = config.getStringArray(getParamName(PARAM_WHITELIST));
            if (config.containsKey(getParamName(PARAM_TOKEN))) {
                token = config.getString(getParamName(PARAM_TOKEN));
            }
            if (config.containsKey(getParamName(PARAM_USER))) {
                user = config.getString(getParamName(PARAM_USER));
            }
        } catch(Exception e) {
            throw new ConfigurationException("Parameter parsing failed: "+e.getClass().getSimpleName()+" "+e.getMessage());
        }
    }
    
    public void replaceWithCommandLine(CommandLine line) throws ConfigurationException {
        if (line.hasOption(PARAM_WHITELIST)) {
            String[] channels = line.getOptionValue(PARAM_WHITELIST).split(",");
            if (channels.length > 0) {
                this.whitelist = channels;
            }
        }
        if (line.hasOption(PARAM_WILDCARD)) {
            wildcard = Integer.parseInt(line.getOptionValue(PARAM_WILDCARD));
        }
        if (!line.hasOption(PARAM_USER) || !line.hasOption(PARAM_TOKEN)) {
            throw new ConfigurationException("Required parameters missing");
        }
        token = line.getOptionValue(PARAM_TOKEN);
        user = line.getOptionValue(PARAM_USER);
    }
    
    private String getParamName(String param) {
        return ROOT_NAMESPACE + "." + param;
    }

    public String[] getWhitelist() {
        return whitelist;
    }

    public int getWildcard() {
        return wildcard;
    }

    public String getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public void setWildcard(int wildcard) {
        this.wildcard = wildcard;
    }

    public void setWhitelist(String[] whitelist) {
        this.whitelist = whitelist;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
