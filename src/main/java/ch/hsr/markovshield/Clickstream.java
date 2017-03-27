package ch.hsr.markovshield;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by maede on 27.03.2017.
 */
public class ClickStream {

    private final Login login;
    private final List<Click> clicks;

    public ClickStream(Login login, Click click) {
        this.login = login;
        clicks = new LinkedList<>();
        clicks.add(click);
    }

    private ClickStream(Login login, List<Click> clicks) {
        this.login = login;
        this.clicks = clicks;
    }

    @Override
    public String toString() {
        return "ClickStream{" +
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

    public ClickStream appendClick(ClickStream clickStream) {
        System.out.println("appending Click");
        if (this.login.equals(clickStream.getLogin())) {
            LinkedList<Click> clicks = new LinkedList<>(this.getClicks());
            clicks.addAll(clickStream.getClicks());
            return new ClickStream(this.login, clicks);
        } else {
            return this;
        }
    }
}
