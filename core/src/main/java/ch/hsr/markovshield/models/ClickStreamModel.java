package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Date;

@JsonTypeInfo (use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ClickStreamModel extends Serializable{

    Date getTimeCreated();

    double clickStreamScore(ClickStream clickStream);
}
