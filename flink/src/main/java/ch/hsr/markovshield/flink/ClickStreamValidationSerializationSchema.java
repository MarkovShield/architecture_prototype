package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ClickStreamValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

import static ch.hsr.markovshield.flink.MarkovShieldAnalyser.MARKOV_CLICK_STREAM_VALIDATION_TOPIC;


public class ClickStreamValidationSerializationSchema implements KeyedSerializationSchema<ClickStreamValidation> {

    private final ObjectMapper mapper;

    public ClickStreamValidationSerializationSchema() {
        mapper = new ObjectMapper();
    }

    @Override
    public byte[] serializeKey(ClickStreamValidation validation) {
        return validation.getSessionUUID().getBytes();
    }

    @Override
    public byte[] serializeValue(ClickStreamValidation validation) {
        try {
            return mapper.writeValueAsBytes(validation);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new byte[1];
    }

    @Override
    public String getTargetTopic(ClickStreamValidation validation) {
        return MARKOV_CLICK_STREAM_VALIDATION_TOPIC;
    }
}
