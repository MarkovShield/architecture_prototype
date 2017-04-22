package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ValidationClickStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import java.io.IOException;


public class ClickStreamValidationDeserializationSchema implements KeyedDeserializationSchema<ValidationClickStream> {

    private final ObjectMapper mapper;

    public ClickStreamValidationDeserializationSchema() {
        mapper = new ObjectMapper();
    }

    @Override
    public TypeInformation<ValidationClickStream> getProducedType() {
        return TypeExtractor.getForClass(ValidationClickStream.class);
    }

    @Override
    public ValidationClickStream deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
        return mapper.readValue(bytes1, ValidationClickStream.class);
    }

    @Override
    public boolean isEndOfStream(ValidationClickStream o) {
        return false;
    }
}
