package ch.hsr.markovshield;

import org.apache.avro.specific.SpecificRecord;

import java.util.Objects;

/**
 * Created by maede on 27.03.2017.
 */
public class Click {

    private final String sessionId;
    private final String url;

    public Click(String sessionId, String url){
        this.sessionId = sessionId;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Click{" +
                "sessionId='" + sessionId + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Click click = (Click) o;
        return Objects.equals(sessionId, click.sessionId) &&
                Objects.equals(url, click.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, url);
    }

    public String getSessionId() {

        return sessionId;
    }

    public String getUrl() {
        return url;
    }
}
