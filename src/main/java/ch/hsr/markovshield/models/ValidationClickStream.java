package ch.hsr.markovshield.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.connect.source.SourceTask;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ValidationClickStream {

    private final String userName;
    private final String sessionId;
    private final List<Click> clicks;
    private final UserModel userModel;
    private final Map<String, UrlConfiguration> urlConfigurations;

    @JsonCreator
    public ValidationClickStream(@JsonProperty ("userName") String userName,
                                 @JsonProperty ("sessionId") String sessionId,
                                 @JsonProperty ("clicks") List<Click> clicks,
                                 @JsonProperty ("userModel") UserModel userModel,
                                 @JsonProperty ("UrlConfiguration") Map<String, UrlConfiguration> urlConfigurations) {
        this.userName = userName;
        this.sessionId = sessionId;
        this.clicks = clicks;
        this.userModel = userModel;
        this.urlConfigurations = urlConfigurations;
    }

    public String getUserName() {
        return userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<Click> getClicks() {
        return clicks;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public Date timeStampOfLastClick() {
        return Collections.max(clicks, (Click o1, Click o2) -> {
            if (o1.getTimeStamp().equals(o2.getTimeStamp())) {
                return 0;
            }
            if (o1.getTimeStamp().before(o2.getTimeStamp())) {
                return -1;
            } else {
                return 1;
            }
        }).getTimeStamp();
    }

    public static ValidationClickStream fromClickstream(ClickStream clickStream, Map<String, UrlConfiguration> urlConfigurations) {

        ValidationClickStream validationClickStream = new ValidationClickStream(clickStream.getUserName(),
            clickStream.getSessionId(),
            clickStream.getClicks(),
            null,
            urlConfigurations);
        System.out.println(validationClickStream);
        return validationClickStream;

    }

    @Override
    public String toString() {
        return "ValidationClickStream{" +
            "userName='" + userName + '\'' +
            ", sessionId='" + sessionId + '\'' +
            ", clicks=" + clicks +
            ", userModel=" + userModel +
            '}';
    }

    public Map<String, UrlConfiguration> getUrlConfigurations() {
        return urlConfigurations;
    }

}
