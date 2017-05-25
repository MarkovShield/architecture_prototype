package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ValidatedClickStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import java.io.IOException;

public class ValidatedClickStreamDeserializationSchema implements KeyedDeserializationSchema<ValidatedClickStream> {

    private final ObjectMapper mapper;
    private final TypeInformation<ValidatedClickStream> forClass;

    public ValidatedClickStreamDeserializationSchema() {
        mapper = new ObjectMapper();
        forClass = TypeExtractor.getForClass(ValidatedClickStream.class);
    }

    @Override
    public TypeInformation<ValidatedClickStream> getProducedType() {
        return forClass;
    }

    @Override
    public ValidatedClickStream deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
        return mapper.readValue(bytes1, ValidatedClickStream.class);
    }

    @Override
    public boolean isEndOfStream(ValidatedClickStream o) {
        return false;
    }
}
