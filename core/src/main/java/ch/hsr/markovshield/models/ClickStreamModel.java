package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Date;

@JsonTypeInfo (use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ClickStreamModel {

    Date getTimeCreated();

    double clickStreamScore(ClickStream clickStream);
}
