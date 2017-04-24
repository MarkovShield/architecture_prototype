package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ValidatedClickStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;


public class ValidatedClickStreamSerializationSchema implements KeyedSerializationSchema<ValidatedClickStream> {

    private final ObjectMapper mapper;
    private final String topic;

    public ValidatedClickStreamSerializationSchema(String topic) {
        mapper = new ObjectMapper();
        this.topic = topic;
    }

    @Override
    public byte[] serializeKey(ValidatedClickStream validation) {
        return validation.getSessionUUID().getBytes();
    }

    @Override
    public byte[] serializeValue(ValidatedClickStream validation) {
        try {
            return mapper.writeValueAsBytes(validation);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new byte[1];
    }

    @Override
    public String getTargetTopic(ValidatedClickStream validation) {
        return topic;
    }
}