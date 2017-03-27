package ch.hsr.markovshield;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by maede on 27.03.2017.
 */
public class Clickstream {

    private final Login login;
    private final List<Click> clicks;

    public Clickstream(Login login){
        this.login = login;
        clicks = new LinkedList<>();
    }

    @Override
    public String toString() {
        return "Clickstream{" +
                "login=" + login +
                ", clicks=" + clicks +
                '}';
    }

    public Login getLogin() {
        return login;
    }

    public List<Click> getClicks() {
        return new LinkedList(clicks);
    }

    public void addClick(Click click){
        this.clicks.add(click);
    }
}
