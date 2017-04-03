package ch.hsr.markovshield.models;

/**
 * Created by maede on 03.04.2017.
 */
public class Click {
    private String session;
    private String url;

    public Click() {
    }

    public Click(String session, String url) {
        this.session = session;
        this.url = url;
    }

    public String getSession() {
        return session;
    }

    public String getUrl() {
        return url;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Click{" +
                "session='" + session + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
