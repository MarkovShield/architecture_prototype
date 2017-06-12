package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.models.ClickStream;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Date;

@JsonTypeInfo (use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface ClickStreamModel extends Serializable {

    Date getTimeCreated();

    double clickStreamScore(ClickStream clickStream);
}
