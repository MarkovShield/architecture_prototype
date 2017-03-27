package ch.hsr.markovshield;

import java.util.Objects;

/**
 * Created by maede on 27.03.2017.
 */
public class Login {

    private final String sessionId;
    private final String userId;

    public Login(String sessionId, String userId) {

        this.sessionId = sessionId;
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Login{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Login login = (Login) o;
        return Objects.equals(sessionId, login.sessionId) &&
                Objects.equals(userId, login.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, userId);
    }

}
