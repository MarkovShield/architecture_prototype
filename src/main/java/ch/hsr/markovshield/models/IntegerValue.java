package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties ({"empty"})
public class IntegerValue {
    private final int value;

    public IntegerValue(int value) {
        this.value = value;
    }

    public boolean isEmpty(){
        return value == -1;
    }

    public int getValue(){
        return value;
    }

}
