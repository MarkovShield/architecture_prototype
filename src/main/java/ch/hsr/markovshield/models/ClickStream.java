package ch.hsr.markovshield.models;


import java.util.List;

/**
 * Created by maede on 03.04.2017.
 */
public class ClickStream {
    private String user;
    private String session;
    private List<Click> clicks;
    private UserModel userModel;

    public ClickStream() {
    }

    public ClickStream(String user, String session, List<Click> clicks, UserModel userModel) {
        this.user = user;
        this.session = session;
        this.clicks = clicks;
        this.userModel = userModel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public List<Click> getClicks() {
        return clicks;
    }

    public void setClicks(List<Click> clicks) {
        this.clicks = clicks;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    @Override
    public String toString() {
        return "ClickStream{" +
                "user='" + user + '\'' +
                ", session='" + session + '\'' +
                ", clicks=" + clicks +
                ", userModel=" + userModel +
                '}';
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
