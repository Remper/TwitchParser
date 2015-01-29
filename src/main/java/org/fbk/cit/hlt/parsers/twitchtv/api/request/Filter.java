package org.fbk.cit.hlt.parsers.twitchtv.api.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for easy filter selection for KrakenAPI
 */
public class Filter {
    private HashMap<String, String> params;

    public Filter() {
        params = new HashMap<>();
    }

    public Filter hls() {
        return hls(true);
    }

    public Filter hls(boolean value) {
        params.putIfAbsent("hls", Boolean.toString(value));
        return this;
    }

    public Filter embeddable() {
        return embeddable(true);
    }

    public Filter embeddable(boolean value) {
        params.putIfAbsent("embeddable", Boolean.toString(value));
        return this;
    }

    public Filter limit(int value) {
        params.putIfAbsent("limit", Integer.toString(value));
        return this;
    }

    public Filter offset(int value) {
        params.putIfAbsent("offset", Integer.toString(value));
        return this;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    /**
     * Modify the input Map to include all params from filter
     *
     * @param inputParams the input Map
     * @return inputParams for chaining
     */
    public Map<String, String> mergeParams(Map<String, String> inputParams) {
        inputParams.putAll(params);
        return inputParams;
    }
}
