package ch.hsr.markovshield.models;


import java.util.Date;

public interface FrequencyModel {
    Date getTimeCreated();
    int frequencyRating(ClickStream clickStream);
}
