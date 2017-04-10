package ch.hsr.markovshield.models;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by maede on 05.04.2017.
 */
public class ClickCount {

    String userName;
    int clicks;
    List<ValidationClickStream> clickStreamList;
    boolean multipleUsers;
    public ClickCount(String userName, int clicks) {
        this.userName = userName;
        this.clicks = clicks;
        this.clickStreamList = new LinkedList<>();
    }

    public ClickCount() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public void addClickStream(ValidationClickStream clickStream){
        this.clickStreamList.add(clickStream);
    }

    public boolean isMultipleUsers() {
        return multipleUsers;
    }

    public void setMultipleUsers(boolean multipleUsers) {
        this.multipleUsers = multipleUsers;
    }

    @Override
    public String toString() {
        return "ClickCount{" +
            "userName='" + userName + '\'' +
            ", clicks=" + clicks +
            ", clickStreamList=" + clickStreamList +
            ", multipleUsers=" + multipleUsers +
            '}';
    }
}
