package ru.remper.parsers.twitchtv.api;

import org.apache.commons.io.IOUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class with common functions for all three apis
 */
public abstract class AbstractAPI {
    protected String schema;
    protected String host;
    protected String token;
    protected String apiOffset;

    protected AbstractAPI(String schema, String host, String token, String apiOffset)
    {
        this.schema = schema;
        this.host = host;
        this.token = token;
        this.apiOffset = apiOffset;
    }

    protected JsonObject execute(String method) throws IOException
    {
        return this.execute(method, new HashMap<>());
    }

    protected JsonObject execute(String method, Map<String, String> params)
            throws IOException
    {
        try(InputStream is = this.getStream(method, params); JsonReader reader = Json.createReader(is)) {
            return reader.readObject();
        }
    }

    protected InputStream getStream(String method, Map<String, String> params)
            throws IOException
    {
        System.setProperty("http.keepAlive", "false");
        StringBuilder sb = new StringBuilder(255);
        sb.append("?oauth_token=");
        sb.append(this.token);
        for(Map.Entry<String, String> e : params.entrySet()) {
            sb.append("&");
            sb.append(e.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(e.getValue(), "UTF-8"));
        }

        URL url = new URL(this.schema, this.host, "/"+this.apiOffset+"/" + method + sb.toString());
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        conn.setRequestProperty("User-Agent", "Remper's Corpora parser");
        conn.connect();
        return conn.getInputStream();
    }
}
