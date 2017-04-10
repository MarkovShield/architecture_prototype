package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by maede on 10.04.2017.
 */
public class UrlId {
    private final int id;

    public UrlId(@JsonProperty ("id") int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
