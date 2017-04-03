package ch.hsr.markovshield.models;

/**
 * Created by maede on 03.04.2017.
 */
public class Session {
    private String session;
    private String user;

    public Session() {
    }

    public Session(String session, String user) {
        this.session = session;
        this.user = user;
    }

    public String getSession() {
        return session;
    }

    public String getUser() {
        return user;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Session{" +
                "session='" + session + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
