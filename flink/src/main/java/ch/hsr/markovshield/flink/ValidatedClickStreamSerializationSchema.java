package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ValidatedClickStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

import static ch.hsr.markovshield.flink.MarkovShieldAnalyser.MARKOV_VALIDATED_CLICK_STREAMS;


public class ValidatedClickStreamSerializationSchema implements KeyedSerializationSchema<ValidatedClickStream> {

    private final ObjectMapper mapper;

    public ValidatedClickStreamSerializationSchema() {
        mapper = new ObjectMapper();
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
        return MARKOV_VALIDATED_CLICK_STREAMS;
    }
}
