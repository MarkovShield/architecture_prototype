package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.UserModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

/**
 * Created by maede on 24.04.2017.
 */
public class UserModelSerializationSchema implements KeyedSerializationSchema<UserModel> {

    private final String topic;
    private final ObjectMapper mapper;

    public UserModelSerializationSchema(String topic) {
        this.topic = topic;
        mapper = new ObjectMapper();
    }

    @Override
    public byte[] serializeKey(UserModel model) {
        return model.getUserId().getBytes();
    }

    @Override
    public byte[] serializeValue(UserModel model) {
        try {
            return mapper.writeValueAsBytes(model);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new byte[1];
    }

    @Override
    public String getTargetTopic(UserModel validation) {
        return topic;
    }
}